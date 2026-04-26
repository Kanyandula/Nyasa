package com.kanyandula.nyasa.work

import com.kanyandula.nyasa.util.AppError

/**
 * Worker retry policy for an [AppError] surfaced by `safeApiCall`.
 *
 * - Transient (true): WorkManager should retry. Network-level or server-side
 *   conditions that may resolve themselves on a later attempt.
 * - Terminal (false): WorkManager should fail. The state can't be fixed by
 *   retrying — auth needs renewal, request shape is wrong, or the resource
 *   isn't there. Failing surfaces the failure to the user via the existing
 *   `WorkInfo` toast in `MainActivity`.
 */
internal fun AppError.isTransient(): Boolean = when (this) {
    AppError.Offline,
    AppError.Timeout,
    is AppError.Server,
    is AppError.Unknown -> true
    AppError.Unauthorized,
    AppError.Forbidden,
    AppError.NotFound,
    is AppError.Validation -> false
}

/**
 * Stable identifier written to `WorkInfo.outputData[OUTPUT_ERROR]` for terminal
 * (non-transient) failures. Logs and tests inspect this; the user-facing toast
 * is dispatched separately by `MainActivity`'s `WorkInfo` observer.
 *
 * Calling on a transient error is a programmer mistake — we'd never reach
 * `failureFor` from a transient case in the worker.
 */
internal fun AppError.reasonCode(): String = when (this) {
    AppError.Unauthorized -> UploadFailureReasons.UNAUTHORIZED
    AppError.Forbidden -> UploadFailureReasons.FORBIDDEN
    AppError.NotFound -> UploadFailureReasons.NOT_FOUND
    is AppError.Validation -> UploadFailureReasons.VALIDATION
    AppError.Offline, AppError.Timeout, is AppError.Server, is AppError.Unknown ->
        error("reasonCode() called on transient error $this")
}

/** Stable string identifiers persisted to `WorkInfo.outputData[OUTPUT_ERROR]`. */
internal object UploadFailureReasons {
    const val INVALID_INPUT = "InvalidInput"
    const val IMAGE_MISSING = "ImageMissing"
    const val UNAUTHORIZED = "Unauthorized"
    const val FORBIDDEN = "Forbidden"
    const val NOT_FOUND = "NotFound"
    const val VALIDATION = "Validation"
    const val UNEXPECTED_LOADING = "UnexpectedLoading"
}
