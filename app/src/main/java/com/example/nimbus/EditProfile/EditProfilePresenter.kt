package com.example.nimbus.EditProfile

// Interface for the View (Activity) to implement
interface EditProfileView {
    fun onUserDataLoaded(userData: Map<String, String>)
    fun onUpdateSuccess()
    fun onUpdateFailure()
}

class EditProfilePresenter(private val view: EditProfileView, private val model: EditProfileModel) {

    // Load user data from the Model and pass it to the View
    fun loadUserData() {
        val userData = model.getUserData()
        view.onUserDataLoaded(userData)
    }

    // Update user data using the Model
    fun updateUserData(
        newFirstName: String,
        newMiddleName: String? = null,
        newLastName: String? = null
    ) {
        model.updateUserData(newFirstName, newMiddleName, newLastName) { success ->
            if (success) {
                view.onUpdateSuccess()
            } else {
                view.onUpdateFailure()
            }
        }
    }
}
