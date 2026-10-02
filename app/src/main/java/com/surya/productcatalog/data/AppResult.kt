package com.surya.productcatalog.data

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

/** Outcome of a network call: either the data or a user-friendly error message. */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val message: String) : AppResult<Nothing>
}

/**
 * Runs [block] and turns any exception into an [AppResult.Error] with a friendly message.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        // A cancelled coroutine (e.g. an outdated search) is not an error - let it stop.
        throw e
    } catch (e: Exception) {
        AppResult.Error(e.toUserMessage())
    }

/** SocketTimeoutException is a subclass of IOException, so it must be checked first. */
fun Throwable.toUserMessage(): String = when (this) {
    is SocketTimeoutException -> "Request timed out"
    is IOException -> "No internet connection"
    is HttpException -> "Server error, please try again"
    else -> "Something went wrong, please try again"
}
