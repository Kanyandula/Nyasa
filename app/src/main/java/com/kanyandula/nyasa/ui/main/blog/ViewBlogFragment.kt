package com.kanyandula.nyasa.ui.main.blog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.ui.AreYouSureCallback
import com.kanyandula.nyasa.ui.DataStateChangeListener
import com.kanyandula.nyasa.ui.UICommunicationListener
import com.kanyandula.nyasa.ui.UIMessage
import com.kanyandula.nyasa.ui.UIMessageType
import com.kanyandula.nyasa.ui.handleStandardUiEvent
import com.kanyandula.nyasa.ui.main.blog.composables.BlogDetailScreen
import com.kanyandula.nyasa.ui.main.blog.state.BlogNavigationEvent
import com.kanyandula.nyasa.ui.main.blog.viewmodel.BlogViewModel
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewBlogFragment : Fragment() {

    private val viewModel: BlogViewModel by activityViewModels()
    private val args: ViewBlogFragmentArgs by navArgs()
    lateinit var stateChangeListener: DataStateChangeListener
    lateinit var uiCommunicationListener: UICommunicationListener

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
                    val state by viewModel.viewBlogState.collectAsStateWithLifecycle()
                    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

                    LaunchedEffect(args.blogSlug) {
                        viewModel.loadBlogBySlug(args.blogSlug)
                        viewModel.checkIsAuthorOfBlogPost(args.blogSlug)
                    }

                    LaunchedEffect(Unit) {
                        viewModel.events.collect { event ->
                            when (event) {
                                is BlogNavigationEvent.BlogDeleted -> {
                                    findNavController().popBackStack()
                                }
                                else -> handleStandardUiEvent(event, stateChangeListener)
                            }
                        }
                    }

                    BlogDetailScreen(
                        blogPost = state.blogPost,
                        isAuthor = state.isAuthorOfBlogPost,
                        isLoading = isLoading,
                        onEditClick = {
                            val blogPost = viewModel.getBlogPost() ?: return@BlogDetailScreen
                            viewModel.setUpdatedBlogFields(
                                blogPost.title,
                                blogPost.body,
                                blogPost.image.toUri()
                            )
                            findNavController().navigate(
                                ViewBlogFragmentDirections
                                    .actionViewBlogFragmentToUpdateBlogFragment(args.blogSlug)
                            )
                        },
                        onDeleteClick = { confirmDeleteRequest() },
                        onNavigateBack = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }

    private fun confirmDeleteRequest() {
        val callback: AreYouSureCallback = object : AreYouSureCallback {
            override fun proceed() {
                viewModel.deleteBlogPost()
            }

            override fun cancel() { /* ignore */ }
        }
        uiCommunicationListener.onUIMessageReceived(
            UIMessage(
                getString(R.string.are_you_sure_delete),
                UIMessageType.AreYouSureDialog(callback)
            )
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
            uiCommunicationListener = context as UICommunicationListener
        } catch (e: ClassCastException) {
            Log.e("AppDebug", "$context must implement DataStateChangeListener")
        }
    }
}
