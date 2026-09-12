@file:Suppress("NOTHING_TO_INLINE")

package com.github.mvysny.unsigned

/**
 * Reads the byte at [byteOffset], in two's complement form.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is not an index into this array.
 */
public inline fun ByteArray.getByte(byteOffset: Int): Byte = get(byteOffset)

/**
 * Writes [value] into the byte at [byteOffset], in two's complement form.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is not an index into this array.
 */
public inline fun ByteArray.setByte(byteOffset: Int, value: Byte) {
    set(byteOffset, value)
}

/**
 * Writes the low 8 bits of [value] into the byte at [byteOffset], in two's complement form. The
 * high 24 bits are dropped, whatever they hold.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is not an index into this array.
 */
public inline fun ByteArray.setByte(byteOffset: Int, value: Int) {
    set(byteOffset, value.toByte())
}

/**
 * Reads the byte at [byteOffset], in unsigned binary form.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is not an index into this array.
 */
public inline fun ByteArray.getUByte(byteOffset: Int): UByte = getByte(byteOffset).toUByte()

/**
 * Writes the low 8 bits of [value] into the byte at [byteOffset], in unsigned binary form. The
 * high 24 bits are dropped, whatever they hold.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is not an index into this array.
 */
public inline fun ByteArray.setUByte(byteOffset: Int, value: UInt) {
    set(byteOffset, value.toByte())
}

/**
 * Writes [value] into the byte at [byteOffset], in unsigned binary form.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is not an index into this array.
 */
public inline fun ByteArray.setUByte(byteOffset: Int, value: UByte) {
    set(byteOffset, value.toByte())
}

/**
 * Writes [value] into the two bytes starting at [byteOffset], in two's complement form and
 * [endian] byte order.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than two bytes remain
 *   from it.
 */
public inline fun ByteArray.setShort(byteOffset: Int, value: Short, endian: Endian = Endian.Big) {
    endian.setShort(this, byteOffset, value)
}

/**
 * Reads the two bytes starting at [byteOffset], in two's complement form and [endian] byte order.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than two bytes remain
 *   from it.
 */
public inline fun ByteArray.getShort(
    byteOffset: Int,
    endian: Endian = Endian.Big
): Short = endian.getShort(this, byteOffset)

/**
 * Writes the low 16 bits of [value] into the two bytes starting at [byteOffset], in two's
 * complement form and [endian] byte order. The high 16 bits are dropped, whatever they hold.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than two bytes remain
 *   from it.
 */
public inline fun ByteArray.setShort(byteOffset: Int, value: Int, endian: Endian = Endian.Big) {
    endian.setShort(this, byteOffset, value)
}

/**
 * Reads the two bytes starting at [byteOffset], in unsigned binary form and [endian] byte order.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than two bytes remain
 *   from it.
 */
public inline fun ByteArray.getUShort(byteOffset: Int, endian: Endian = Endian.Big): UShort = endian.getUShort(this, byteOffset)

/**
 * Writes [value] into the two bytes starting at [byteOffset], in unsigned binary form and [endian]
 * byte order.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than two bytes remain
 *   from it.
 */
public inline fun ByteArray.setUShort(byteOffset: Int, value: UShort, endian: Endian = Endian.Big) {
    endian.setUShort(this, byteOffset, value)
}

/**
 * Writes the low 16 bits of [value] into the two bytes starting at [byteOffset], in unsigned
 * binary form and [endian] byte order. The high 16 bits are dropped, whatever they hold.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than two bytes remain
 *   from it.
 */
public inline fun ByteArray.setUShort(byteOffset: Int, value: UInt, endian: Endian = Endian.Big) {
    endian.setUShort(this, byteOffset, value)
}


/**
 * Reads the four bytes starting at [byteOffset], in two's complement form and [endian] byte order.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than four bytes remain
 *   from it.
 */
public inline fun ByteArray.getInt(byteOffset: Int, endian: Endian = Endian.Big): Int = endian.getInt(this, byteOffset)

/**
 * Writes [value] into the four bytes starting at [byteOffset], in two's complement form and
 * [endian] byte order.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than four bytes remain
 *   from it.
 */
