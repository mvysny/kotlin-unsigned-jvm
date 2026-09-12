package com.github.mvysny.unsigned

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

private const val SIZE = 8

/**
 * Expects [access] to fail with an [IndexOutOfBoundsException] for every offset at which a
 * [width]-byte value no longer fits in a [SIZE]-byte array — just past the last valid one, past
 * the end of the array, negative, and at both extremes of [Int] (where a naive
 * `byteOffset + width` bounds check would overflow and wave the access through).
 */
private fun expectOutOfBounds(width: Int, access: (ByteArray, Int) -> Unit) {
    val badOffsets = listOf(-1, SIZE - width + 1, SIZE, Int.MAX_VALUE, Int.MIN_VALUE)
    for (byteOffset in badOffsets.distinct()) {
        assertFailsWith<IndexOutOfBoundsException>("a $width-byte value must not be accessible at offset $byteOffset of a $SIZE-byte array") {
            access(ByteArray(SIZE), byteOffset)
        }
    }
}

/**
 * Pins the out-of-bounds contract documented on [Endian]: an accessor whose bytes fall outside the
 * array throws [IndexOutOfBoundsException] rather than reading adjacent memory, silently truncating,
 * or throwing something else. Only the exception type is asserted — the message the `VarHandle`
 * bounds check produces counts in units of the value's width and is not part of the contract.
 */
class BoundsTest {
    @Nested inner class Byte_ {
        @Test fun get() = expectOutOfBounds(1) { b, o -> b.getByte(o) }
        @Test fun set() = expectOutOfBounds(1) { b, o -> b.setByte(o, 0.toByte()) }
        @Test fun getUnsigned() = expectOutOfBounds(1) { b, o -> b.getUByte(o) }
        @Test fun setUnsigned() = expectOutOfBounds(1) { b, o -> b.setUByte(o, 0.toUByte()) }
    }
    @Nested inner class Short_ {
        @Test fun big() = expectOutOfBounds(2) { b, o -> b.getShort(o) }
        @Test fun little() = expectOutOfBounds(2) { b, o -> b.getShort(o, Endian.Little) }
        @Test fun setBig() = expectOutOfBounds(2) { b, o -> b.setShort(o, 0.toShort()) }
        @Test fun setLittle() = expectOutOfBounds(2) { b, o -> b.setShort(o, 0.toShort(), Endian.Little) }
        @Test fun getUnsigned() = expectOutOfBounds(2) { b, o -> b.getUShort(o) }
        @Test fun setUnsigned() = expectOutOfBounds(2) { b, o -> b.setUShort(o, 0.toUShort()) }
    }
    @Nested inner class Int_ {
        @Test fun big() = expectOutOfBounds(4) { b, o -> b.getInt(o) }
        @Test fun little() = expectOutOfBounds(4) { b, o -> b.getInt(o, Endian.Little) }
        @Test fun setBig() = expectOutOfBounds(4) { b, o -> b.setInt(o, 0) }
        @Test fun setLittle() = expectOutOfBounds(4) { b, o -> b.setInt(o, 0, Endian.Little) }
        @Test fun getUnsigned() = expectOutOfBounds(4) { b, o -> b.getUInt(o) }
        @Test fun setUnsigned() = expectOutOfBounds(4) { b, o -> b.setUInt(o, 0u) }
    }
    @Nested inner class Long_ {
        @Test fun big() = expectOutOfBounds(8) { b, o -> b.getLong(o) }
        @Test fun little() = expectOutOfBounds(8) { b, o -> b.getLong(o, Endian.Little) }
        @Test fun setBig() = expectOutOfBounds(8) { b, o -> b.setLong(o, 0L) }
        @Test fun setLittle() = expectOutOfBounds(8) { b, o -> b.setLong(o, 0L, Endian.Little) }
        @Test fun getUnsigned() = expectOutOfBounds(8) { b, o -> b.getULong(o) }
        @Test fun setUnsigned() = expectOutOfBounds(8) { b, o -> b.setULong(o, 0uL) }
    }
}
