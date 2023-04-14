@file:Suppress("NOTHING_TO_INLINE")

package com.github.mvysny.unsigned

/**
 * Sets the two bytes starting at the specified [byteOffset] in this
 * object to the two's complement binary representation of the specified
 * [value].
 *
 * The [byteOffset] must be non-negative, and
 * `byteOffset + 2` must be less than or equal to the length of [bytes].
 */
public inline fun ByteArray.setShort(byteOffset: Int, value: Short, endian: Endian = Endian.Big) {
    endian.setShort(this, byteOffset, value)
}

/**
 * Sets the two bytes starting at the specified [byteOffset] in this
 * object to the two's complement binary representation of the specified
 * [value]. Only the lowest 16 bits are considered, high 16 bits are ignored.
 *
 * The [byteOffset] must be non-negative, and
 * `byteOffset + 2` must be less than or equal to the length of [bytes].
 */
public inline fun ByteArray.setShort(byteOffset: Int, value: Int, endian: Endian = Endian.Big) {
    endian.setShort(this, byteOffset, value)
}

/**
 * Sets the two bytes starting at the specified [byteOffset] in this
 * object to the two's complement binary representation of the specified
 * [value].
 *
 * The [byteOffset] must be non-negative, and
 * `byteOffset + 2` must be less than or equal to the length of [bytes].
 */
public inline fun ByteArray.setUShort(byteOffset: Int, value: UShort, endian: Endian = Endian.Big) {
    endian.setUShort(this, byteOffset, value)
}

/**
 * Sets the two bytes starting at the specified [byteOffset] in this
 * object to the two's complement binary representation of the specified
 * [value]. Only the lowest 16 bits are considered, high 16 bits are ignored.
 *
 * The [byteOffset] must be non-negative, and
 * `byteOffset + 2` must be less than or equal to the length of [bytes].
 */
public inline fun ByteArray.setUShort(byteOffset: Int, value: UInt, endian: Endian = Endian.Big) {
    endian.setUShort(this, byteOffset, value)
}
