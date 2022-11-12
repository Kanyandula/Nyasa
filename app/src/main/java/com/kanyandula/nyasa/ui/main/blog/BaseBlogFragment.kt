package com.kanyandula.nyasa.ui.main.blog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.ui.DataStateChangeListener
import com.kanyandula.nyasa.ui.UICommunicationListener
import com.kanyandula.nyasa.ui.main.blog.viewmodel.BlogViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseBlogFragment <T : ViewBinding>(private val bindingInflater: (layoutInflater:LayoutInflater) -> T
)  : Fragment(){

    val TAG: String = "AppDebug"

    // Bindings
    private var _binding: T? = null

    protected val binding get() = _binding


    @Inject
    lateinit var requestManager: RequestManager

    lateinit var uiCommunicationListener: UICommunicationListener


    val viewModel: BlogViewModel by activityViewModels()

    lateinit var stateChangeListener: DataStateChangeListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = bindingInflater.invoke(inflater)
        return binding?.root



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        setupActionBarWithNavController(R.id.blogFragment, activity as AppCompatActivity)



        cancelActiveJobs()
    }

    fun cancelActiveJobs(){
       viewModel.cancelActiveJobs()
    }



    /*
          @fragmentId is id of fragment from graph to be EXCLUDED from action back bar nav
        */
    fun setupActionBarWithNavController(fragmentId: Int, activity: AppCompatActivity){
        val appBarConfiguration = AppBarConfiguration(setOf(fragmentId))
        NavigationUI.setupActionBarWithNavController(
            activity,
            findNavController(),
            appBarConfiguration
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            stateChangeListener = context as DataStateChangeListener
        }catch(e: ClassCastException){
            Log.e(TAG, "$context must implement DataStateChangeListener" )
        }

        try{
            uiCommunicationListener = context as UICommunicationListener
        }catch(e: ClassCastException){
            Log.e(TAG, "$context must implement UICommunicationListener" )
        }

        try {
            requestManager = context as RequestManager
        }catch (e: ClassCastException){
            Log.e(TAG, "$context must implement RequestManager" )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}