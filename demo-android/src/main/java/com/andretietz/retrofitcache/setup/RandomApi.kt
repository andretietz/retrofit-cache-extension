package com.andretietz.retrofitcache.setup

import com.andretietz.retrofitcache.ResponseCache
import retrofit2.Response
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface RandomApi {
  @GET("api/v1.0/random")
  @ResponseCache(20, unit = TimeUnit.SECONDS)
  suspend fun randomNumber(): Response<List<Int>>
}
