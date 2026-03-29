package com.kanyandula.nyasa.ui.main.blog

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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.drjacky.imagepicker.ImagePicker
import com.kanyandula.nyasa.ui.DataStateChangeListener
import com.kanyandula.nyasa.ui.handleStandardUiEvent
import com.kanyandula.nyasa.ui.main.blog.composables.EditBlogScreen
import com.kanyandula.nyasa.ui.main.blog.state.BlogNavigationEvent
import com.kanyandula.nyasa.ui.main.blog.viewmodel.BlogViewModel
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.util.ErrorHandling
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateBlogFragment : Fragment() {

    private val viewModel: BlogViewModel by activityViewModels()
    private val args: UpdateBlogFragmentArgs by navArgs()
    lateinit var stateChangeListener: DataStateChangeListener

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (it.data?.hasExtra(ImagePicker.EXTRA_FILE_PATH) == true) {
                    val uri = it.data?.data
                    if (uri != null) {
                        viewModel.setUpdatedBlogFields(title = null, body = null, uri = uri)
                    }
                } else {
                    stateChangeListener.displayErrorDialog(
                        ErrorHandling.ERROR_SOMETHING_WRONG_WITH_IMAGE
                    )
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
                    val state by viewModel.updateBlogState.collectAsStateWithLifecycle()
                    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

                    LaunchedEffect(Unit) {
                        viewModel.events.collect { event ->
                            when (event) {
                                is BlogNavigationEvent.BlogUpdateSuccess -> {
                                    findNavController().popBackStack()
                                }
                                else -> handleStandardUiEvent(event, stateChangeListener)
                            }
                        }
                    }

                    EditBlogScreen(
                        initialTitle = state.updatedBlogTitle.orEmpty(),
                        initialBody = state.updatedBlogBody.orEmpty(),
                        imageUri = state.updatedImageUri,
                        isLoading = isLoading,
                        onSave = { title, body ->
                            viewModel.updateBlogPost(
                                slug = args.blogSlug,
                                title = title,
                                body = body,
                                imageUri = viewModel.getUpdatedBlogUri()
                            )
                            stateChangeListener.hideSoftKeyboard()
                        },
                        onPickImage = { pickGalleryImage() },
                        onNavigateBack = { findNavController().popBackStack() }
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
