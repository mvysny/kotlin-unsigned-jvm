package com.github.mvysny.unsigned

/**
 * Formats this byte array as a string of hex, e.g. "02ff35"
 */
fun ByteArray.toHex(): String = joinToString(separator = "") { it.toHex() }

/**
 * Formats this byte as a 2-character hex value, e.g. "03" or "fe".
 */
fun Byte.toHex(): String = toUByte().toInt().toString(16).padStart(2, '0')
