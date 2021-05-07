package com.darkraha.services.core.extensions

import java.math.BigInteger
import java.security.MessageDigest

/**
 * Encodes string by md5 algorithm.
 */
fun String.encodeMd5(): String {
    val digest = MessageDigest.getInstance("MD5")
    digest.update(this.toByteArray())
    return BigInteger(digest.digest()).abs().toString(36)
}


fun String.lastPathSegment(): String {
    val ind = lastIndexOf("/")
    return if (ind > 0) substring(ind + 1) else this
}