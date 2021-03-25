package com.andretietz.retrofitcache

import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.Type

internal class HttpCacheCallAdapterFactory(
  private val registration: MutableMap<Int, HttpCache> = mutableMapOf()
) : CallAdapter.Factory() {

  private fun isAnnotated(annotations: Array<Annotation>): HttpCache? {
    for (annotation in annotations) {
      if (HttpCache::class == annotation.annotationClass) return annotation as HttpCache
    }
    return null
  }

  @Suppress("UNCHECKED_CAST")
  override fun get(
    returnType: Type,
    annotations: Array<Annotation>,
    retrofit: Retrofit
  ): CallAdapter<*, *>? {
    val annotation = isAnnotated(annotations)
    // getting all calladapters except this one
    val callAdapterFactories =
      retrofit.callAdapterFactories().filterNot { it is HttpCacheCallAdapterFactory }
    // iterating through them in order to find the one which would be used normally
    for (i in callAdapterFactories.indices) {
      // try getting the calladapter which would be used normally
      val adapter = callAdapterFactories[i].get(returnType, annotations, retrofit)
      if (adapter != null) {
        // adapter for return type found
        if (annotation != null) {
          // if the reques was annotated
          return ProxyCallAdapter(
            adapter as CallAdapter<Any, Any>,
            registration,
            annotation
          )
        }
        return adapter
      }
    }
    return null
  }
}
