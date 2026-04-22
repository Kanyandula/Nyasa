package com.kanyandula.nyasa.domain.usecase.auth

import com.kanyandula.nyasa.domain.repository.AuthRepository
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckPreviousAuthUseCase
@Inject
constructor(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<Resource<AuthToken?>> =
        authRepository.checkPreviousAuthUser()
}
