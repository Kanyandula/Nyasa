package com.kanyandula.nyasa.domain.usecase.auth

import com.kanyandula.nyasa.domain.repository.AuthRepository
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase
@Inject
constructor(private val authRepository: AuthRepository) {
    operator fun invoke(email: String, password: String): Flow<Resource<AuthToken>> =
        authRepository.attemptLogin(email, password)
}
