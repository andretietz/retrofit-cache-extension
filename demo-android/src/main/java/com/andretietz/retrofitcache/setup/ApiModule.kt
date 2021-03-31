package com.andretietz.retrofitcache.setup

import android.app.Application
import com.andretietz.retrofit.responseCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

  @Provides
  @Singleton
  fun provideApi(retrofit: Retrofit): RandomApi = retrofit.create(RandomApi::class.java)

  @Provides
  @Singleton
  fun provideRetrofit(application: Application): Retrofit {
    val loggingInterceptor = HttpLoggingInterceptor()
    loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    return Retrofit.Builder()
      .baseUrl("http://www.randomnumberapi.com/")
      .client(OkHttpClient.Builder()
        .addNetworkInterceptor(loggingInterceptor)
        .build()
      )
      .addConverterFactory(MoshiConverterFactory.create())
      .build()
      .responseCache(Cache(directory = application.cacheDir, 10 * 1024))
  }
}
