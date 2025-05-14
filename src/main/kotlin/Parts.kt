package com.github.mvysny.unsigned

/**
 * Returns the highest 8 bits of this 16-bit unsigned value.
 */
public inline val UShort.hibyte: UByte get() = (toInt() ushr 8).toUByte()
/**
 * Returns the lowest 8 bits of this 16-bit unsigned value.
 */
public inline val UShort.lobyte: UByte get() = toUByte()
