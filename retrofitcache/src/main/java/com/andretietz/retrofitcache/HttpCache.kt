package com.andretietz.retrofitcache

import java.util.concurrent.TimeUnit

@Target(
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class HttpCache(
  val value: Int,
  val unit: TimeUnit,
  val override: Boolean = false
)
