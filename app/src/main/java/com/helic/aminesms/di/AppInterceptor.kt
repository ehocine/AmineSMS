package com.helic.aminesms.di

import com.helic.aminesms.utils.Constants.API_KEY
import okhttp3.Interceptor
import okhttp3.Response

class AppInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("X-API-KEY", API_KEY)
            .build()
        return chain.proceed(request = request)
    }
}