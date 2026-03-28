@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewbinding.ViewBinding
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.ui.DataStateChangeListener
import com.kanyandula.nyasa.ui.UICommunicationListener
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.ui.collectLoadingState
import com.kanyandula.nyasa.ui.collectUiEvents
import com.kanyandula.nyasa.ui.handleStandardUiEvent
import com.kanyandula.nyasa.ui.setupActionBarWithNavController

abstract class BaseCreateBlogFragment<T : ViewBinding>(
    private val bindingInflater: (layoutInflater: LayoutInflater) -> T
) : Fragment() {

    private var _binding: T? = null
    protected val binding get() = _binding

    val viewModel: CreateBlogViewModel by activityViewModels()

    val TAG: String = "AppDebug"

    lateinit var stateChangeListener: DataStateChangeListener
    lateinit var uiCommunicationListener: UICommunicationListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(R.id.createBlogFragment, activity as AppCompatActivity)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        collectLoadingState(viewModel.isLoading, stateChangeListener)
        collectUiEvents(viewModel.events) { handleUiEvent(it) }
    }

    protected open fun handleUiEvent(event: UiEvent) {
        handleStandardUiEvent(event, stateChangeListener)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement DataStateChangeListener")
        }

        try {
            uiCommunicationListener = context as UICommunicationListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement UICommunicationListener")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
