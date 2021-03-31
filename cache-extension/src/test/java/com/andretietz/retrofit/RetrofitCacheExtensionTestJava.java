package com.andretietz.retrofit;

import org.junit.Test;

import java.io.File;

import okhttp3.Cache;
import retrofit2.Retrofit;

import static junit.framework.TestCase.assertNotNull;

public class RetrofitCacheExtensionTestJava {
    @Test
    public void testInit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.foo.bar/")
                .build();
        retrofit = ResponseCacheExtension.setup(retrofit, new Cache(new File("."), 5000));

        assertNotNull(retrofit);
    }
}
