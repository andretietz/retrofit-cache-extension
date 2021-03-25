package com.andretietz.retrofitcache

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response

internal class HttpCachingInterceptor(
  private val registration: Map<Int, HttpCache>,
  private val cacheControl: (annotation: HttpCache) -> CacheControl
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val response: Response = chain.proceed(request)
    val cacheInfo = registration[RequestIdentifier.identify(request)]
    if (cacheInfo != null) {
      val cacheControl: CacheControl = cacheControl(cacheInfo)
      if (containsCachingInformation(response) && !cacheInfo.override) {
        return response
      }
      return response.newBuilder()
        .removeHeader("Pragma")
        .removeHeader(CACHE_CONTROL)
        .header(CACHE_CONTROL, cacheControl.toString())
        .build()
    }
    return response
  }

  private fun containsCachingInformation(response: Response): Boolean {
    return response.headers.names().contains(CACHE_CONTROL)
  }

  companion object {
    const val CACHE_CONTROL = "Cache-Control"
  }
}