public inline fun ByteArray.setInt(byteOffset: Int, value: Int, endian: Endian = Endian.Big) {
    endian.setInt(this, byteOffset, value)
}

/**
 * Reads the four bytes starting at [byteOffset], in unsigned binary form and [endian] byte order.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than four bytes remain
 *   from it.
 */
public inline fun ByteArray.getUInt(byteOffset: Int, endian: Endian = Endian.Big): UInt = endian.getUInt(this, byteOffset)

/**
 * Writes [value] into the four bytes starting at [byteOffset], in unsigned binary form and
 * [endian] byte order.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than four bytes remain
 *   from it.
 */
public inline fun ByteArray.setUInt(byteOffset: Int, value: UInt, endian: Endian = Endian.Big) {
    endian.setUInt(this, byteOffset, value)
}


/**
 * Reads the eight bytes starting at [byteOffset], in two's complement form and [endian] byte order.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than eight bytes remain
 *   from it.
 */
public inline fun ByteArray.getLong(byteOffset: Int, endian: Endian = Endian.Big): Long = endian.getLong(this, byteOffset)

/**
 * Writes [value] into the eight bytes starting at [byteOffset], in two's complement form and
 * [endian] byte order.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than eight bytes remain
 *   from it.
 */
public inline fun ByteArray.setLong(byteOffset: Int, value: Long, endian: Endian = Endian.Big) {
    endian.setLong(this, byteOffset, value)
}

/**
 * Reads the eight bytes starting at [byteOffset], in unsigned binary form and [endian] byte order.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than eight bytes remain
 *   from it.
 */
public inline fun ByteArray.getULong(byteOffset: Int, endian: Endian = Endian.Big): ULong = endian.getULong(this, byteOffset)

/**
 * Writes [value] into the eight bytes starting at [byteOffset], in unsigned binary form and
 * [endian] byte order.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than eight bytes remain
 *   from it.
 */
public inline fun ByteArray.setULong(byteOffset: Int, value: ULong, endian: Endian = Endian.Big) {
    endian.setULong(this, byteOffset, value)
}


/**
 * Reads the four bytes starting at [byteOffset] as IEEE 754 binary32, in [endian] byte order.
 *
 * Every four-byte pattern is a valid [Float] — NaNs and infinities included — so nothing here
 * validates or rejects. See [Endian.getFloat] for what that means for a NaN payload.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than four bytes remain
 *   from it.
 */
public inline fun ByteArray.getFloat(byteOffset: Int, endian: Endian = Endian.Big): Float = endian.getFloat(this, byteOffset)

/**
 * Writes [value] into the four bytes starting at [byteOffset] as IEEE 754 binary32, in [endian]
 * byte order. The bits go out exactly as given, NaN payload included — see [Endian.setFloat].
 *
 * There is deliberately no overload taking a [Double]: `bytes.setFloat(0, 1.0)` is a compile error
 * rather than a silent narrowing, since Kotlin has no implicit widening to manufacture one.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than four bytes remain
 *   from it.
 */
public inline fun ByteArray.setFloat(byteOffset: Int, value: Float, endian: Endian = Endian.Big) {
    endian.setFloat(this, byteOffset, value)
}

/**
 * Reads the eight bytes starting at [byteOffset] as IEEE 754 binary64, in [endian] byte order.
 *
 * Every eight-byte pattern is a valid [Double] — NaNs and infinities included — so nothing here
 * validates or rejects. See [Endian.getDouble] for what that means for a NaN payload.
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than eight bytes remain
 *   from it.
 */
public inline fun ByteArray.getDouble(byteOffset: Int, endian: Endian = Endian.Big): Double = endian.getDouble(this, byteOffset)

/**
 * Writes [value] into the eight bytes starting at [byteOffset] as IEEE 754 binary64, in [endian]
 * byte order. The bits go out exactly as given, NaN payload included — see [Endian.setDouble].
 *
 * @throws IndexOutOfBoundsException if [byteOffset] is negative, or fewer than eight bytes remain
 *   from it.
 */
public inline fun ByteArray.setDouble(byteOffset: Int, value: Double, endian: Endian = Endian.Big) {
    endian.setDouble(this, byteOffset, value)
}
