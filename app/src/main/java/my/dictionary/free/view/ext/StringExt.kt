package my.dictionary.free.view.ext

import java.nio.charset.Charset

fun String.decodeHex(charset: Charset): String {
    require(length % 2 == 0) {"Bad hex string. Value is not divide 2"}
    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
        .toString(charset)
}

fun String.toUnicode(radix: Int = 16): String {
    return this.toLong(radix).toInt().toChar().toString()
}