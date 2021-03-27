package com.andretietz.retrofitcache

import com.andretietz.retrofitcache.RetrofitCacheExtensionTest.TestApi.Companion.RESPONSE_GET_INT
import com.andretietz.retrofitcache.RetrofitCacheExtensionTest.TestApi.Companion.RESPONSE_GET_INT_CACHE
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private const val CACHE_SIZE = 50L * 1024L * 1024L

@ExperimentalCoroutinesApi
class RetrofitCacheExtensionTest {

  @JvmField
  @Rule
  val folder: TemporaryFolder = TemporaryFolder()

  private val server = MockWebServer()
  private lateinit var retrofit: Retrofit
  private lateinit var cache: Cache

  @Before
  fun setup() {
    server.start()
    cache = Cache(folder.newFolder("http_cache"), CACHE_SIZE)
    retrofit = Retrofit.Builder()
      .baseUrl(server.url("/"))
      .addConverterFactory(MoshiConverterFactory.create())
      .build()
      .responseCache(cache)
  }

  @After
  fun tearDown() {
    server.shutdown()
  }

  @Test
  fun `check if CallAdapter is in place`() {
    assertThat(retrofit.callAdapterFactories()[0])
      .isInstanceOf(HttpCacheCallAdapterFactory::class.java)
  }

  @Test
  fun `check if Interceptor is in place`() {
    assertThat(retrofit.callFactory()).isInstanceOf(OkHttpClient::class.java)
    val okHttpClient = retrofit.callFactory() as OkHttpClient
    val interceptor = okHttpClient.networkInterceptors.find { it is HttpCachingInterceptor }
    assertThat(interceptor).isNotNull
  }

  @Test
  fun `check if non-annotated Response doesn't contain header`() {
    runBlocking {
      val api = retrofit.create(TestApi::class.java)
      server.enqueue(RESPONSE_GET_INT)

      val response = api.getInt()

      assertThat(response.body()).isEqualTo(1234)
      assertThat(response.headers().map { pair -> pair.first }).doesNotContain("Cache-Control")
    }
  }

  @Test
  fun `check if annotated Response contains header`() {
    runBlocking {
      val api = retrofit.create(TestApi::class.java)
      server.enqueue(RESPONSE_GET_INT)

      val response = api.getCachedInt()

      assertThat(response.body()).isEqualTo(1234)
      assertThat(response.headers().map { pair -> pair.first }).contains("Cache-Control")
    }
  }

  @Test
  fun `checking hitcounts on annotated and non-annotated requests`() {
    runBlocking {
      val api = retrofit.create(TestApi::class.java)

      server.enqueue(RESPONSE_GET_INT)
      var response = api.getCachedInt()
      assertThat(response.body()).isEqualTo(1234)
      assertThat(cache.requestCount()).isEqualTo(1)
      assertThat(cache.networkCount()).isEqualTo(1)
      assertThat(cache.hitCount()).isEqualTo(0)

      server.enqueue(RESPONSE_GET_INT)
      response = api.getInt()
      assertThat(response.body()).isEqualTo(1234)
      assertThat(cache.requestCount()).isEqualTo(2)
      assertThat(cache.networkCount()).isEqualTo(2)
      assertThat(cache.hitCount()).isEqualTo(0)


      response = api.getCachedInt()
      assertThat(response.body()).isEqualTo(1234)
      assertThat(cache.requestCount()).isEqualTo(3)
      assertThat(cache.networkCount()).isEqualTo(2)
      assertThat(cache.hitCount()).isEqualTo(1)

      server.enqueue(RESPONSE_GET_INT)
      response = api.getInt()
      assertThat(response.body()).isEqualTo(1234)
      assertThat(cache.requestCount()).isEqualTo(4)
      assertThat(cache.networkCount()).isEqualTo(3)
      assertThat(cache.hitCount()).isEqualTo(1)


      response = api.getCachedInt()
      assertThat(response.body()).isEqualTo(1234)
      assertThat(cache.requestCount()).isEqualTo(5)
      assertThat(cache.networkCount()).isEqualTo(3)
      assertThat(cache.hitCount()).isEqualTo(2)
    }
  }

  @Test
  fun `checking hitcounts on timeouted cache`() {
    runBlocking {
      val api = retrofit.create(TestApi::class.java)
      server.enqueue(RESPONSE_GET_INT)
      server.enqueue(RESPONSE_GET_INT)

      api.getShortCachedInt()
      assertThat(cache.requestCount()).isEqualTo(1)
      assertThat(cache.networkCount()).isEqualTo(1)
      assertThat(cache.hitCount()).isEqualTo(0)


      api.getShortCachedInt()
      assertThat(cache.requestCount()).isEqualTo(2)
      assertThat(cache.networkCount()).isEqualTo(1)
      assertThat(cache.hitCount()).isEqualTo(1)

      delay(1000) // let the cache become stale

      api.getShortCachedInt()
      assertThat(cache.requestCount()).isEqualTo(3)
      assertThat(cache.networkCount()).isEqualTo(2)
      assertThat(cache.hitCount()).isEqualTo(1)
    }
  }

  @Test
  fun `check if server-side caching has priority`() {
    runBlocking {
      val api = retrofit.create(TestApi::class.java)
      server.enqueue(RESPONSE_GET_INT_CACHE)

      val response = api.getCachedInt()
      assertThat(response.body()).isEqualTo(1234)
      assertThat(response.headers().map { pair -> pair.first }).contains("Cache-Control")
      assertThat(
        response.headers().first { it.first == "Cache-Control" }.second
      ).isEqualTo("max-age=987")
    }
  }

  @Test
  fun `if flag is set, server-side caching will be overridden`() {
    runBlocking {
      val api = retrofit.create(TestApi::class.java)
      server.enqueue(RESPONSE_GET_INT_CACHE)

      val response = api.getForceCachedInt()
      assertThat(response.body()).isEqualTo(1234)
      assertThat(response.headers().map { pair -> pair.first }).contains("Cache-Control")
      assertThat(
        response.headers().first { it.first == "Cache-Control" }.second
      ).isEqualTo("max-age=60")
    }
  }

  interface TestApi {

    @GET("cached")
    @ResponseCache(60)
    suspend fun getCachedInt(): Response<Int>

    @GET("force-cached")
    @ResponseCache(60, override = true)
    suspend fun getForceCachedInt(): Response<Int>

    @GET("non-cached")
    suspend fun getInt(): Response<Int>

    @GET("short-cached")
    @ResponseCache(1)
    suspend fun getShortCachedInt(): Response<Int>

    companion object {
      val RESPONSE_GET_INT = MockResponse().apply { setBody("1234") }
      val RESPONSE_GET_INT_CACHE = MockResponse().apply {
        setHeader("Cache-Control", "max-age=987")
        setBody("1234")
      }
    }
  }


}
