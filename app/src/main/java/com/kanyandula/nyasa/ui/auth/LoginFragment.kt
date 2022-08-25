package com.kanyandula.nyasa.ui.auth


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.codingwithmitch.openapi.util.ApiEmptyResponse
import com.codingwithmitch.openapi.util.ApiErrorResponse
import com.codingwithmitch.openapi.util.ApiSuccessResponse
import com.kanyandula.nyasa.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@AndroidEntryPoint
class LoginFragment : Fragment() {

    val TAG: String = "AppDebug"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "LoginFragment: ${viewModel}")

        viewModel.testLogin().observe(viewLifecycleOwner, Observer { response ->

            when(response){
                is ApiSuccessResponse ->{
                    Log.d(TAG, "LOGIN RESPONSE: ${response.body}")
                }
                is ApiErrorResponse ->{
                    Log.d(TAG, "LOGIN RESPONSE: ${response.errorMessage}")
                }
                is ApiEmptyResponse ->{
                    Log.d(TAG, "LOGIN RESPONSE: Empty Response")
                }
            }
        })

    }

}