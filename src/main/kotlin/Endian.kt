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
 * The byte order a multi-byte value is read or written in. Both constants offer the same
 * accessors, so byte order is an ordinary value — pick it, store it, or take it from a header you
 * just parsed, and mix two of them over one array:
 *
 * ```
 * val bytes = ByteArray(4)
 * Endian.Little.setInt(bytes, 0, 0x0a0b0c0d)   // bytes are now 0d 0c 0b 0a
 * Endian.Big.getUShort(bytes, 2)               // => 0x0b0au
 * ```
 *
 * The same accessors exist as [ByteArray] extensions — `bytes.getUShort(2)` — where the byte
 * order is a trailing parameter defaulting to [Big].
 *
 * Three rules hold for every accessor below, and are not repeated on each one:
 *
 * - **Any offset works.** Access is unaligned-safe: `byteOffset` need not be a multiple of the
 *   value's width, and costs nothing extra when it isn't.
 * - **Out of range throws.** Any accessor — here or on the [ByteArray] extensions — whose bytes
 *   fall outside the array throws [IndexOutOfBoundsException] rather than truncating or reading
 *   past the end. Its *message* counts in units of the value's width rather than bytes, so an
 *   [Int] read at offset 2 of a 4-byte array reports "index 2 out of bounds for length 1".
 * - **A wider-typed setter truncates in silence.** The overloads taking a type wider than they
 *   write — [setShort] from an [Int], [setUShort] from a [UInt] — keep the low bits and discard
 *   the rest without checking. That is deliberate, so that a value carried around in a wider type
 *   needs no cast at the call site.
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
     * Reads the two bytes of [bytes] starting at [byteOffset], in two's complement form.
     */
    public abstract fun getShort(bytes: ByteArray, byteOffset: Int): Short

    /**
     * Writes [value] into the two bytes of [bytes] starting at [byteOffset], in two's complement
     * form.
     */
    public inline fun setShort(bytes: ByteArray, byteOffset: Int, value: Short) {
        setShort(bytes, byteOffset, value.toInt())
    }

    /**
     * Writes the low 16 bits of [value] into the two bytes of [bytes] starting at [byteOffset],
     * in two's complement form. The high 16 bits are dropped, whatever they hold.
     */
    public abstract fun setShort(bytes: ByteArray, byteOffset: Int, value: Int)

    /**
     * Reads the two bytes of [bytes] starting at [byteOffset], in unsigned binary form.
     */
    public inline fun getUShort(bytes: ByteArray, byteOffset: Int): UShort = getShort(bytes, byteOffset).toUShort()

    /**
     * Writes [value] into the two bytes of [bytes] starting at [byteOffset], in unsigned binary
     * form.
     */
    public inline fun setUShort(bytes: ByteArray, byteOffset: Int, value: UShort) {
        setShort(bytes, byteOffset, value.toShort());
    }

    /**
     * Writes the low 16 bits of [value] into the two bytes of [bytes] starting at [byteOffset],
     * in unsigned binary form. The high 16 bits are dropped, whatever they hold.
     */
    public inline fun setUShort(bytes: ByteArray, byteOffset: Int, value: UInt) {
        setShort(bytes, byteOffset, value.toShort())
    }

    /**
     * Reads the four bytes of [bytes] starting at [byteOffset], in two's complement form.
     */
    public abstract fun getInt(bytes: ByteArray, byteOffset: Int): Int

    /**
     * Writes [value] into the four bytes of [bytes] starting at [byteOffset], in two's complement
     * form.
     */
    public abstract fun setInt(bytes: ByteArray, byteOffset: Int, value: Int)

    /**
     * Reads the four bytes of [bytes] starting at [byteOffset], in unsigned binary form.
     */
    public inline fun getUInt(bytes: ByteArray, byteOffset: Int): UInt = getInt(bytes, byteOffset).toUInt()

    /**
     * Writes [value] into the four bytes of [bytes] starting at [byteOffset], in unsigned binary
     * form.
     */
    public inline fun setUInt(bytes: ByteArray, byteOffset: Int, value: UInt) {
        setInt(bytes, byteOffset, value.toInt())
    }

    /**
     * Reads the eight bytes of [bytes] starting at [byteOffset], in two's complement form.
     */
    public abstract fun getLong(bytes: ByteArray, byteOffset: Int): Long

    /**
     * Writes [value] into the eight bytes of [bytes] starting at [byteOffset], in two's complement
     * form.
     */
    public abstract fun setLong(bytes: ByteArray, byteOffset: Int, value: Long)

    /**
     * Reads the eight bytes of [bytes] starting at [byteOffset], in unsigned binary form.
     */
    public inline fun getULong(bytes: ByteArray, byteOffset: Int): ULong = getLong(bytes, byteOffset).toULong()

    /**
     * Writes [value] into the eight bytes of [bytes] starting at [byteOffset], in unsigned binary
     * form.
     */
    public inline fun setULong(bytes: ByteArray, byteOffset: Int, value: ULong) {
        setLong(bytes, byteOffset, value.toLong())
    }

    /**
     * Reads the four bytes of [bytes] starting at [byteOffset] as IEEE 754 binary32.
     *
     * Every four-byte pattern is a valid [Float] — NaNs and infinities included — so nothing here
     * validates or rejects. Materializing a NaN may not preserve its payload bits, which is
     * [Float.Companion.fromBits]'s own documented caveat rather than a conversion this adds.
     */
    public inline fun getFloat(bytes: ByteArray, byteOffset: Int): Float =
        Float.fromBits(getInt(bytes, byteOffset))

    /**
     * Writes [value] into the four bytes of [bytes] starting at [byteOffset], as IEEE 754 binary32.
     *
     * The bits go out exactly as given. This uses [Float.toRawBits] and not [Float.toBits], which
     * would rewrite every NaN to the canonical `0x7fc00000` — and since every binary format that
     * carries a float specifies a bit pattern, and some use NaN payloads as sentinels, that would
     * be non-conformance rather than a detail.
     */
    public inline fun setFloat(bytes: ByteArray, byteOffset: Int, value: Float) {
        setInt(bytes, byteOffset, value.toRawBits())
    }

    /**
     * Reads the eight bytes of [bytes] starting at [byteOffset] as IEEE 754 binary64.
     *
     * Every eight-byte pattern is a valid [Double] — NaNs and infinities included — so nothing here
     * validates or rejects. Materializing a NaN may not preserve its payload bits, which is
     * [Double.Companion.fromBits]'s own documented caveat rather than a conversion this adds.
     */
    public inline fun getDouble(bytes: ByteArray, byteOffset: Int): Double =
        Double.fromBits(getLong(bytes, byteOffset))

    /**
     * Writes [value] into the eight bytes of [bytes] starting at [byteOffset], as IEEE 754
     * binary64.
     *
     * The bits go out exactly as given. This uses [Double.toRawBits] and not [Double.toBits],
     * which would rewrite every NaN to the canonical `0x7ff8000000000000` — see [setFloat] for
     * why that matters.
     */
    public inline fun setDouble(bytes: ByteArray, byteOffset: Int, value: Double) {
        setLong(bytes, byteOffset, value.toRawBits())
    }
}
