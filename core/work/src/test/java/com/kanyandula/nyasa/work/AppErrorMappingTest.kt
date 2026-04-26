package com.kanyandula.nyasa.work

import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.util.AppError
import org.junit.Test

class AppErrorMappingTest {

    @Test
    fun `Offline is transient`() {
        assertThat(AppError.Offline.isTransient()).isTrue()
    }

    @Test
    fun `Timeout is transient`() {
        assertThat(AppError.Timeout.isTransient()).isTrue()
    }

    @Test
    fun `Server is transient`() {
        assertThat(AppError.Server(code = 503).isTransient()).isTrue()
    }

    @Test
    fun `Unknown is transient`() {
        assertThat(AppError.Unknown(cause = RuntimeException("boom")).isTransient()).isTrue()
    }

    @Test
    fun `Unauthorized is terminal`() {
        assertThat(AppError.Unauthorized.isTransient()).isFalse()
    }

    @Test
    fun `Forbidden is terminal`() {
        assertThat(AppError.Forbidden.isTransient()).isFalse()
    }

    @Test
    fun `NotFound is terminal`() {
        assertThat(AppError.NotFound.isTransient()).isFalse()
    }

    @Test
    fun `Validation is terminal`() {
        assertThat(AppError.Validation(fields = mapOf("title" to "required")).isTransient())
            .isFalse()
    }

    @Test
    fun `terminal errors emit stable reason codes`() {
        assertThat(AppError.Unauthorized.reasonCode())
            .isEqualTo(UploadFailureReasons.UNAUTHORIZED)
        assertThat(AppError.Forbidden.reasonCode())
            .isEqualTo(UploadFailureReasons.FORBIDDEN)
        assertThat(AppError.NotFound.reasonCode())
            .isEqualTo(UploadFailureReasons.NOT_FOUND)
        assertThat(AppError.Validation(emptyMap()).reasonCode())
            .isEqualTo(UploadFailureReasons.VALIDATION)
    }

    @Test(expected = IllegalStateException::class)
    fun `reasonCode on transient error is a programmer mistake`() {
        AppError.Offline.reasonCode()
    }
}
