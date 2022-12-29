package com.kanyandula.nyasa.ui.main.blog

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.FragmentUpdateBlogBinding
import com.kanyandula.nyasa.ui.*
import com.kanyandula.nyasa.ui.main.blog.state.BlogStateEvent
import com.kanyandula.nyasa.ui.main.blog.viewmodel.getUpdatedBlogUri
import com.kanyandula.nyasa.ui.main.blog.viewmodel.onBlogPostUpdateSuccess
import com.kanyandula.nyasa.ui.main.blog.viewmodel.setUpdatedBlogFields
import com.kanyandula.nyasa.util.ErrorHandling
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateBlogFragment : BaseBlogFragment<FragmentUpdateBlogBinding>(FragmentUpdateBlogBinding::inflate){


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

                    showErrorDialog(ErrorHandling.ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }

            }

        }


    fun showErrorDialog(errorMessage: String){
        stateChangeListener.onDataStateChange(
            DataState(
                Event(StateError(Response(errorMessage, ResponseType.Dialog()))),
                Loading(isLoading = false),
                Data(Event.dataEvent(null), null)
            )
        )
    }

    private fun showImageSelectionError(){
        stateChangeListener.onDataStateChange(
            DataState(
                Event(StateError(
                    Response(
                        "Something went wrong with the image.",
                        ResponseType.Dialog()
                    )
                )),
                Loading(isLoading = false),
                Data(Event.dataEvent(null), null)
            )
        )
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        subscribeObservers()
            binding?.imageContainer?.setOnClickListener {
                    pickGalleryImage()

            }

    }


    private fun pickGalleryImage() {
        galleryLauncher.launch(

            ImagePicker.with(requireActivity())
                .crop()
                .galleryOnly()
                .maxResultSize(1130,561)
                .setOutputFormat(Bitmap.CompressFormat.WEBP)
                .cropFreeStyle()
                .galleryMimeTypes( // no gif images at all
                    mimeTypes = arrayOf(
                        "image/png",
                        "image/jpg",
                        "image/jpeg"
                    )
                )
                .createIntent()
        )
    }


    fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState !=null){
                stateChangeListener.onDataStateChange(dataState)
                dataState.data?.let{ data ->
                    data.data?.getContentIfNotHandled()?.let{ viewState ->

                        // if this is not null, the blogpost was updated
                        viewState.viewBlogFields.blogPost?.let{ blogPost ->
                            viewModel.onBlogPostUpdateSuccess(blogPost).let {
                                findNavController().popBackStack()
                            }
                        }
                    }
                }

            }

        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.updatedBlogFields.let{ updatedBlogFields ->
                setBlogProperties(
                    updatedBlogFields.updatedBlogTitle,
                    updatedBlogFields.updatedBlogBody,
                    updatedBlogFields.updatedImageUri
                )
            }
        })
    }

    fun setBlogProperties(title: String?, body: String?, image: Uri?){

        binding?.let {
            Glide.with(this@UpdateBlogFragment)
                .load(image)
                .into(it.blogImage)
        }

        binding?.blogTitle
            ?.setText(title)
        binding?.blogBody?.setText(body)
    }




    private fun saveChanges(){
        var multipartBody: MultipartBody.Part? = null
        viewModel.getUpdatedBlogUri()?.let{ imageUri ->
            imageUri.path?.let{filePath ->
                val imageFile = File(filePath)
                Log.d(TAG, "UpdateBlogFragment, imageFile: file: ${imageFile}")
                if(imageFile.exists()){
                    val requestBody =
                        imageFile
                            .asRequestBody("image/*".toMediaTypeOrNull())
                    // name = field name in serializer
                    // filename = name of the image file
                    // requestBody = file with file type information
                    multipartBody = MultipartBody.Part.createFormData(
                        "image",
                        imageFile.name,
                        requestBody
                    )
                }
            }
        }

            viewModel.setStateEvent(
                BlogStateEvent.UpdateBlogPostEvent(
                    binding?.blogTitle
                        ?.text.toString(),
                    binding?.blogBody?.text.toString(),
                    multipartBody
                )
            )
            stateChangeListener.hideSoftKeyboard()

    }






    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.update_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item

                when(menuItem.itemId){
                    R.id.save -> {
                        saveChanges()
                        return true
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }




    // To retain the fields content when the user rotates the screen before saving the changes
    override fun onPause() {
        super.onPause()
        viewModel.setUpdatedBlogFields(
            uri = null,
            title = binding?.blogTitle?.text.toString(),
            body =  binding?.blogBody?.text.toString()
        )
    }

}