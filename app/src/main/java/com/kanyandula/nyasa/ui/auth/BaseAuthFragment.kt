package com.kanyandula.nyasa.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewbinding.ViewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseAuthFragment<T : ViewBinding>(private val bindingInflater: (layoutInflater: LayoutInflater) -> T
)  : Fragment(){

    val TAG: String = "AppDebug"

    // Bindings
    private var _binding: T? = null

    protected val binding get() = _binding



     val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = bindingInflater.invoke(inflater)
        return binding?.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancelActiveJobs()
    }

    private fun cancelActiveJobs(){
        viewModel.cancelActiveJobs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}















