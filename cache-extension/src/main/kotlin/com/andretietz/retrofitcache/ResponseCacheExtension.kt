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
    require(cache.directory.isDirectory)
    val okHttpClient = retrofit.callFactory().let { callFactory ->
      check(callFactory is OkHttpClient) { "RetrofitCache only works with OkHttp as Http Client!" }
      callFactory.newBuilder()
        .addNetworkInterceptor(HttpCachingInterceptor(cacheControl))
        .cache(cache)
        .build()
    }
    return retrofit.newBuilder()
      .client(okHttpClient)
      .build()
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
