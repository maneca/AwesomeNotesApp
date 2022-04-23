package com.joao.awesomenotesapp.util

import android.net.Uri


sealed class UiEvent(val uri: Uri = Uri.EMPTY){

    object NoteDeleted : UiEvent()
    object UserLoggedOut: UiEvent()
    object NoteSaved : UiEvent()
    object Failed : UiEvent()
    object NoInternetConnection : UiEvent()
    object UserLoggedIn: UiEvent()
    object SyncSuccessful: UiEvent()
    object UploadingNote: UiEvent()
    class ImageUrl(uri: Uri) : UiEvent(uri)
}
