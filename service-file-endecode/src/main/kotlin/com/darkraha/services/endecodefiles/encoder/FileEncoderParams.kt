package com.darkraha.services.endecodefiles.encoder

import java.io.File

class FileEncoderParams(
    val source: Any,
    val dstFile: File,
    val encoder: Encoder<*>,
    val encodeParams: Any? = null
)