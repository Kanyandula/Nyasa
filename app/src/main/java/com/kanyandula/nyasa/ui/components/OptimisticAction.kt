package com.kanyandula.nyasa.ui.components

import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.Resource

suspend fun <S> optimisticAction(
    currentState: S,
    predict: (S) -> S,
    action: suspend () -> Resource<S>,
    rollback: (S) -> S,
    emit: suspend (S) -> Unit,
    onError: suspend (AppError) -> Unit
) {
    emit(predict(currentState))
    when (val result = action()) {
        is Resource.Success -> emit(result.data)
        is Resource.Error -> {
            emit(rollback(currentState))
            onError(result.error)
        }
        is Resource.Loading -> Unit
    }
}
