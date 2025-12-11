package com.example.nimbus.ClotheslineStatus

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ClotheslineStatusModel {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val clotheslineRef: DatabaseReference = database.getReference("clothsline_status")

    // Data class to represent the clothesline status
    data class ClotheslineStatus(
        val automaticMode: Boolean = true,
        val manualShade: Int = 30,
        val rainStatus: Boolean = true,
        val shadeStatus: Boolean = true
    )

    interface StatusListener {
        fun onStatusChanged(status: ClotheslineStatus)
        fun onError(error: String)
    }

    interface OperationListener {
        fun onSuccess()
        fun onError(error: String)
    }

    // Create/Update the entire status
    fun updateClotheslineStatus(status: ClotheslineStatus, listener: OperationListener) {
        clotheslineRef.setValue(status)
            .addOnSuccessListener {
                listener.onSuccess()
            }
            .addOnFailureListener { exception ->
                listener.onError("Failed to update status: ${exception.message}")
            }
    }

    // Read - Get current status with real-time updates
    fun getCurrentStatus(listener: StatusListener) {
        clotheslineRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val status = snapshot.getValue(ClotheslineStatus::class.java)
                    if (status != null) {
                        listener.onStatusChanged(status)
                    } else {
                        listener.onError("Status data not found")
                    }
                } catch (e: Exception) {
                    listener.onError("Error parsing data: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                listener.onError("Database error: ${error.message}")
            }
        })
    }

    // Update individual fields

    fun updateAutomaticMode(isAutomatic: Boolean, listener: OperationListener) {
        clotheslineRef.child("automaticMode").setValue(isAutomatic)
            .addOnSuccessListener {
                listener.onSuccess()
            }
            .addOnFailureListener { exception ->
                listener.onError("Failed to update automatic mode: ${exception.message}")
            }
    }

    fun updateManualShade(minutes: Int, listener: OperationListener) {
        clotheslineRef.child("manualShade").setValue(minutes)
            .addOnSuccessListener {
                listener.onSuccess()
            }
            .addOnFailureListener { exception ->
                listener.onError("Failed to update manual shade: ${exception.message}")
            }
    }

    fun updateRainStatus(isRaining: Boolean, listener: OperationListener) {
        clotheslineRef.child("rainStatus").setValue(isRaining)
            .addOnSuccessListener {
                listener.onSuccess()
            }
            .addOnFailureListener { exception ->
                listener.onError("Failed to update rain status: ${exception.message}")
            }
    }

    fun updateShadeStatus(isRetracted: Boolean, listener: OperationListener) {
        clotheslineRef.child("shadeStatus").setValue(isRetracted)
            .addOnSuccessListener {
                listener.onSuccess()
            }
            .addOnFailureListener { exception ->
                listener.onError("Failed to update shade status: ${exception.message}")
            }
    }

    // Read single value without real-time updates
    fun getSingleValue(childPath: String, listener: (Any?) -> Unit) {
        clotheslineRef.child(childPath).get()
            .addOnSuccessListener { snapshot ->
                listener(snapshot.value)
            }
            .addOnFailureListener { exception ->
                listener(null)
            }
    }

    // Remove real-time listener (important to prevent memory leaks)
    fun removeStatusListener() {
        // Since we're adding anonymous listeners, we need to store reference to remove them
        // In a real implementation, you might want to store the ValueEventListener
    }

    companion object {
        // Helper method to get readable rain status
        fun getRainStatusText(isRaining: Boolean): String {
            return if (isRaining) "Raining" else "Not Detected"
        }

        // Helper method to get readable shade status
        fun getShadeStatusText(isRetracted: Boolean): String {
            return if (isRetracted) "Retracted" else "Closed"
        }
    }
}