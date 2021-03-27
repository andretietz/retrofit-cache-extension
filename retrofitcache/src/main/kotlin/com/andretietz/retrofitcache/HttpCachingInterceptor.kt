package com.andretietz.retrofitcache

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Invocation

internal class HttpCachingInterceptor(
  private val cacheControl: (annotation: ResponseCache) -> CacheControl
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val response = chain.proceed(request)
    findAnnotation(request)?.let { cacheInfo ->
      val cacheControl: CacheControl = cacheControl(cacheInfo)
      if (containsCachingInformation(response) && !cacheInfo.override) {
        return response
      }
      return response.newBuilder()
        .removeHeader(CACHE_PRAGMA)
        .removeHeader(CACHE_CONTROL)
        .header(CACHE_CONTROL, cacheControl.toString())
        .build()
    }
    return response
  }

  private val registration: MutableMap<Int, ResponseCache> = mutableMapOf()

  private fun findAnnotation(
    request: Request
  ): ResponseCache? {
    val key = request.url.hashCode() + 31 * request.method.hashCode()
    return registration[key] ?: request.tag(Invocation::class.java)
      ?.method()
      ?.annotations
      ?.filterIsInstance<ResponseCache>()
      ?.firstOrNull()
      ?.also { registration[key] = it }
  }

  private fun containsCachingInformation(response: Response): Boolean {
    return response.headers.names().contains(CACHE_CONTROL)
  }


  companion object {
    const val CACHE_CONTROL = "Cache-Control"
    const val CACHE_PRAGMA = "Pragma"
  }
//
//  override fun intercept(chain: Interceptor.Chain, annotation: ResponseCache): Response {
//    val request = chain.request()
//    val response = chain.proceed(request)
//    val cacheControl: CacheControl = cacheControl(annotation)
//    if (containsCachingInformation(response) && !annotation.override) {
//      return response
//    }
//    return response.newBuilder()
//      .removeHeader(CACHE_PRAGMA)
//      .removeHeader(CACHE_CONTROL)
//      .header(CACHE_CONTROL, cacheControl.toString())
//      .build()
//  }
}

