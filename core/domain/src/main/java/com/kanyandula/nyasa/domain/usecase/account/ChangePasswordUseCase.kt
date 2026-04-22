package com.kanyandula.nyasa.domain.usecase.account

import com.kanyandula.nyasa.domain.repository.AccountRepository
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChangePasswordUseCase
@Inject
constructor(private val accountRepository: AccountRepository) {
    operator fun invoke(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): Flow<Resource<String>> =
        accountRepository.updatePassword(currentPassword, newPassword, confirmNewPassword)
}
