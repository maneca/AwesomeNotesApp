package com.joao.awesomenotesapp.util

sealed class CustomExceptions(val message: String? = null) {
    object NoInternetConnectionException : CustomExceptions()

    object UnknownException : CustomExceptions()

    object ApiNotResponding : CustomExceptions()

    class ConflictException(message : String) : CustomExceptions(message)
}