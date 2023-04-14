@file:Suppress("NOTHING_TO_INLINE")

package com.github.mvysny.unsigned

/**
 * Returns the (possibly negative) byte represented by the byte at the
 * specified [byteOffset] in this object, in two's complement binary
 * representation.
 *
 * The return value will be between -128 and 127, inclusive.
 *
 * The [byteOffset] must be non-negative, and
 * less than the length of this object.
 */
public inline fun ByteArray.getByte(byteOffset: Int): Byte = get(byteOffset)

/**
 * Sets the byte at the specified [byteOffset] in this object to the
 * two's complement binary representation of the specified [value], which
 * must fit in a single byte.
 *
 * In other words, [value] must be between -128 and 127, inclusive.
 *
 * The [byteOffset] must be non-negative, and
 * less than the length of this object.
 */
public inline fun ByteArray.setByte(byteOffset: Int, value: Byte) {
    set(byteOffset, value)
}

/**
 * Sets the byte at the specified [byteOffset] in this object to the
 * two's complement binary representation of the specified [value], which
 * must fit in a single byte.
 *
 * In other words, [value] must be between -128 and 127, inclusive.
 *
 * The [byteOffset] must be non-negative, and
 * less than the length of this object.
 */
public inline fun ByteArray.setByte(byteOffset: Int, value: Int) {
    set(byteOffset, value.toByte())
}

/**
* Returns the positive integer represented by the byte at the specified
* [byteOffset] in this object, in unsigned binary form.
*
* The return value will be between 0 and 255, inclusive.
*
* The [byteOffset] must be non-negative, and
* less than the length of this object.
 */
public inline fun ByteArray.getUByte(byteOffset: Int): UByte = getByte(byteOffset).toUByte()

/**
* Sets the byte at the specified [byteOffset] in this object to the
* unsigned binary representation of the specified [value], which must fit
* in a single byte.
*
* In other words, [value] must be between 0 and 255, inclusive.
*
* The [byteOffset] must be non-negative, and
* less than the length of this object.
 */
public inline fun ByteArray.setUByte(byteOffset: Int, value: UInt) {
    set(byteOffset, value.toByte())
}

/**
 * Sets the byte at the specified [byteOffset] in this object to the
 * unsigned binary representation of the specified [value], which must fit
 * in a single byte.
 *
 * In other words, [value] must be between 0 and 255, inclusive.
 *
 * The [byteOffset] must be non-negative, and
 * less than the length of this object.
 */
public inline fun ByteArray.setUByte(byteOffset: Int, value: UByte) {
    set(byteOffset, value.toByte())
}

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
