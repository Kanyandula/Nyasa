package com.kanyandula.nyasa.ui.main.account

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
import com.kanyandula.nyasa.R

import com.kanyandula.nyasa.ui.DataStateChangeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseAccountFragment <T : ViewBinding>(private val bindingInflater: (layoutInflater: LayoutInflater) -> T)  : Fragment(){

    val TAG: String = "AppDebug"

    // Bindings
    private var _binding: T? = null

    protected val binding get() = _binding

    val viewModel: AccountViewModel by activityViewModels()

    lateinit var stateChangeListener: DataStateChangeListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = bindingInflater.invoke(inflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(R.id.accountFragment, activity as AppCompatActivity)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)

        cancelActiveJobs()
    }

    fun cancelActiveJobs(){
        // When a fragment is destroyed make sure to cancel any on-going requests.
        // Note: If you wanted a particular request to continue even if the fragment was destroyed, you could write a
        //       special condition in the repository or something.
        viewModel.cancelActiveJobs()
    }



    /*
          @fragmentId is id of fragment from graph to be EXCLUDED from action back bar nav
        */
    private fun setupActionBarWithNavController(fragmentId: Int, activity: AppCompatActivity){
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


















