@file:Suppress("NOTHING_TO_INLINE")

package com.github.mvysny.unsigned

import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import java.nio.ByteOrder

// C2 collapses a byte-array-view VarHandle into a single unaligned (byte-swapping) load or store
// only while it can see the handle itself as a constant — which means a static final field, and
// therefore a top-level `val`. Folding these into `Endian` as constructor-injected instance fields
// reads better, compiles, and passes every test, but silently demotes each access to a generic
// VarHandle invocation slower than the hand-rolled shifts this replaced. Leave them out here.
private val SHORT_BE: VarHandle =
    MethodHandles.byteArrayViewVarHandle(ShortArray::class.java, ByteOrder.BIG_ENDIAN)
private val SHORT_LE: VarHandle =
    MethodHandles.byteArrayViewVarHandle(ShortArray::class.java, ByteOrder.LITTLE_ENDIAN)
private val INT_BE: VarHandle =
    MethodHandles.byteArrayViewVarHandle(IntArray::class.java, ByteOrder.BIG_ENDIAN)
private val INT_LE: VarHandle =
    MethodHandles.byteArrayViewVarHandle(IntArray::class.java, ByteOrder.LITTLE_ENDIAN)
private val LONG_BE: VarHandle =
    MethodHandles.byteArrayViewVarHandle(LongArray::class.java, ByteOrder.BIG_ENDIAN)
private val LONG_LE: VarHandle =
    MethodHandles.byteArrayViewVarHandle(LongArray::class.java, ByteOrder.LITTLE_ENDIAN)

/**
 * Describes endianness to be used when accessing or updating a sequence of bytes.
 *
 * ```
 * val bytes = ByteArray(4)
 * Endian.Little.setInt(bytes, 0, 0x0a0b0c0d)
 * Endian.Big.getUShort(bytes, 2)      // => 0x0b0au
 * ```
 *
 * Offsets are unaligned-safe: any accessor may start at any offset.
 *
 * Every accessor throws [IndexOutOfBoundsException] when the bytes it would touch fall
 * outside the array — but the message counts in units of the value's width rather than
 * bytes, so reading an [Int] at offset 2 of a 4-byte array reports "index 2 out of bounds
 * for length 1".
 */
public enum class Endian {
    Big {
        override fun getShort(bytes: ByteArray, byteOffset: Int): Short =
            SHORT_BE.get(bytes, byteOffset) as Short

        override fun setShort(bytes: ByteArray, byteOffset: Int, value: Int) {
            SHORT_BE.set(bytes, byteOffset, value.toShort())
        }

        override fun getInt(bytes: ByteArray, byteOffset: Int): Int =
            INT_BE.get(bytes, byteOffset) as Int

        override fun setInt(bytes: ByteArray, byteOffset: Int, value: Int) {
            INT_BE.set(bytes, byteOffset, value)
        }

        override fun getLong(bytes: ByteArray, byteOffset: Int): Long =
            LONG_BE.get(bytes, byteOffset) as Long

        override fun setLong(bytes: ByteArray, byteOffset: Int, value: Long) {
            LONG_BE.set(bytes, byteOffset, value)
        }
    },
    Little {
        override fun getShort(bytes: ByteArray, byteOffset: Int): Short =
            SHORT_LE.get(bytes, byteOffset) as Short

        override fun setShort(bytes: ByteArray, byteOffset: Int, value: Int) {
            SHORT_LE.set(bytes, byteOffset, value.toShort())
        }

        override fun getInt(bytes: ByteArray, byteOffset: Int): Int =
            INT_LE.get(bytes, byteOffset) as Int

        override fun setInt(bytes: ByteArray, byteOffset: Int, value: Int) {
            INT_LE.set(bytes, byteOffset, value)
        }

        override fun getLong(bytes: ByteArray, byteOffset: Int): Long =
            LONG_LE.get(bytes, byteOffset) as Long

        override fun setLong(bytes: ByteArray, byteOffset: Int, value: Long) {
            LONG_LE.set(bytes, byteOffset, value)
        }
    };

    /**
     * Returns the (possibly negative) integer represented by the two bytes at
     * the specified [byteOffset] in this object, in two's complement binary
     * form.
     *
     * The return value will be between -2<sup>15</sup> and 2<sup>15</sup> - 1,
     * inclusive.
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 2` must be less than or equal to the length of this object.
     */
    public abstract fun getShort(bytes: ByteArray, byteOffset: Int): Short

    /**
     * Sets the two bytes starting at the specified [byteOffset] in this
     * object to the two's complement binary representation of the specified
     * [value].
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 2` must be less than or equal to the length of [bytes].
     */
    public inline fun setShort(bytes: ByteArray, byteOffset: Int, value: Short) {
        setShort(bytes, byteOffset, value.toInt())
    }

    /**
     * Sets the two bytes starting at the specified [byteOffset] in this
     * object to the two's complement binary representation of the specified
     * [value]. Only the lowest 16 bits are considered, high 16 bits are ignored.
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 2` must be less than or equal to the length of [bytes].
     */
    public abstract fun setShort(bytes: ByteArray, byteOffset: Int, value: Int)

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
    public inline fun getUShort(bytes: ByteArray, byteOffset: Int): UShort = getShort(bytes, byteOffset).toUShort()

    /**
     * Sets the two bytes starting at the specified [byteOffset] in this object
     * to the unsigned binary representation of the specified [value],
     * which must fit in two bytes.
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 2` must be less than or equal to the length of [bytes].
     */
    public inline fun setUShort(bytes: ByteArray, byteOffset: Int, value: UShort) {
        setShort(bytes, byteOffset, value.toShort());
    }

    /**
     * Sets the two bytes starting at the specified [byteOffset] in this object
     * to the unsigned binary representation of the specified [value],
     * which must fit in two bytes.
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 2` must be less than or equal to the length of [bytes].
     */
    public inline fun setUShort(bytes: ByteArray, byteOffset: Int, value: UInt) {
        setShort(bytes, byteOffset, value.toShort())
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
    public abstract fun getInt(bytes: ByteArray, byteOffset: Int): Int

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
    public abstract fun setInt(bytes: ByteArray, byteOffset: Int, value: Int)

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
    public inline fun getUInt(bytes: ByteArray, byteOffset: Int): UInt = getInt(bytes, byteOffset).toUInt()

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
    public inline fun setUInt(bytes: ByteArray, byteOffset: Int, value: UInt) {
        setInt(bytes, byteOffset, value.toInt())
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
    public abstract fun getLong(bytes: ByteArray, byteOffset: Int): Long

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
    public abstract fun setLong(bytes: ByteArray, byteOffset: Int, value: Long)

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
    public inline fun getULong(bytes: ByteArray, byteOffset: Int): ULong = getLong(bytes, byteOffset).toULong()

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
    public inline fun setULong(bytes: ByteArray, byteOffset: Int, value: ULong) {
        setLong(bytes, byteOffset, value.toLong())
    }
}
