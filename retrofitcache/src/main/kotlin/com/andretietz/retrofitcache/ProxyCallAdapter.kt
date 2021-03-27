package com.andretietz.retrofitcache

import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

internal class ProxyCallAdapter<RETURN_TYPE : Any>(
    private val adapter: CallAdapter<Any, RETURN_TYPE>,
    private val registration: MutableMap<Int, ResponseCache>,
    private val info: ResponseCache
) : CallAdapter<Any, RETURN_TYPE> {

  override fun responseType(): Type = adapter.responseType()

  override fun adapt(call: Call<Any>): RETURN_TYPE {
    registration[RequestIdentifier.identify(call.request())] = info
    return adapter.adapt(call)
  }
}

