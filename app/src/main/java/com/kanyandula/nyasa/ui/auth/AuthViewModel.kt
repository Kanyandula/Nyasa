package com.kanyandula.nyasa.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.codingwithmitch.openapi.util.GenericApiResponse
import com.kanyandula.nyasa.api.auth.network_responses.LoginResponse
import com.kanyandula.nyasa.api.auth.network_responses.RegistrationResponse
import com.kanyandula.nyasa.repository.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltViewModel
class AuthViewModel
@Inject
constructor(
   val authRepository: AuthRepository
): ViewModel(){

   fun testLogin(): LiveData<GenericApiResponse<LoginResponse>> {
      return authRepository.testLoginRequest(
         "mitchelltabian1234@gmail.com",
         "codingwithmitch1"
      )
   }

   fun testRegister(): LiveData<GenericApiResponse<RegistrationResponse>> {
      return authRepository.testRegistrationRequest(
         "mitchelltabian1234@gmail.com",
         "mitchelltabian1234",
         "codingwithmitch1",
         "codingwithmitch1"
      )
   }
}