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
 * Returns the (possibly negative) integer represented by the two bytes at
 * the specified [byteOffset] in this object, in two's complement binary
 * form.
 *
 * The return value will be between -2^15 and 2^15 - 1,
 * inclusive.
 *
 * The [byteOffset] must be non-negative, and
 * `byteOffset + 2` must be less than or equal to the length of this object.
 */
public inline fun ByteArray.getShort(
    byteOffset: Int,
    endian: Endian = Endian.Big
): Short = endian.getShort(this, byteOffset)

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
* Returns the positive integer represented by the two bytes starting
* at the specified [byteOffset] in this object, in unsigned binary
* form.
*
* The return value will be between 0 and  2^16 - 1, inclusive.
*
* The [byteOffset] must be non-negative, and
* `byteOffset + 2` must be less than or equal to the length of this object.
 */
public inline fun ByteArray.getUShort(byteOffset: Int, endian: Endian = Endian.Big): UShort = endian.getUShort(this, byteOffset)

/**
 * Sets the two bytes starting at the specified [byteOffset] in this object
 * to the unsigned binary representation of the specified [value],
 * which must fit in two bytes.
 *
 * The [byteOffset] must be non-negative, and
 * `byteOffset + 2` must be less than or equal to the length of [bytes].
 */
public inline fun ByteArray.setUShort(byteOffset: Int, value: UShort, endian: Endian = Endian.Big) {
    endian.setUShort(this, byteOffset, value)
}

/**
 * Sets the two bytes starting at the specified [byteOffset] in this object
 * to the unsigned binary representation of the specified [value],
 * which must fit in two bytes.
 *
 * The [byteOffset] must be non-negative, and
 * `byteOffset + 2` must be less than or equal to the length of [bytes].
 */
public inline fun ByteArray.setUShort(byteOffset: Int, value: UInt, endian: Endian = Endian.Big) {
    endian.setUShort(this, byteOffset, value)
}


/**
 * Returns the (possibly negative) integer represented by the four bytes at
 * the specified [byteOffset] in this object, in two's complement binary
 * form.
 *
 * The return value will be between -2^31 and 2^31 - 1,
 * inclusive.
 *
 * The [byteOffset] must be non-negative, and
 * `byteOffset + 4` must be less than or equal to the length of this object.
 */
public inline fun ByteArray.getInt(byteOffset: Int, endian: Endian = Endian.Big): Int = endian.getInt(this, byteOffset)

/**
 * Sets the four bytes starting at the specified [byteOffset] in this
 * object to the two's complement binary representation of the specified
 * [value], which must fit in four bytes.
 *
 * In other words, [value] must lie
 * between -2^31 and 2^31 - 1, inclusive.
 *
 * The [byteOffset] must be non-negative, and
 * `byteOffset + 4` must be less than or equal to the length of this object.
 */
public inline fun ByteArray.setInt(byteOffset: Int, value: Int, endian: Endian = Endian.Big) {
    endian.setInt(this, byteOffset, value)
}

/**
 * Returns the positive integer represented by the four bytes starting
 * at the specified [byteOffset] in this object, in unsigned binary
 * form.
 *
 * The return value will be between 0 and  2^32 - 1, inclusive.
 *
 * The [byteOffset] must be non-negative, and
 * `byteOffset + 4` must be less than or equal to the length of this object.
 */
public inline fun ByteArray.getUInt(byteOffset: Int, endian: Endian = Endian.Big): UInt = endian.getUInt(this, byteOffset)

/**
 * Sets the four bytes starting at the specified [byteOffset] in this object
 * to the unsigned binary representation of the specified [value],
 * which must fit in four bytes.
 *
 * In other words, [value] must be between
 * 0 and 2^32 - 1, inclusive.
 *
 * The [byteOffset] must be non-negative, and
 * `byteOffset + 4` must be less than or equal to the length of this object.
 */
public inline fun ByteArray.setUInt(byteOffset: Int, value: UInt, endian: Endian = Endian.Big) {
    endian.setUInt(this, byteOffset, value)
}


/**
 * Returns the (possibly negative) integer represented by the eight bytes at
 * the specified [byteOffset] in this object, in two's complement binary
 * form.
 *
 * The return value will be between -2^63 and 2^63 - 1,
 * inclusive.
 *
 * The [byteOffset] must be non-negative, and
 * `byteOffset + 8` must be less than or equal to the length of this object.
 */
public inline fun ByteArray.getLong(byteOffset: Int, endian: Endian = Endian.Big): Long = endian.getLong(this, byteOffset)

/**
 * Sets the eight bytes starting at the specified [byteOffset] in this
 * object to the two's complement binary representation of the specified
 * [value], which must fit in eight bytes.
 *
 * In other words, [value] must lie
 * between -2^63 and 2^63 - 1, inclusive.
 *
 * The [byteOffset] must be non-negative, and
 * `byteOffset + 8` must be less than or equal to the length of this object.
 */
public inline fun ByteArray.setLong(byteOffset: Int, value: Long, endian: Endian = Endian.Big) {
    endian.setLong(this, byteOffset, value)
}

/**
 * Returns the positive integer represented by the eight bytes starting
 * at the specified [byteOffset] in this object, in unsigned binary
 * form.
 *
 * The return value will be between 0 and  2^64 - 1, inclusive.
 *
 * The [byteOffset] must be non-negative, and
 * `byteOffset + 8` must be less than or equal to the length of this object.
 */
public inline fun ByteArray.getULong(byteOffset: Int, endian: Endian = Endian.Big): ULong = endian.getULong(this, byteOffset)

/**
 * Sets the eight bytes starting at the specified [byteOffset] in this object
 * to the unsigned binary representation of the specified [value],
 * which must fit in eight bytes.
 *
 * In other words, [value] must be between
 * 0 and 2^64 - 1, inclusive.
 *
 * The [byteOffset] must be non-negative, and
 * `byteOffset + 8` must be less than or equal to the length of this object.
 */
public inline fun ByteArray.setULong(byteOffset: Int, value: ULong, endian: Endian = Endian.Big) {
    endian.setULong(this, byteOffset, value)
}
