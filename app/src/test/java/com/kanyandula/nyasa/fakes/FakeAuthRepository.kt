package com.kanyandula.nyasa.fakes

import com.kanyandula.nyasa.domain.repository.AuthRepository
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow

class FakeAuthRepository : AuthRepository {

    var loginResult: Resource<AuthToken> = Resource.Success(AuthToken(1, "fake-token"))
    var registrationResult: Resource<AuthToken> = Resource.Success(AuthToken(1, "fake-token"))
    var previousAuthResult: Resource<AuthToken?> = Resource.Success(null)

    var loginCallCount = 0
        private set
    var registerCallCount = 0
        private set
    var checkPreviousAuthCallCount = 0
        private set

    override fun attemptLogin(email: String, password: String): Flow<Resource<AuthToken>> {
        loginCallCount++
        return fakeResourceFlow { loginResult }
    }

    override fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Flow<Resource<AuthToken>> {
        registerCallCount++
        return fakeResourceFlow { registrationResult }
    }

    override fun checkPreviousAuthUser(): Flow<Resource<AuthToken?>> {
        checkPreviousAuthCallCount++
        return fakeResourceFlow { previousAuthResult }
    }
}
