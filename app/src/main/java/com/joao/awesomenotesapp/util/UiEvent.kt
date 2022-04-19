package com.joao.awesomenotesapp.util


sealed class UiEvent{

    object NoteDeleted : UiEvent()
    object UserLoggedOut: UiEvent()
    object NoteSaved : UiEvent()
    object Failed : UiEvent()
    object NoInternetConnection : UiEvent()
    object UserLoggedIn: UiEvent()
}
