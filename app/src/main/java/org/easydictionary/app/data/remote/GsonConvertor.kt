package org.easydictionary.app.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder

fun provideGsonDateConvertor(): Gson {
    return GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()
}