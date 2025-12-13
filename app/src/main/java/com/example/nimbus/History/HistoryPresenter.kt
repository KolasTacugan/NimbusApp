package com.example.nimbus.History

import android.content.Context

class HistoryPresenter(
    private val view: HistoryContract.View,
    private val context: Context
) : HistoryContract.Presenter {

    private lateinit var model: HistoryModel

    init {
        model = HistoryModel(this, context)
    }

    override fun fetchNotifications() {
        view.showLoading()
        model.fetchNotifications()
    }

    override fun onNotificationsFetched(notifications: List<NotificationData>) {
        view.hideLoading()
        view.showNotifications(notifications)
    }

    override fun onError(message: String) {
        view.hideLoading()
        view.showError(message)
    }
}