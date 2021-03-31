package com.andretietz.retrofitcache

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andretietz.retrofitcache.setup.CACHE_VALIDITY
import com.andretietz.retrofitcache.setup.RandomApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val api: RandomApi) : ViewModel() {


  private val _state = Channel<ViewState>(capacity = Channel.Factory.CONFLATED).apply {
    viewModelScope.launch { send(ViewState.InitialState) }
  }

  val state = _state.consumeAsFlow()

  suspend fun updateNumber() {
    withContext(Dispatchers.IO) {
      api.randomNumber().let { response ->
        val number = response.body()?.first()
        if (number != null) {
          val currentResponse = response.raw()
          val cachedResponse = response.raw().cacheResponse
          _state.send(
            ViewState.NumberUpdate(
              number,
              currentResponse.receivedResponseAtMillis == cachedResponse?.receivedResponseAtMillis,
              currentResponse.receivedResponseAtMillis + CACHE_VALIDITY - System.currentTimeMillis()
            )
          )
        } else {
          _state.send(ViewState.Error(IllegalStateException("No number received!")))
        }
      }

    }
  }

  sealed class ViewState {
    object InitialState : ViewState()
    data class NumberUpdate(val value: Int, val fromCache: Boolean, val validityMillis: Long) :
      ViewState()

    data class Error(val error: Throwable) : ViewState()
  }
}
