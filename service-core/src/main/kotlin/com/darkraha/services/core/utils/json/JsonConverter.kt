package com.darkraha.services.core.utils.json

import com.google.gson.Gson
import java.io.Reader

class JsonConverter(protected val gson: Gson = Gson()) : JsonConverterA() {

    override fun toJson(obj: Any): String {
        return gson.toJson(obj)
    }

    override fun <T> fromJson(src: String, cls: Class<T>): T {
        return gson.fromJson(src, cls)
    }

    override fun <T> readJson(src: Reader, cls: Class<T>): T {
        return gson.fromJson(src, cls)
    }
}