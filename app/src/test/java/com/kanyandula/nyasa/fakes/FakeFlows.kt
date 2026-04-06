package com.kanyandula.nyasa.fakes

import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> fakeResourceFlow(result: () -> Resource<T>): Flow<Resource<T>> = flow {
    emit(Resource.Loading())
    emit(result())
}
