package com.kanyandula.nyasa.ui.main.blog


import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.net.toUri
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.FragmentViewBlogBinding
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.ui.AreYouSureCallback
import com.kanyandula.nyasa.ui.UIMessage
import com.kanyandula.nyasa.ui.UIMessageType
import com.kanyandula.nyasa.ui.main.blog.state.BlogStateEvent.*
import com.kanyandula.nyasa.ui.main.blog.viewmodel.*
import com.kanyandula.nyasa.util.DateUtils
import com.kanyandula.nyasa.util.SuccessHandling.Companion.SUCCESS_BLOG_DELETED
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class ViewBlogFragment : BaseBlogFragment<FragmentViewBlogBinding>(FragmentViewBlogBinding::inflate){



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


    fun confirmDeleteRequest(){
        val callback: AreYouSureCallback = object: AreYouSureCallback {

            override fun proceed() {
                deleteBlogPost()
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

    fun deleteBlogPost(){
        viewModel.setStateEvent(
            DeleteBlogPostEvent()
        )
    }


    fun checkIsAuthorOfBlogPost(){
        viewModel.setIsAuthorOfBlogPost(false) // reset
        viewModel.setStateEvent(CheckAuthorOfBlogPost())
    }


    fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer{ dataState ->
            if (dataState !=null){
                stateChangeListener.onDataStateChange(dataState)

                dataState.data?.let { data ->
                    data.data?.getContentIfNotHandled()?.let { viewState ->
                        viewModel.setIsAuthorOfBlogPost(
                            viewState.viewBlogFields.isAuthorOfBlogPost
                        )
                    }
                    data.response?.peekContent()?.let{ response ->
                        if(response.message.equals(SUCCESS_BLOG_DELETED)){
                            viewModel.removeDeletedBlogPost()
                            findNavController().popBackStack()
                        }
                    }
                }
            }

        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.viewBlogFields.blogPost?.let{ blogPost ->
                setBlogProperties(blogPost)
            }

            if(viewState.viewBlogFields.isAuthorOfBlogPost){
                adaptViewToAuthorMode()
            }
        })
    }


    private fun adaptViewToAuthorMode() {
        activity?.invalidateOptionsMenu()
        binding?.deleteButton?.visibility = View.VISIBLE
    }

     fun setBlogProperties(blogPost: BlogPost){





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


      // val uri = Uri.parse(blogPost.slug)
       // val intent = Intent(Intent.ACTION_VIEW, uri)
//        text_view_creator.apply {
//            text = "https://nyasablog.com/blog/${blogPost.slug}/detail/"
//            setOnClickListener {
//                context.startActivity(intent)
//            }
//            paint.isUnderlineText = true
//        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                if(viewModel.isAuthorOfBlogPost()){
                    menuInflater.inflate(R.menu.edit_view_menu, menu)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item

                if(viewModel.isAuthorOfBlogPost()){
                    when(menuItem.itemId){
                        R.id.edit -> {
                            navUpdateBlogFragment()
                            return true
                        }
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


    private fun navUpdateBlogFragment(){
        try{
            // prep for next fragment
            viewModel.setUpdatedBlogFields(
                viewModel.getBlogPost().title,
                viewModel.getBlogPost().body,
                viewModel.getBlogPost().image.toUri()
            )
            findNavController().navigate(R.id.action_viewBlogFragment_to_updateBlogFragment)
        }catch (e: Exception){
            // send error report or something. These fields should never be null. Not possible
            Log.e(TAG, "Exception: ${e.message}")
        }
    }


}