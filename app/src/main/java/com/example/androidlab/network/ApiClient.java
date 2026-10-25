package com.example.androidlab.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Единственный на приложение экземпляр Retrofit с настроенным HTTP-клиентом.
 */
public final class ApiClient {

    private static MlbbHubApi api;

    private ApiClient() {
    }

    public static synchronized MlbbHubApi getApi() {
        if (api == null) {
            // Запросы и коды ответов видны в LogCat с тегом okhttp.OkHttpClient
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .build();

            api = new Retrofit.Builder()
                    .baseUrl(MlbbHubApi.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(MlbbHubApi.class);
        }
        return api;
    }
}
