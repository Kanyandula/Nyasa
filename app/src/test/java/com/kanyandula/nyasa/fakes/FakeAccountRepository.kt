package com.kanyandula.nyasa.fakes

import com.kanyandula.nyasa.domain.repository.AccountRepository
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow

class FakeAccountRepository : AccountRepository {

    var accountPropertiesResult: Resource<AccountProperties> = Resource.Success(
        AccountProperties(pk = 1, email = "test@test.com", username = "testuser")
    )
    var saveResult: Resource<String> = Resource.Success("Update successful")
    var updatePasswordResult: Resource<String> = Resource.Success("Password changed")

    override fun getAccountProperties(): Flow<Resource<AccountProperties>> =
        fakeResourceFlow { accountPropertiesResult }

    override fun saveAccountProperties(
        accountProperties: AccountProperties
    ): Flow<Resource<String>> = fakeResourceFlow { saveResult }

    override fun updatePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): Flow<Resource<String>> = fakeResourceFlow { updatePasswordResult }
}
