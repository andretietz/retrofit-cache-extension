package com.andretietz.retrofitcache

import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object ResponseCacheExtension {

  @JvmStatic
  @JvmOverloads
  fun setup(
    retrofit: Retrofit,
    cache: Cache,
    cacheControl: (annotation: ResponseCache) -> CacheControl = {
      CacheControl.Builder()
        .maxAge(it.value, it.unit)
        .build()
    }
  ): Retrofit {
    if (!cache.directory.isDirectory)
      throw IllegalArgumentException("The Cache must have a directory set!")
    val annotationRegister = mutableMapOf<Int, ResponseCache>()
    val okHttpClient = retrofit.callFactory().let { callFactory ->
      if (callFactory !is OkHttpClient) {
        throw IllegalStateException("RetrofitCache only works with OkHttp as Http Client!")
      } else {
        callFactory.newBuilder()
          .addNetworkInterceptor(HttpCachingInterceptor(annotationRegister, cacheControl))
          .cache(cache)
          .build()
      }
    }
    val builder = Retrofit.Builder()
      .client(okHttpClient)
      .baseUrl(retrofit.baseUrl())
    builder.callAdapterFactories()
    retrofit.converterFactories().forEach { builder.addConverterFactory(it) }
    retrofit.callbackExecutor()?.let { builder.callbackExecutor(it) }
    builder.addCallAdapterFactory(HttpCacheCallAdapterFactory(annotationRegister))
    retrofit.callAdapterFactories()
      // figure out how to remove default adapters!
      .forEach { builder.addCallAdapterFactory(it) }
    return builder.build()
  }
}

fun Retrofit.responseCache(
  cache: Cache,
  cacheControl: (annotation: ResponseCache) -> CacheControl = {
    CacheControl.Builder()
      .maxAge(it.value, it.unit)
      .build()
  }
) = ResponseCacheExtension.setup(this, cache, cacheControl)
