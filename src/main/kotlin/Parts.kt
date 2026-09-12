package com.github.mvysny.unsigned

/**
 * The high 8 bits of this 16-bit value.
 *
 * ```
 * 0x80FF.toUShort().hibyte   // => 0x80u
 * ```
 */
public inline val UShort.hibyte: UByte get() = (toInt() ushr 8).toUByte()

/**
 * The low 8 bits of this 16-bit value.
 *
 * ```
 * 0x80FF.toUShort().lobyte   // => 0xFFu
 * ```
 */
public inline val UShort.lobyte: UByte get() = toUByte()
