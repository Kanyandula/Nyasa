package com.kanyandula.nyasa.ui.main.create_blog



import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.ImagePicker.Companion.EXTRA_FILE_PATH
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.FragmentCreateBlogBinding
import com.kanyandula.nyasa.ui.*
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogStateEvent
import com.kanyandula.nyasa.util.Constants.Companion.GALLERY_REQUEST_CODE
import com.kanyandula.nyasa.util.ErrorHandling.Companion.ERROR_MUST_SELECT_IMAGE
import com.kanyandula.nyasa.util.ErrorHandling.Companion.ERROR_SOMETHING_WRONG_WITH_IMAGE
import com.kanyandula.nyasa.util.SuccessHandling.Companion.SUCCESS_BLOG_CREATED
import com.kanyandula.nyasa.util.UploadStreamRequestBody
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


@OptIn(ExperimentalCoroutinesApi::class)
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

                    showErrorDialog(ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }

            }

        }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        binding?.apply {

            blogImage.setOnClickListener {
                if (stateChangeListener.isStoragePermissionGranted()) {
                    pickFromGallery()
                }
            }

            updateTextview.setOnClickListener {
                if (stateChangeListener.isStoragePermissionGranted()) {
                    pickFromGallery()

                }


            }
        }

        subscribeObservers()
    }



    fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null){
                stateChangeListener.onDataStateChange(dataState)
                dataState.data?.let { data ->
                    data.response?.let { event ->
                        event.peekContent().let { response ->
                            response.message?.let { message ->
                                if (message.equals(SUCCESS_BLOG_CREATED)) {
                                    viewModel.clearNewBlogFields()
                                }
                            }
                        }
                    }
                }
            }

        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.blogFields.let{ newBlogFields ->
                setBlogProperties(
                    newBlogFields.newBlogTitle,
                    newBlogFields.newBlogBody,
                    newBlogFields.newImageUri
                )
            }
        })
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



    private fun pickFromGallery() {
        pickGalleryImage()

    }


    fun setBlogProperties(title: String?, body: String?, image: Uri?){

        if(image != null){

            binding?.let {
                Glide.with(this@CreateBlogFragment)
                    .load(image)
                    .into(it.blogImage)
            }
            binding?.let {
                Glide.with(this@CreateBlogFragment)
                    .load(image)
                    .into(it.blogImage)
            }
        }

        binding?.blogTitle?.setText(title)
        binding?.blogBody?.setText(body)
    }





    private fun publishNewBlog(){

        var multipartBody: MultipartBody.Part? = null
        viewModel.viewState.value?.blogFields?.newImageUri?.let{ imageUri ->

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

        multipartBody?.let {

            viewModel.setStateEvent(
                CreateBlogStateEvent.CreateNewBlogEvent(
                    binding?.blogTitle
                        ?.text.toString(),
                    binding?.blogBody?.text.toString(),
                    it
                )
            )

        }?: showErrorDialog(ERROR_MUST_SELECT_IMAGE)

        stateChangeListener.hideSoftKeyboard()
    }



    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.publish_menu, menu)

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId){
                    R.id.publish -> {
                        val callback: AreYouSureCallback = object: AreYouSureCallback {

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
                // Validate and handle the selected menu item
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }




    private fun showErrorDialog(errorMessage: String){
        stateChangeListener.onDataStateChange(
            DataState(
                Event(StateError(Response(errorMessage, ResponseType.Dialog()))),
                Loading(isLoading = false),
                Data(Event.dataEvent(null), null)
            )
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