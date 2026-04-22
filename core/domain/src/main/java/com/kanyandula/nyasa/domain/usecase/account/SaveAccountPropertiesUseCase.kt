package com.kanyandula.nyasa.domain.usecase.account

import com.kanyandula.nyasa.domain.repository.AccountRepository
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveAccountPropertiesUseCase
@Inject
constructor(private val accountRepository: AccountRepository) {
    operator fun invoke(accountProperties: AccountProperties): Flow<Resource<String>> =
        accountRepository.saveAccountProperties(accountProperties)
}
