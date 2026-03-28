@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.ImagePicker.Companion.EXTRA_FILE_PATH
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.FragmentCreateBlogBinding
import com.kanyandula.nyasa.ui.AreYouSureCallback
import com.kanyandula.nyasa.ui.UIMessage
import com.kanyandula.nyasa.ui.UIMessageType
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_MUST_SELECT_IMAGE
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_SOMETHING_WRONG_WITH_IMAGE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateBlogFragment : BaseCreateBlogFragment<FragmentCreateBlogBinding>(FragmentCreateBlogBinding::inflate) {

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (it.data?.hasExtra(EXTRA_FILE_PATH)!!) {
                    val uri = it.data?.data!!
                    viewModel.setNewBlogFields(
                        title = null,
                        body = null,
                        uri = uri
                    )
                } else {
                    stateChangeListener.displayErrorDialog(ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        binding?.apply {
            blogImage.setOnClickListener {
                pickGalleryImage()
            }

            publish.setOnClickListener {
                publishNewBlog()
            }
        }

        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState
                    .map { it.blogFields }
                    .distinctUntilChanged()
                    .collect { newBlogFields ->
                        setBlogProperties(
                            newBlogFields.newBlogTitle,
                            newBlogFields.newBlogBody,
                            newBlogFields.newImageUri
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
                    mimeTypes = arrayOf(
                        "image/png",
                        "image/jpg",
                        "image/jpeg"
                    )
                )
                .createIntent()
        )
    }

    private fun setBlogProperties(title: String?, body: String?, image: Uri?) {
        if (image != null) {
            binding?.let {
                Glide.with(this@CreateBlogFragment)
                    .load(image)
                    .into(it.blogImage)
            }
        }

        binding?.blogTitle?.setText(title)
        binding?.blogBody?.setText(body)
    }

    private fun publishNewBlog() {
        val imageUri = viewModel.viewState.value.blogFields.newImageUri
        if (imageUri == null) {
            stateChangeListener.displayErrorDialog(ERROR_MUST_SELECT_IMAGE)
            return
        }

        viewModel.createNewBlogPost(
            binding?.blogTitle?.text.toString(),
            binding?.blogBody?.text.toString(),
            imageUri
        )
        stateChangeListener.hideSoftKeyboard()
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(
            object : MenuProvider {
                override fun onPrepareMenu(menu: Menu) { /* no-op */ }

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.publish_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.publish -> {
                            val callback: AreYouSureCallback = object : AreYouSureCallback {
                                override fun proceed() {
                                    publishNewBlog()
                                }

                                override fun cancel() {
                                    // ignore
                                }
                            }
                            uiCommunicationListener.onUIMessageReceived(
                                UIMessage(
                                    getString(R.string.are_you_sure_publish),
                                    UIMessageType.AreYouSureDialog(callback)
                                )
                            )
                            return true
                        }
                    }
                    return true
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    override fun onPause() {
        super.onPause()
        viewModel.setNewBlogFields(
            binding?.blogTitle?.text.toString(),
            binding?.blogBody?.text.toString(),
            null
        )
    }
}
