package com.example.nimbus.History

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nimbus.R
import com.google.android.material.snackbar.Snackbar

class HistoryActivity : AppCompatActivity(), HistoryContract.View {

    private lateinit var presenter: HistoryPresenter
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        // Set edge-to-edge window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize presenter WITH CONTEXT - pass 'this' as context
        presenter = HistoryPresenter(this, this)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.rainHistoryRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with empty list
        adapter = NotificationAdapter(emptyList())
        recyclerView.adapter = adapter

        // Fetch notifications
        presenter.fetchNotifications()
    }

    override fun showNotifications(notifications: List<NotificationData>) {
        // Update adapter with new data
        adapter.updateNotifications(notifications)

        // Show message if no notifications
        if (notifications.isEmpty()) {
            Snackbar.make(findViewById(R.id.main), "No rain notifications found", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun showError(message: String) {
        Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG).show()
    }

    override fun showLoading() {
        // You can show a ProgressBar here
        // For now, we'll just show a loading snackbar
        Snackbar.make(findViewById(R.id.main), "Loading notifications...", Snackbar.LENGTH_SHORT).show()
    }

    override fun hideLoading() {
        // Hide loading indicator
        // No action needed for now since we're using snackbar
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources if needed
    }
}

// Notification Adapter
class NotificationAdapter(private var notifications: List<NotificationData>) :
    RecyclerView.Adapter<NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): NotificationViewHolder {
        // Use your rain_history_item.xml layout
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.rain_history_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int = notifications.size

    fun updateNotifications(newNotifications: List<NotificationData>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}

// Notification ViewHolder
class NotificationViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {

    private val timestampText: android.widget.TextView =
        itemView.findViewById(R.id.timestampText)
    private val cardView: androidx.cardview.widget.CardView =
        itemView.findViewById(R.id.rainHistoryCard)

    fun bind(notification: NotificationData) {
        // Format date and time
        val formattedTime = formatTime(notification.time)
        val dateTime = "${notification.date} ${formattedTime}"

        // Since we're only fetching rainStatus = true, we can assume it's always rain detected
        // But keeping the structure in case you want to show all statuses later
        val statusText = if (notification.rainStatus) {
            // Keep blue color for rain detected
            cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#75C2E6")) // Original blue
            "Rain detected on: $dateTime"
        } else {
            // This should not happen since we're filtering for rainStatus = true
            // But keeping it for safety
            cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#90EE90")) // Light green
            "No rain detected on: $dateTime"
        }

        timestampText.text = statusText
    }

    private fun formatTime(time: String): String {
        // Format time from 24-hour to 12-hour format if needed
        return try {
            val inputFormat = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            val date = inputFormat.parse(time)
            outputFormat.format(date ?: time)
        } catch (e: Exception) {
            time // Return original if parsing fails
        }
    }
}