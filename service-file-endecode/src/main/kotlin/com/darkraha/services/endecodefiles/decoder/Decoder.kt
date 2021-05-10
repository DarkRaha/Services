package com.darkraha.services.endecodefiles.decoder

import java.io.File

abstract class Decoder<R> {
    abstract fun decode(file: File, param: Any?): R?

    open fun calcSize(obj: R?) = 0
}