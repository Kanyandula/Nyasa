package com.kanyandula.nyasa.ui.components

import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class OptimisticActionTest {

    @Test
    fun `success emits predicted then server state`() = runTest {
        val emissions = mutableListOf<Int>()

        optimisticAction(
            currentState = 0,
            predict = { it + 1 },
            action = { Resource.Success(10) },
            rollback = { it },
            emit = { emissions.add(it) },
            onError = {}
        )

        assertEquals(listOf(1, 10), emissions)
    }

    @Test
    fun `error emits predicted then rollback`() = runTest {
        val emissions = mutableListOf<Int>()
        var capturedError: AppError? = null

        optimisticAction(
            currentState = 0,
            predict = { it + 1 },
            action = { Resource.Error(AppError.Server(500)) },
            rollback = { it },
            emit = { emissions.add(it) },
            onError = { capturedError = it }
        )

        assertEquals(listOf(1, 0), emissions)
        assertEquals(AppError.Server(500), capturedError)
    }

    @Test
    fun `rollback restores original state not predicted`() = runTest {
        val emissions = mutableListOf<String>()

        optimisticAction(
            currentState = "original",
            predict = { "predicted" },
            action = { Resource.Error(AppError.Offline) },
            rollback = { it },
            emit = { emissions.add(it) },
            onError = {}
        )

        assertEquals(listOf("predicted", "original"), emissions)
    }

    @Test
    fun `offline error triggers rollback and onError`() = runTest {
        var errorReceived = false

        optimisticAction(
            currentState = true,
            predict = { false },
            action = { Resource.Error(AppError.Offline) },
            rollback = { it },
            emit = {},
            onError = { errorReceived = true }
        )

        assertEquals(true, errorReceived)
    }
}
