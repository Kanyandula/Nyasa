package com.kanyandula.nyasa.ui.auth

import androidx.lifecycle.LiveData
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.repository.auth.AuthRepository
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.DataState
import com.kanyandula.nyasa.ui.auth.state.AuthStateEvent
import com.kanyandula.nyasa.ui.auth.state.AuthStateEvent.*
import com.kanyandula.nyasa.ui.auth.state.AuthViewState
import com.kanyandula.nyasa.ui.auth.state.LoginFields
import com.kanyandula.nyasa.ui.auth.state.RegistrationFields
import com.kanyandula.nyasa.util.AbsentLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltViewModel
class AuthViewModel
@Inject
constructor(
   val authRepository: AuthRepository
): BaseViewModel<AuthStateEvent, AuthViewState>(){


   override fun handleStateEvent(stateEvent: AuthStateEvent): LiveData<DataState<AuthViewState>> {
      when(stateEvent){

         is LoginAttemptEvent -> {
            return authRepository.attemptLogin(
               stateEvent.email,
               stateEvent.password
            )
         }

         is RegisterAttemptEvent -> {
            return authRepository.attemptRegistration(
               stateEvent.email,
               stateEvent.username,
               stateEvent.password,
               stateEvent.confirm_password
            )
         }

         is CheckPreviousAuthEvent -> {
            return authRepository.checkPreviousAuthUser()
         }




      }
   }



   override fun initNewViewState(): AuthViewState {
      return AuthViewState()
   }

   fun setRegistrationFields(registrationFields: RegistrationFields){
      val update = getCurrentViewStateOrNew()
      if(update.registrationFields == registrationFields){
         return
      }
      update.registrationFields = registrationFields
      _viewState.value = update
   }

   fun setLoginFields(loginFields: LoginFields){
      val update = getCurrentViewStateOrNew()
      if(update.loginFields == loginFields){
         return
      }
      update.loginFields = loginFields
      _viewState.value = update
   }

   fun setAuthToken(authToken: AuthToken){
      val update = getCurrentViewStateOrNew()
      if(update.authToken == authToken){
         return
      }
      update.authToken = authToken
      _viewState.value = update
   }

   fun cancelActiveJobs(){
      authRepository.cancelActiveJobs()
   }


   override fun onCleared() {
      super.onCleared()
      cancelActiveJobs()
   }
}