package com.kanyandula.nyasa.work

object UploadKeys {
    const val INPUT_TITLE = "upload_title"
    const val INPUT_BODY = "upload_body"
    const val INPUT_CATEGORY = "upload_category"
    const val INPUT_TAGS_CSV = "upload_tags_csv"
    const val INPUT_IMAGE_PATH = "upload_image_path"

    /** Present → PUT update on this slug. Absent → POST create. */
    const val INPUT_SLUG = "upload_slug_for_update"

    const val PROGRESS_PERCENT = "upload_progress_percent"

    const val OUTPUT_SLUG = "upload_slug"
    const val OUTPUT_MESSAGE = "upload_message"
    const val OUTPUT_ERROR = "upload_error"

    const val WORK_TAG_UPLOAD = "blog_upload"
    const val NOTIFICATION_CHANNEL_ID = "upload_progress"
}
