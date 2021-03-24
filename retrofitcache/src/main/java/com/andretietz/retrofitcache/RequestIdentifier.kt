package com.andretietz.retrofitcache

import okhttp3.Request

internal object RequestIdentifier {
  @JvmStatic
  fun identify(request: Request) = request.url.hashCode() + 31 * request.method.hashCode()
}

