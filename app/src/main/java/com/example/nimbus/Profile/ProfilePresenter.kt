package com.example.nimbus.Profile

class ProfilePresenter(private val view: ProfileView, private val model: ProfileModel) {

    fun loadProfile() {
        val profile = model.getProfile()
        view.displayProfile(profile)
    }
}

// This interface is used to communicate from Presenter -> Activity (View)
interface ProfileView {
    fun displayProfile(profile: Profile)
}
