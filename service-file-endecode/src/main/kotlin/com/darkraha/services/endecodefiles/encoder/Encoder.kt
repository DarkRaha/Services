package com.darkraha.services.endecodefiles.encoder

import java.io.File

abstract class Encoder<T> {
    abstract fun encode(obj: T, encodeParam: Any?, dstFile: File): File?
}