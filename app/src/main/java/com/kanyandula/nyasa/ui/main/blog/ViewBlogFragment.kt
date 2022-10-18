package com.kanyandula.nyasa.ui.main.blog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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

@OptIn(ExperimentalCoroutinesApi::class)
class ViewBlogFragment : BaseBlogFragment<FragmentViewBlogBinding>(FragmentViewBlogBinding::inflate){


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
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
        binding?.deleteButton?.visibility
    }

    private fun setBlogProperties(blogPost: BlogPost){
        binding?.let {
            requestManager
                .load(blogPost.image)
                .into(it.blogImage)
        }
        binding?.blogTitle?.setText(blogPost.title)
        binding?.blogAuthor?.setText(blogPost.username)
        binding?.blogUpdateDate?.setText(DateUtils.convertLongToStringDate(blogPost.date_updated))
        binding?.blogBody?.setText(blogPost.body)
        val uri = Uri.parse(blogPost.slug)
        val intent = Intent(Intent.ACTION_VIEW, uri)
//        text_view_creator.apply {
//            text = "https://nyasablog.com/blog/${blogPost.slug}/detail/"
//            setOnClickListener {
//                context.startActivity(intent)
//            }
//            paint.isUnderlineText = true
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        if(viewModel.isAuthorOfBlogPost()){
            inflater.inflate(R.menu.edit_view_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(viewModel.isAuthorOfBlogPost()){
            when(item.itemId){
                R.id.edit -> {
                    navUpdateBlogFragment()
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
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