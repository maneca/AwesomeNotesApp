package com.joao.awesomenotesapp.util

import android.net.Uri


sealed class UiEvent(){

    object NoteDeleted : UiEvent()
    object UserLoggedOut: UiEvent()
    object NoteSaved : UiEvent()
    object Failed : UiEvent()
    object NoInternetConnection : UiEvent()
    object UserLoggedIn: UiEvent()
    object UploadingNote: UiEvent()
    class ImageUrl(val uri: Uri) : UiEvent()
}
