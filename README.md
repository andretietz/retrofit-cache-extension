# Retrofit Cache Extension
[![Snapshot build](https://github.com/andretietz/retroauth/workflows/Snapshot%20build/badge.svg)](https://github.com/andretietz/retrofit-cache-extension/actions?query=workflow%3A%22Snapshot+build%22)

This libary depends on:
* [retrofit](https://github.com/square/retrofit) 2.9.0
* [okhttp](https://github.com/square/okhttp) 4.9.1
 
This extension adds a `ResponseCache` annotation to the retrofit api in order to cache `GET` responses.

It's using the caching functionalities from okhttp, meaning that it'll add HTTP headers to the responses which are
annotated with the `ResponseCache` annotation.

## Setup

### Add the dependency

```groovy
implementation 'com.andretietz.retrofit:cache-extension:x.y.z'
```

### Snapshot versions

Snapshot versions are available, by adding the mavenCentral snapshot repository

```groovy
 maven { url { "https://oss.sonatype.org/content/repositories/snapshots" } }
```

and adding `-SNAPSHOT` to the version number:

```groovy
implementation 'com.andretietz.retrofit:cache-extension:x.y.z-SNAPSHOT'
```

### Setup the retrofit instance

There are 3 different ways on how to set it up:

* using the kotlin extension
  ```kotlin
    val retrofit = Retrofit.Builder()
    // ... setup your retrofit instance
    .build()
    .responseCache(Cache(CACHE_DIR, CACHE_SIZE))
  ```
* using the static method (also for java)
     ```java
        Retrofit retrofit = ResponseCacheExtension.setup(retrofit, new Cache(CACHE_DIR, CACHE_SIZE)); 
    ```
* adding the interceptor and the cache manually
    ```kotlin
    val client = OkHttpClient.Builder()
        // ...
        .addNetworkInterceptor(ResponseCacheInterceptor())
        .cache(Cache(CACHE_DIR, CACHE_SIZE))
        .build()
    val retrofit = Retrofit.Builder()
        //... setup your retrofit instance
        .client(client)
        .build()
    ```

### Apply Annotations

```kotlin
interface SomeApi {
    @GET("foo/bar")
    @ResponseCache(5, unit = TimeUnit.MINUTES)
    fun cacheThisForFiveMinutes()
}
```

## Limitations

* Only `GET`-Calls are cached!
* If your cache is too small or not setup correctly, nothing will be cached.

## Configuration

### What if the server provides it's own `Cache-Control` Headers?

The `Cache-Control` Headers will be set only, if there are none in already. Meaning, if the server provides the Headers,
it'll NOT override them by default.

If for some reason you need that, use the (optional) `override = true` argument as annotation argument. This argument
is `false` by default.

### What if I want to set my own custom `Cache-Control` Header?

There's the option in all 3 options mentioned above to provide a custom CacheControl content.

```kotlin
// ...
.build()
    .responseCache(Cache(CACHE_DIR, CACHE_SIZE)) { annotation ->
        CacheControl.Builder()
            // ... setup your custom CacheControl
            .build()
    }   
```

```java
// ...
retrofit=ResponseCacheExtension.setup(
        retrofit,
        new Cache(CACHE_DIR,CACHE_SIZE),
        annotation->new CacheControl.Builder()
        // ...
        .build()
        );
```

```kotlin
val client = OkHttpClient.Builder()
    // ...
    .addNetworkInterceptor(ResponseCacheInterceptor() { annotation ->
        CacheControl.Builder()
            // ...
            .build()
    })
    .cache(Cache(CACHE_DIR, CACHE_SIZE))
    .build()
```

Default is:

```kotlin
{ annotation: ResponseCache ->
    CacheControl.Builder()
        .maxAge(annotation.value, annotation.unit)
        .build()
}
```

## LICENSE

```
Copyrights 2021 André Tietz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
