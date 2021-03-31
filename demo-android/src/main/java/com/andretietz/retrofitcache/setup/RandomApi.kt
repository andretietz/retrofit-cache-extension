package com.andretietz.retrofitcache.setup

import com.andretietz.retrofit.ResponseCache
import retrofit2.Response
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

const val CACHE_VALIDITY = 20000

interface RandomApi {
  @GET("api/v1.0/random")
  @ResponseCache(CACHE_VALIDITY, unit = TimeUnit.MILLISECONDS)
  suspend fun randomNumber(): Response<List<Int>>
}
