package com.kanyandula.nyasa.ui.auth

import androidx.lifecycle.ViewModel
import com.kanyandula.nyasa.repository.auth.AuthRepository
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(
   val authRepository: AuthRepository
): ViewModel(){
}