package com.kanyandula.nyasa.ui.main.blog

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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.FragmentUpdateBlogBinding
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.ui.main.blog.state.BlogNavigationEvent
import com.kanyandula.nyasa.util.ErrorHandling
import kotlinx.coroutines.launch

class UpdateBlogFragment : BaseBlogFragment<FragmentUpdateBlogBinding>(FragmentUpdateBlogBinding::inflate) {

    private val args: UpdateBlogFragmentArgs by navArgs()

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (it.data?.hasExtra(ImagePicker.EXTRA_FILE_PATH)!!) {
                    val uri = it.data?.data!!
                    viewModel.setUpdatedBlogFields(
                        title = null,
                        body = null,
                        uri = uri
                    )
                } else {
                    stateChangeListener.displayErrorDialog(ErrorHandling.ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        subscribeObservers()
        binding?.imageContainer?.setOnClickListener {
            pickGalleryImage()
        }
    }

    override fun handleUiEvent(event: UiEvent) {
        when (event) {
            is BlogNavigationEvent.BlogUpdateSuccess -> {
                findNavController().popBackStack()
            }
            else -> super.handleUiEvent(event)
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

    private fun subscribeObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateBlogState.collect { state ->
                    setBlogProperties(
                        state.updatedBlogTitle,
                        state.updatedBlogBody,
                        state.updatedImageUri
                    )
                }
            }
        }
    }

    fun setBlogProperties(title: String?, body: String?, image: Uri?) {
        binding?.let {
            Glide.with(this@UpdateBlogFragment)
                .load(image)
                .into(it.blogImage)
        }

        binding?.blogTitle?.let { if (it.text.toString() != title.orEmpty()) it.setText(title) }
        binding?.blogBody?.let { if (it.text.toString() != body.orEmpty()) it.setText(body) }
    }

    private fun saveChanges() {
        viewModel.updateBlogPost(
            slug = args.blogSlug,
            title = binding?.blogTitle?.text.toString(),
            body = binding?.blogBody?.text.toString(),
            imageUri = viewModel.getUpdatedBlogUri()
        )
        stateChangeListener.hideSoftKeyboard()
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(
            object : MenuProvider {
                override fun onPrepareMenu(menu: Menu) { /* no-op */ }

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.update_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.save -> {
                            saveChanges()
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
        viewModel.setUpdatedBlogFields(
            uri = null,
            title = binding?.blogTitle?.text.toString(),
            body = binding?.blogBody?.text.toString()
        )
    }
}
