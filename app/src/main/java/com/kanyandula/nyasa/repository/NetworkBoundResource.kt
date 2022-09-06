package com.kanyandula.nyasa.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.kanyandula.nyasa.ui.DataState
import com.kanyandula.nyasa.ui.Response
import com.kanyandula.nyasa.ui.ResponseType
import com.kanyandula.nyasa.util.*
import com.kanyandula.nyasa.util.Constants.Companion.NETWORK_TIMEOUT
import com.kanyandula.nyasa.util.Constants.Companion.TESTING_CACHE_DELAY
import com.kanyandula.nyasa.util.Constants.Companion.TESTING_NETWORK_DELAY
import com.kanyandula.nyasa.util.ErrorHandling.Companion.ERROR_CHECK_NETWORK_CONNECTION
import com.kanyandula.nyasa.util.ErrorHandling.Companion.ERROR_UNKNOWN
import com.kanyandula.nyasa.util.ErrorHandling.Companion.UNABLE_TODO_OPERATION_WO_INTERNET
import com.kanyandula.nyasa.util.ErrorHandling.Companion.UNABLE_TO_RESOLVE_HOST

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

@OptIn(InternalCoroutinesApi::class, kotlinx.coroutines.DelicateCoroutinesApi::class)
abstract class NetworkBoundResource<ResponseObject, ViewStateType>
    (
    isNetworkAvailable: Boolean, // is their a network connection?
    isNetworkRequest: Boolean // is this a network request?
) {

    private val TAG: String = "AppDebug"

    protected val result = MediatorLiveData<DataState<ViewStateType>>()
    protected lateinit var job: CompletableJob
    protected lateinit var coroutineScope: CoroutineScope

    init {
        setJob(initNewJob())
        setValue(DataState.loading(isLoading = true, cachedData = null))

        if(isNetworkRequest){
            if(isNetworkAvailable){
                coroutineScope.launch {

                    // simulate a network delay for testing
                    delay(TESTING_NETWORK_DELAY)

                    withContext(Main){

                        // make network call
                        val apiResponse = createCall()
                        result.addSource(apiResponse){ response ->
                            result.removeSource(apiResponse)

                            coroutineScope.launch {
                                handleNetworkCall(response)
                            }
                        }
                    }
                }

                GlobalScope.launch(IO){
                    delay(NETWORK_TIMEOUT)

                    if(!job.isCompleted){
                        Log.e(TAG, "NetworkBoundResource: JOB NETWORK TIMEOUT." )
                        job.cancel(CancellationException(ErrorHandling.UNABLE_TO_RESOLVE_HOST))
                    }
                }
            }
            else{
                onErrorReturn(ErrorHandling.UNABLE_TODO_OPERATION_WO_INTERNET, shouldUseDialog = true, shouldUseToast = false)
            }
        }
        else{
            coroutineScope.launch {
                delay(TESTING_CACHE_DELAY)
                // View data from cache only and return
                createCacheRequestAndReturn()
            }
        }
    }

    suspend fun handleNetworkCall(response: GenericApiResponse<ResponseObject>){

        when(response){
            is ApiSuccessResponse ->{
                handleApiSuccessResponse(response)
            }
            is ApiErrorResponse ->{
                Log.e(TAG, "NetworkBoundResource: ${response.errorMessage}")
                onErrorReturn(response.errorMessage, true, false)
            }
            is ApiEmptyResponse ->{
                Log.e(TAG, "NetworkBoundResource: Request returned NOTHING (HTTP 204).")
                onErrorReturn("HTTP 204. Returned NOTHING.", true, false)
            }
        }
    }

    fun onCompleteJob(dataState: DataState<ViewStateType>){
        GlobalScope.launch(Main) {
            job.complete()
            setValue(dataState)
        }
    }

    fun onErrorReturn(errorMessage: String?, shouldUseDialog: Boolean, shouldUseToast: Boolean){
        var msg = errorMessage
        var useDialog = shouldUseDialog
        var responseType: ResponseType = ResponseType.None()
        if(msg == null){
            msg = ERROR_UNKNOWN
        }
        else if(ErrorHandling.isNetworkError(msg)){
            msg = ERROR_CHECK_NETWORK_CONNECTION
            useDialog = false
        }
        if(shouldUseToast){
            responseType = ResponseType.Toast()
        }
        if(useDialog){
            responseType = ResponseType.Dialog()
        }

        onCompleteJob(DataState.error(Response(msg, responseType)))
    }

    fun setValue(dataState: DataState<ViewStateType>){
        result.value = dataState
    }

    @OptIn(InternalCoroutinesApi::class)
    private fun initNewJob(): Job{
        Log.d(TAG, "initNewJob: called.")
        job = Job() // create new job
        job.invokeOnCompletion(onCancelling = true, invokeImmediately = true, handler = object: CompletionHandler{
            override fun invoke(cause: Throwable?) {
                if(job.isCancelled){
                    Log.e(TAG, "NetworkBoundResource: Job has been cancelled.")
                    cause?.let{
                        onErrorReturn(it.message, false, true)
                    }?: onErrorReturn("Unknown error.", false, true)
                }
                else if(job.isCompleted){
                    Log.e(TAG, "NetworkBoundResource: Job has been completed.")
                    // Do nothing? Should be handled already
                }
            }
        })
        coroutineScope = CoroutineScope(IO + job)
        return job
    }

    fun asLiveData() = result as LiveData<DataState<ViewStateType>>

    abstract suspend fun createCacheRequestAndReturn()

    abstract suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<ResponseObject>)

    abstract fun createCall(): LiveData<GenericApiResponse<ResponseObject>>

    abstract fun setJob(job: Job)

}


















