package com.kanyandula.nyasa.ui.main.blog

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.FragmentViewBlogBinding
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.ui.AreYouSureCallback
import com.kanyandula.nyasa.ui.UIMessage
import com.kanyandula.nyasa.ui.UIMessageType
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.ui.main.blog.state.BlogNavigationEvent
import com.kanyandula.nyasa.util.DateUtils
import kotlinx.coroutines.launch

class ViewBlogFragment : BaseBlogFragment<FragmentViewBlogBinding>(FragmentViewBlogBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        subscribeObservers()
        checkIsAuthorOfBlogPost()
        stateChangeListener.expandAppBar()
        binding?.deleteButton?.setOnClickListener {
            confirmDeleteRequest()
        }
    }

    override fun handleUiEvent(event: UiEvent) {
        when (event) {
            is BlogNavigationEvent.BlogDeleted -> {
                findNavController().popBackStack()
            }
            else -> super.handleUiEvent(event)
        }
    }

    private fun confirmDeleteRequest() {
        val callback: AreYouSureCallback = object : AreYouSureCallback {
            override fun proceed() {
                viewModel.deleteBlogPost()
            }

            override fun cancel() {
                // ignore
            }
        }
        uiCommunicationListener.onUIMessageReceived(
            UIMessage(
                getString(R.string.are_you_sure_delete),
                UIMessageType.AreYouSureDialog(callback)
            )
        )
    }

    private fun checkIsAuthorOfBlogPost() {
        viewModel.setIsAuthorOfBlogPost(false)
        viewModel.checkIsAuthorOfBlogPost()
    }

    private fun subscribeObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewBlogState.collect { state ->
                    state.blogPost?.let { blogPost ->
                        setBlogProperties(blogPost)
                    }

                    if (state.isAuthorOfBlogPost) {
                        adaptViewToAuthorMode()
                    }
                }
            }
        }
    }

    private fun adaptViewToAuthorMode() {
        activity?.invalidateOptionsMenu()
        binding?.deleteButton?.visibility = View.VISIBLE
    }

    fun setBlogProperties(blogPost: BlogPost) {
        binding?.let {
            Glide.with(this@ViewBlogFragment)
                .load(blogPost.image)
                .into(it.blogImage)
        }

        binding?.apply {
            blogTitle.text = blogPost.title
            blogAuthor.text = blogPost.username
            blogUpdateDate.text = DateUtils.convertLongToStringDate(blogPost.date_updated)
            blogBody.text = blogPost.body
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(
            object : MenuProvider {
                override fun onPrepareMenu(menu: Menu) { /* no-op */ }

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    if (viewModel.isAuthorOfBlogPost()) {
                        menuInflater.inflate(R.menu.edit_view_menu, menu)
                    }
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    if (viewModel.isAuthorOfBlogPost()) {
                        when (menuItem.itemId) {
                            R.id.edit -> {
                                navUpdateBlogFragment()
                                return true
                            }
                        }
                    }
                    return true
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    @Suppress("TooGenericExceptionCaught")
    private fun navUpdateBlogFragment() {
        try {
            viewModel.setUpdatedBlogFields(
                viewModel.getBlogPost().title,
                viewModel.getBlogPost().body,
                viewModel.getBlogPost().image.toUri()
            )
            findNavController().navigate(R.id.action_viewBlogFragment_to_updateBlogFragment)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
        }
    }
}
