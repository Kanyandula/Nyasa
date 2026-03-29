@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.ImagePicker.Companion.EXTRA_FILE_PATH
import com.kanyandula.nyasa.ui.DataStateChangeListener
import com.kanyandula.nyasa.ui.handleStandardUiEvent
import com.kanyandula.nyasa.ui.main.create_blog.composables.CreateBlogScreen
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_MUST_SELECT_IMAGE
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_SOMETHING_WRONG_WITH_IMAGE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class CreateBlogFragment : Fragment() {

    private val viewModel: CreateBlogViewModel by activityViewModels()
    lateinit var stateChangeListener: DataStateChangeListener

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (it.data?.hasExtra(EXTRA_FILE_PATH) == true) {
                    val uri = it.data?.data
                    if (uri != null) {
                        viewModel.setNewBlogFields(title = null, body = null, uri = uri)
                    }
                } else {
                    stateChangeListener.displayErrorDialog(ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                NyasaTheme {
                    val blogFields by viewModel.viewState
                        .map { it.blogFields }
                        .distinctUntilChanged()
                        .collectAsStateWithLifecycle(
                            initialValue = viewModel.viewState.value.blogFields
                        )
                    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

                    LaunchedEffect(Unit) {
                        viewModel.events.collect { event ->
                            handleStandardUiEvent(event, stateChangeListener)
                        }
                    }

                    CreateBlogScreen(
                        initialTitle = blogFields.newBlogTitle.orEmpty(),
                        initialBody = blogFields.newBlogBody.orEmpty(),
                        imageUri = blogFields.newImageUri,
                        isLoading = isLoading,
                        onPublish = { title, body ->
                            val imageUri = viewModel.viewState.value.blogFields.newImageUri
                            if (imageUri == null) {
                                stateChangeListener.displayErrorDialog(ERROR_MUST_SELECT_IMAGE)
                                return@CreateBlogScreen
                            }
                            viewModel.createNewBlogPost(title, body, imageUri)
                            stateChangeListener.hideSoftKeyboard()
                        },
                        onPickImage = { pickGalleryImage() }
                    )
                }
            }
        }
    }

    private fun pickGalleryImage() {
        galleryLauncher.launch(
            ImagePicker.with(requireActivity())
                .crop(1130F, 961F)
                .galleryOnly()
                .setOutputFormat(Bitmap.CompressFormat.JPEG)
                .galleryMimeTypes(
                    mimeTypes = arrayOf("image/png", "image/jpg", "image/jpeg")
                )
                .createIntent()
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Log.e("AppDebug", "$context must implement DataStateChangeListener")
        }
    }
}
