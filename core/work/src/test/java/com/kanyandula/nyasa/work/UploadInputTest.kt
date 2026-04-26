package com.kanyandula.nyasa.work

import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UploadInputTest {

    @Test
    fun `from valid Data produces UploadInput`() {
        val data = workDataOf(
            UploadKeys.INPUT_TITLE to "T",
            UploadKeys.INPUT_BODY to "B",
            UploadKeys.INPUT_CATEGORY to "Tech",
            UploadKeys.INPUT_TAGS_CSV to "kotlin,android",
            UploadKeys.INPUT_IMAGE_PATH to "/tmp/img.jpg",
            UploadKeys.INPUT_SLUG to "post-slug",
        )

        val input = UploadInput.from(data)

        assertThat(input).isNotNull()
        input!!
        assertThat(input.title).isEqualTo("T")
        assertThat(input.body).isEqualTo("B")
        assertThat(input.category).isEqualTo("Tech")
        assertThat(input.tagsCsv).isEqualTo("kotlin,android")
        assertThat(input.imageFile?.path).isEqualTo("/tmp/img.jpg")
        assertThat(input.slug).isEqualTo("post-slug")
    }

    @Test
    fun `from missing title is null`() {
        val data = workDataOf(UploadKeys.INPUT_BODY to "B")
        assertThat(UploadInput.from(data)).isNull()
    }

    @Test
    fun `from missing body is null`() {
        val data = workDataOf(UploadKeys.INPUT_TITLE to "T")
        assertThat(UploadInput.from(data)).isNull()
    }

    @Test
    fun `from blank title is null`() {
        val data = workDataOf(
            UploadKeys.INPUT_TITLE to "   ",
            UploadKeys.INPUT_BODY to "B",
        )
        assertThat(UploadInput.from(data)).isNull()
    }

    @Test
    fun `from create flow has null slug`() {
        val data = workDataOf(
            UploadKeys.INPUT_TITLE to "T",
            UploadKeys.INPUT_BODY to "B",
        )
        val input = UploadInput.from(data)
        assertThat(input?.slug).isNull()
    }

    @Test
    fun `from update flow has slug populated`() {
        val data = workDataOf(
            UploadKeys.INPUT_TITLE to "T",
            UploadKeys.INPUT_BODY to "B",
            UploadKeys.INPUT_SLUG to "my-slug",
        )
        val input = UploadInput.from(data)
        assertThat(input?.slug).isEqualTo("my-slug")
    }

    @Test
    fun `from null image path leaves imageFile null`() {
        val data = workDataOf(
            UploadKeys.INPUT_TITLE to "T",
            UploadKeys.INPUT_BODY to "B",
        )
        val input = UploadInput.from(data)
        assertThat(input?.imageFile).isNull()
    }
}
