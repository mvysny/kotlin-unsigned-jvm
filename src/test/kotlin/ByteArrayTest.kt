package com.github.mvysny.unsigned

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

/**
 * Runs [block] on a 10-byte array pre-filled with 0xff, then expects the array to read as
 * [expectedHex]. The 0xff fill (rather than zeros) makes a write that touches one byte too
 * many visible.
 */
private fun expectBytes(expectedHex: String, block: (ByteArray) -> Unit) {
    val bytes = ByteArray(10) { 0xff.toByte() }
    block(bytes)
    expect(expectedHex) { bytes.toHex() }
}

/**
 * Tests the [ByteArray] extension API. Every function is a one-line delegate to [Endian] (which
 * [EndianTest] covers exhaustively), so these tests target what delegating can get wrong: reaching
 * the right [Endian] function, and defaulting to [Endian.Big] when no endianness is passed.
 */
class ByteArrayTest {
    @Nested inner class GetByte {
        @Test fun `0x7f`() {
            expect(0x7f.toByte()) { "ff7fff".fromHex().getByte(1) }
        }
        @Test fun `0xff is negative`() {
            expect((-1).toByte()) { "00ff00".fromHex().getByte(1) }
        }
    }
    @Nested inner class SetByteByte {
        @Test fun `0x7f`() {
            expectBytes("ff7fffffffffffffffff") { it.setByte(1, 0x7f.toByte()) }
        }
        @Test fun `0x00`() {
            expectBytes("ff00ffffffffffffffff") { it.setByte(1, 0.toByte()) }
        }
    }
    @Nested inner class SetByteInt {
        @Test fun `0x7f`() {
            expectBytes("ff7fffffffffffffffff") { it.setByte(1, 0x7f) }
        }
        @Test fun `0x1234 keeps the low byte only`() {
            expectBytes("ff34ffffffffffffffff") { it.setByte(1, 0x1234) }
        }
    }
    @Nested inner class GetUByte {
        @Test fun `0x7f`() {
            expect(0x7f.toUByte()) { "ff7fff".fromHex().getUByte(1) }
        }
        @Test fun `0xff`() {
            expect(0xff.toUByte()) { "00ff00".fromHex().getUByte(1) }
        }
    }
    @Nested inner class SetUByteUByte {
        @Test fun `0xfe`() {
            expectBytes("fffeffffffffffffffff") { it.setUByte(1, 0xfe.toUByte()) }
        }
        @Test fun `0x00`() {
            expectBytes("ff00ffffffffffffffff") { it.setUByte(1, 0.toUByte()) }
        }
    }
    @Nested inner class SetUByteUInt {
        @Test fun `0xfe`() {
            expectBytes("fffeffffffffffffffff") { it.setUByte(1, 0xfeu) }
        }
        @Test fun `0x1234 keeps the low byte only`() {
            expectBytes("ff34ffffffffffffffff") { it.setUByte(1, 0x1234u) }
        }
    }

    @Nested inner class GetShort {
        @Test fun `big is the default`() {
            expect(0x0102.toShort()) { "ff0102ff".fromHex().getShort(1) }
        }
        @Test fun little() {
            expect(0x0201.toShort()) { "ff0102ff".fromHex().getShort(1, Endian.Little) }
        }
    }
    @Nested inner class SetShortShort {
        @Test fun `big is the default`() {
            expectBytes("ff0102ffffffffffffff") { it.setShort(1, 0x0102.toShort()) }
        }
        @Test fun little() {
            expectBytes("ff0201ffffffffffffff") { it.setShort(1, 0x0102.toShort(), Endian.Little) }
        }
    }
    @Nested inner class SetShortInt {
        @Test fun `big is the default`() {
            expectBytes("ff0102ffffffffffffff") { it.setShort(1, 0x0102) }
        }
        @Test fun little() {
            expectBytes("ff0201ffffffffffffff") { it.setShort(1, 0x0102, Endian.Little) }
        }
        @Test fun `0xdeadbeef keeps the low 16 bits only`() {
            expectBytes("ffbeefffffffffffffff") { it.setShort(1, 0xdeadbeef.toInt()) }
        }
    }
    @Nested inner class GetUShort {
        @Test fun `big is the default`() {
            expect(0xdeadu.toUShort()) { "ffdeadff".fromHex().getUShort(1) }
        }
        @Test fun little() {
            expect(0xaddeu.toUShort()) { "ffdeadff".fromHex().getUShort(1, Endian.Little) }
        }
    }
    @Nested inner class SetUShortUShort {
        @Test fun `big is the default`() {
            expectBytes("ffdeadffffffffffffff") { it.setUShort(1, 0xdead.toUShort()) }
        }
        @Test fun little() {
            expectBytes("ffaddeffffffffffffff") { it.setUShort(1, 0xdead.toUShort(), Endian.Little) }
        }
    }
    @Nested inner class SetUShortUInt {
        @Test fun `big is the default`() {
            expectBytes("ffdeadffffffffffffff") { it.setUShort(1, 0xdeadu) }
        }
        @Test fun little() {
            expectBytes("ffaddeffffffffffffff") { it.setUShort(1, 0xdeadu, Endian.Little) }
        }
        @Test fun `0xdeadbeef keeps the low 16 bits only`() {
            expectBytes("ffbeefffffffffffffff") { it.setUShort(1, 0xdeadbeefu) }
        }
    }

