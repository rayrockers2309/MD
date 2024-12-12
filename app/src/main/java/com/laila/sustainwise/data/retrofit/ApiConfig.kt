//package com.laila.sustainwise.data.retrofit
//
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//class ApiConfig {
//
//    companion object {
//        fun getApiService(): ApiService {
//            val loggingInterceptor =
//                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
//
//            val client = OkHttpClient.Builder()
//                .addInterceptor(loggingInterceptor)
//                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
//                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
//                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
//                .build()
//
//            val retrofit = Retrofit.Builder()
//                .baseUrl("https://sustain-wise-1041878630324.asia-southeast2.run.app")
//                .addConverterFactory(GsonConverterFactory.create())
//                .client(client)
//                .build()
//            return retrofit.create(ApiService::class.java)
//        }
//    }
//}