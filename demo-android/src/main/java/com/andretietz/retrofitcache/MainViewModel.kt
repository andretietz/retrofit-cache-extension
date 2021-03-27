package com.andretietz.retrofitcache

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andretietz.retrofitcache.setup.RandomApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val api: RandomApi) : ViewModel() {


  private val pState = Channel<ViewState>().apply {
    viewModelScope.launch { send(ViewState.InitialState) }
  }
  val state: ReceiveChannel<ViewState> = pState

  suspend fun updateNumber() {
    withContext(Dispatchers.IO) {
      api.randomNumber().let { response ->
        val isFromCache = response.raw().cacheResponse != null
        val number = response.body()?.first()
        if (number != null) {
          pState.send(ViewState.NumberUpdate(number, isFromCache))
        } else {
          pState.send(ViewState.Error(IllegalStateException("No number received!")))
        }
      }

    }
  }

  sealed class ViewState {
    object InitialState : ViewState()
    data class NumberUpdate(val value: Int, val fromCache: Boolean) : ViewState()
    data class Error(val error: Throwable) : ViewState()
  }
}
