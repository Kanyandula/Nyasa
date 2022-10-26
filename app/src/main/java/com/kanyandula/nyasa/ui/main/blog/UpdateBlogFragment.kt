package com.kanyandula.nyasa.ui.main.blog

import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.FragmentUpdateBlogBinding
import com.kanyandula.nyasa.ui.main.blog.state.BlogStateEvent
import com.kanyandula.nyasa.ui.main.blog.viewmodel.onBlogPostUpdateSuccess
import com.kanyandula.nyasa.ui.main.blog.viewmodel.setUpdatedBlogFields

import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MultipartBody

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateBlogFragment : BaseBlogFragment<FragmentUpdateBlogBinding>(FragmentUpdateBlogBinding::inflate){



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        subscribeObservers()
    }


    fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
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
//        binding?.let {
//            requestManager
//                .load(image)
//                .into(it.blogImage)
//        }
        binding?.blogTitle
            ?.setText(title)
        binding?.blogBody?.setText(body)
    }

    private fun saveChanges(){
        var multipartBody: MultipartBody.Part? = null
        viewModel.setStateEvent(
            BlogStateEvent.UpdateBlogPostEvent(
                binding?.blogTitle?.text.toString(),
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