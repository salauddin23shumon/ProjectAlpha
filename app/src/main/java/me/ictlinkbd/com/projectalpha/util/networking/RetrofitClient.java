package me.ictlinkbd.com.projectalpha.util.networking;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    private static Retrofit retrofit;
    private static RetrofitClient retrofitClient;


    private RetrofitClient(String url) {
        Log.d(TAG, "RetrofitClient: "+url);
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getClient())
                .build();
    }

    private RetrofitClient(final String token, String url) {
        OkHttpClient client = new OkHttpClient.Builder()
                .writeTimeout(3, TimeUnit.MINUTES)
                .readTimeout(3, TimeUnit.MINUTES)
                .addInterceptor(new Interceptor() {
            @NotNull
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "bearer" + token)
                        .build();
                return chain.proceed(newRequest);
            }
        }).build();


        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    public static synchronized RetrofitClient getInstance(String url) {
        return new RetrofitClient(url);
    }

    public static synchronized RetrofitClient getInstance(String token, String url) {
        return new RetrofitClient(token,url);
    }

    public ApiServiceInterface getApiService() {
        return retrofit.create(ApiServiceInterface.class);
    }

    public OkHttpClient getClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .writeTimeout(3, TimeUnit.MINUTES)
                .readTimeout(3, TimeUnit.MINUTES)
                .addInterceptor(interceptor)
                .build();
    }

}