    @Nested inner class GetInt {
        @Test fun `big is the default`() {
            expect(0x01020304) { "ff01020304ff".fromHex().getInt(1) }
        }
        @Test fun little() {
            expect(0x04030201) { "ff01020304ff".fromHex().getInt(1, Endian.Little) }
        }
    }
    @Nested inner class SetInt {
        @Test fun `big is the default`() {
            expectBytes("ff01020304ffffffffff") { it.setInt(1, 0x01020304) }
        }
        @Test fun little() {
            expectBytes("ff04030201ffffffffff") { it.setInt(1, 0x01020304, Endian.Little) }
        }
    }
    @Nested inner class GetUInt {
        @Test fun `big is the default`() {
            expect(0xdeadbeefu) { "ffdeadbeefff".fromHex().getUInt(1) }
        }
        @Test fun little() {
            expect(0xefbeaddeu) { "ffdeadbeefff".fromHex().getUInt(1, Endian.Little) }
        }
    }
    @Nested inner class SetUInt {
        @Test fun `big is the default`() {
            expectBytes("ffdeadbeefffffffffff") { it.setUInt(1, 0xdeadbeefu) }
        }
        @Test fun little() {
            expectBytes("ffefbeaddeffffffffff") { it.setUInt(1, 0xdeadbeefu, Endian.Little) }
        }
    }

    @Nested inner class GetLong {
        @Test fun `big is the default`() {
            expect(0x0102030405060708L) { "ff0102030405060708ff".fromHex().getLong(1) }
        }
        @Test fun little() {
            expect(0x0807060504030201L) { "ff0102030405060708ff".fromHex().getLong(1, Endian.Little) }
        }
    }
    @Nested inner class SetLong {
        @Test fun `big is the default`() {
            expectBytes("ff0102030405060708ff") { it.setLong(1, 0x0102030405060708L) }
        }
        @Test fun little() {
            expectBytes("ff0807060504030201ff") { it.setLong(1, 0x0102030405060708L, Endian.Little) }
        }
    }
    @Nested inner class GetULong {
        @Test fun `big is the default`() {
            expect(deadbeefaabbccdd) { "ffdeadbeefaabbccddff".fromHex().getULong(1) }
        }
        @Test fun little() {
            expect(ddccbbaaefbeadde) { "ffdeadbeefaabbccddff".fromHex().getULong(1, Endian.Little) }
        }
    }
    @Nested inner class SetULong {
        @Test fun `big is the default`() {
            expectBytes("ffdeadbeefaabbccddff") { it.setULong(1, deadbeefaabbccdd) }
        }
        @Test fun little() {
            expectBytes("ffddccbbaaefbeaddeff") { it.setULong(1, deadbeefaabbccdd, Endian.Little) }
        }
    }
    @Nested inner class GetFloat {
        @Test fun `big is the default`() {
            expect(-2f) { "ffc0000000ffffffffff".fromHex().getFloat(1) }
        }
        @Test fun little() {
            expect(-2f) { "ff000000c0ffffffffff".fromHex().getFloat(1, Endian.Little) }
        }
    }
    @Nested inner class SetFloat {
        @Test fun `big is the default`() {
            expectBytes("ffc0000000ffffffffff") { it.setFloat(1, -2f) }
        }
        @Test fun little() {
            expectBytes("ff000000c0ffffffffff") { it.setFloat(1, -2f, Endian.Little) }
        }
    }
    @Nested inner class GetDouble {
        @Test fun `big is the default`() {
            expect(Math.PI) { "ff400921fb54442d18ff".fromHex().getDouble(1) }
        }
        @Test fun little() {
            expect(Math.PI) { "ff182d4454fb210940ff".fromHex().getDouble(1, Endian.Little) }
        }
    }
    @Nested inner class SetDouble {
        @Test fun `big is the default`() {
            expectBytes("ff400921fb54442d18ff") { it.setDouble(1, Math.PI) }
        }
        @Test fun little() {
            expectBytes("ff182d4454fb210940ff") { it.setDouble(1, Math.PI, Endian.Little) }
        }
    }
}

// workaround for https://youtrack.jetbrains.com/issue/KT-4749
private val deadbeefaabbccdd = "deadbeefaabbccdd".toULong(16)
private val ddccbbaaefbeadde = "ddccbbaaefbeadde".toULong(16)
