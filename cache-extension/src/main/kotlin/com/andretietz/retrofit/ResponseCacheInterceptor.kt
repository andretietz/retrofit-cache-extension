package com.andretietz.retrofit

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Invocation

class ResponseCacheInterceptor(
  private val cacheControl: (annotation: ResponseCache) -> CacheControl = {
    CacheControl.Builder()
      .maxAge(it.value, it.unit)
      .build()
  }
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val response = chain.proceed(request)
    if (request.method == "GET") {
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
    }
    return response
  }

  private val registration: MutableMap<Int, ResponseCache> = mutableMapOf()

  private fun findAnnotation(
    request: Request
  ): ResponseCache? {
    val key = request.url.hashCode()
    return registration[key] ?: request.tag(Invocation::class.java)
      ?.method()
      ?.annotations
      ?.filterIsInstance<ResponseCache>()
      ?.firstOrNull()
      ?.also { registration[key] = it }
  }

  private fun containsCachingInformation(response: Response): Boolean =
    response.headers.names().contains(CACHE_CONTROL)

  companion object {
    private const val CACHE_CONTROL = "Cache-Control"
    private const val CACHE_PRAGMA = "Pragma"
  }
}
