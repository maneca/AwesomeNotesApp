package com.joao.awesomenotesapp.util

sealed class Resource<T> (
    val data: T? = null,
    val exception: CustomExceptions? = null
) {
    class Loading<T>(data: T? = null): Resource<T>(data)

    class Success<T>(data: T?): Resource<T>(data)

    class Error<T>(data: T? = null, exception: CustomExceptions?): Resource<T>(data, exception)
}