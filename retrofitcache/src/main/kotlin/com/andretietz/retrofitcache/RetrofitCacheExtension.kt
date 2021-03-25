package com.andretietz.retrofitcache

import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object RetrofitCacheExtension {

  @JvmStatic
  fun setup(
    retrofit: Retrofit,
    cache: Cache,
    cacheControl: (annotation: HttpCache) -> CacheControl = {
      CacheControl.Builder()
        .maxAge(it.value, it.unit)
        .build()
    }): Retrofit {
    if (!cache.directory.isDirectory)
      throw IllegalArgumentException("The Cache must have a directory set!")
    val annotationRegister = mutableMapOf<Int, HttpCache>()
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

fun Retrofit.cacheExtension(
  cache: Cache,
  cacheControl: (annotation: HttpCache) -> CacheControl = {
    CacheControl.Builder()
      .maxAge(it.value, it.unit)
      .build()
  }
) = RetrofitCacheExtension.setup(this, cache, cacheControl)
