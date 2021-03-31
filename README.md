# Retrofit Cache Extension
This extension adds a `ResponseCache` annotation to the retrofit api in order to cache `GET` responses.

## Setup
### Add the dependency
```groovy
implementation 'com.andretietz.retrofit:cache-extension:x.y.z'
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
