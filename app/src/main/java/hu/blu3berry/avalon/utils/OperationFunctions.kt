package hu.blu3berry.avalon.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers


fun <A> performGetOperation(networkCall: suspend () -> Resource<A>): LiveData<Resource<A>> =
    liveData(Dispatchers.IO) {
        emit(Resource.loading())

        val responseStatus = networkCall.invoke()
        if (responseStatus.status == Status.SUCCESS) {
            emit(Resource.success(responseStatus.data!!))

        } else if (responseStatus.status == Status.ERROR) {
            emit(Resource.error(responseStatus.message!!))
        }
    }

fun <A> performPostOperation(
    networkCall: suspend () -> Resource<A>,
) : LiveData<Resource<A>> =
    liveData(Dispatchers.IO) {
        emit(Resource.loading())

        val responseStatus = networkCall.invoke()
        if (responseStatus.status == Status.SUCCESS) {
            emit(responseStatus)
        } else if (responseStatus.status == Status.ERROR) {
            emit(Resource.error(responseStatus.message!!))
        }
    }


