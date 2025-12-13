package com.example.nimbus.History

import android.content.Context
import com.google.firebase.database.Exclude

data class NotificationData(
    val id: String = "",
    val date: String = "",
    val loggedUID: String = "",
    val rainStatus: Boolean = false,
    val time: String = ""
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "date" to date,
            "loggedUID" to loggedUID,
            "rainStatus" to rainStatus,
            "time" to time
        )
    }
}

class HistoryModel(
    private val presenter: HistoryContract.Presenter,
    private val context: Context
) : HistoryContract.Model {

    private val firebaseHelper = FirebaseHelper()

    override fun fetchNotifications() {
        // Get user ID from SharedPreferences
        val userId = getUserIdFromSharedPreferences()

        if (userId.isNullOrEmpty()) {
            presenter.onError("User not found. Please log in first.")
            return
        }

        firebaseHelper.fetchUserRainNotifications(userId) { notifications ->
            if (notifications != null) {
                presenter.onNotificationsFetched(notifications)
            } else {
                presenter.onError("Failed to fetch rain notifications")
            }
        }
    }

    private fun getUserIdFromSharedPreferences(): String? {
        val sharedPref = context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
        return sharedPref.getString("USER_UID", null)
    }
}

// Firebase Helper Class
class FirebaseHelper {

    fun fetchUserRainNotifications(userId: String, callback: (List<NotificationData>?) -> Unit) {
        val database = com.google.firebase.database.FirebaseDatabase.getInstance()
        val notificationsRef = database.getReference("notifications")

        // Create a query that filters by loggedUID = current user's ID
        val query = notificationsRef
            .orderByChild("loggedUID")
            .equalTo(userId)

        query.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val notificationsList = mutableListOf<NotificationData>()

                for (childSnapshot in snapshot.children) {
                    val notification = childSnapshot.getValue(NotificationData::class.java)

                    // Apply additional filter for rainStatus = true
                    if (notification != null && notification.rainStatus == true) {
                        val notificationWithId = notification.copy(id = childSnapshot.key ?: "")
                        notificationsList.add(notificationWithId)
                    }
                }

                // Sort by date and time (most recent first)
                val sortedList = notificationsList.sortedWith(
                    compareByDescending<NotificationData> { it.date }
                        .thenByDescending { it.time }
                )

                callback(sortedList)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                callback(null)
            }
        })
    }
}

// Alternative method with client-side filtering only
class FirebaseHelperAlternative {

    fun fetchUserRainNotificationsAlternative(userId: String, callback: (List<NotificationData>?) -> Unit) {
        val database = com.google.firebase.database.FirebaseDatabase.getInstance()
        val notificationsRef = database.getReference("notifications")

        notificationsRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val notificationsList = mutableListOf<NotificationData>()

                for (childSnapshot in snapshot.children) {
                    val notification = childSnapshot.getValue(NotificationData::class.java)

                    // Filter by both conditions:
                    // 1. loggedUID matches current user (from SharedPreferences)
                    // 2. rainStatus is true
                    if (notification != null &&
                        notification.loggedUID == userId &&
                        notification.rainStatus == true) {

                        val notificationWithId = notification.copy(id = childSnapshot.key ?: "")
                        notificationsList.add(notificationWithId)
                    }
                }

                // Sort by date and time (most recent first)
                val sortedList = notificationsList.sortedWith(
                    compareByDescending<NotificationData> { it.date }
                        .thenByDescending { it.time }
                )

                callback(sortedList)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                callback(null)
            }
        })
    }
}

// Contract interface (unchanged)
interface HistoryContract {
    interface View {
        fun showNotifications(notifications: List<NotificationData>)
        fun showError(message: String)
        fun showLoading()
        fun hideLoading()
    }

    interface Presenter {
        fun fetchNotifications()
        fun onNotificationsFetched(notifications: List<NotificationData>)
        fun onError(message: String)
    }

    interface Model {
        fun fetchNotifications()
    }
}