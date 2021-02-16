package com.darkraha.services.core.utils.json

import java.io.Reader

abstract class JsonConverterA {

    abstract fun toJson(obj: Any): String
    abstract fun <T> fromJson(src: String, cls: Class<T>): T
    abstract fun <T> readJson(src: Reader, cls: Class<T>): T


}