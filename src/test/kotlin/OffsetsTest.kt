package com.github.mvysny.unsigned

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

private const val SIZE = 10

/**
 * Writes [value] with [write] at every offset a [width]-byte value fits at in a 0xff-filled
 * [SIZE]-byte array, then expects [read] to return it back and the bytes outside the written
 * window to still be 0xff.
 *
 * This is what pins the "any accessor may start at any offset" promise: the accessors are only
 * ever exercised at offset 1 elsewhere, which would not catch an off-by-one at either end nor a
 * write that spills into the neighbouring byte.
 */
private fun <V> expectAtEveryOffset(
    width: Int,
    value: V,
    write: (ByteArray, Int, V) -> Unit,
    read: (ByteArray, Int) -> V
) {
    for (byteOffset in 0..(SIZE - width)) {
        val bytes = ByteArray(SIZE) { 0xff.toByte() }
        write(bytes, byteOffset, value)
        expect(value, "written at offset $byteOffset: ${bytes.toHex()}") { read(bytes, byteOffset) }
        val clobbered = (0 until SIZE)
            .filter { it < byteOffset || it >= byteOffset + width }
            .filter { bytes[it] != 0xff.toByte() }
        expect(emptyList(), "writing at offset $byteOffset overwrote bytes outside the window: ${bytes.toHex()}") { clobbered }
    }
}

/**
 * Exercises the accessors at every valid offset, aligned and unaligned alike, including the
 * first and the last one that fits.
 */
class OffsetsTest {
    @Nested inner class Byte_ {
        @Test fun signed() = expectAtEveryOffset(1, 0x7f.toByte(), { b, o, v -> b.setByte(o, v) }, { b, o -> b.getByte(o) })
        @Test fun unsigned() = expectAtEveryOffset(1, 0xfe.toUByte(), { b, o, v -> b.setUByte(o, v) }, { b, o -> b.getUByte(o) })
    }
    @Nested inner class Short_ {
        @Test fun big() = expectAtEveryOffset(2, 0x0102.toShort(), { b, o, v -> b.setShort(o, v) }, { b, o -> b.getShort(o) })
        @Test fun little() = expectAtEveryOffset(2, 0x0102.toShort(), { b, o, v -> b.setShort(o, v, Endian.Little) }, { b, o -> b.getShort(o, Endian.Little) })
    }
    @Nested inner class UShort_ {
        @Test fun big() = expectAtEveryOffset(2, 0xdead.toUShort(), { b, o, v -> b.setUShort(o, v) }, { b, o -> b.getUShort(o) })
        @Test fun little() = expectAtEveryOffset(2, 0xdead.toUShort(), { b, o, v -> b.setUShort(o, v, Endian.Little) }, { b, o -> b.getUShort(o, Endian.Little) })
    }
    @Nested inner class Int_ {
        @Test fun big() = expectAtEveryOffset(4, 0x01020304, { b, o, v -> b.setInt(o, v) }, { b, o -> b.getInt(o) })
        @Test fun little() = expectAtEveryOffset(4, 0x01020304, { b, o, v -> b.setInt(o, v, Endian.Little) }, { b, o -> b.getInt(o, Endian.Little) })
    }
    @Nested inner class UInt_ {
        @Test fun big() = expectAtEveryOffset(4, 0xdeadbeefu, { b, o, v -> b.setUInt(o, v) }, { b, o -> b.getUInt(o) })
        @Test fun little() = expectAtEveryOffset(4, 0xdeadbeefu, { b, o, v -> b.setUInt(o, v, Endian.Little) }, { b, o -> b.getUInt(o, Endian.Little) })
    }
    @Nested inner class Long_ {
        @Test fun big() = expectAtEveryOffset(8, 0x0102030405060708L, { b, o, v -> b.setLong(o, v) }, { b, o -> b.getLong(o) })
        @Test fun little() = expectAtEveryOffset(8, 0x0102030405060708L, { b, o, v -> b.setLong(o, v, Endian.Little) }, { b, o -> b.getLong(o, Endian.Little) })
    }
    @Nested inner class ULong_ {
        @Test fun big() = expectAtEveryOffset(8, deadbeefaabbccdd, { b, o, v -> b.setULong(o, v) }, { b, o -> b.getULong(o) })
        @Test fun little() = expectAtEveryOffset(8, deadbeefaabbccdd, { b, o, v -> b.setULong(o, v, Endian.Little) }, { b, o -> b.getULong(o, Endian.Little) })
    }
}

// workaround for https://youtrack.jetbrains.com/issue/KT-4749
private val deadbeefaabbccdd = "deadbeefaabbccdd".toULong(16)
