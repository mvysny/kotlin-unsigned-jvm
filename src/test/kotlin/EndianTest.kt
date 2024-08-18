package com.github.mvysny.unsigned

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

private fun expect4(expectedHex: String, block: (ByteArray) -> Unit) {
    val bytes = ByteArray(4)
    block(bytes)
    expect(expectedHex) { bytes.toHex() }
}

private fun expect6(expectedHex: String, block: (ByteArray) -> Unit) {
    val bytes = ByteArray(6)
    block(bytes)
    expect(expectedHex) { bytes.toHex() }
}

private fun expect10(expectedHex: String, block: (ByteArray) -> Unit) {
    val bytes = ByteArray(10)
    block(bytes)
    expect(expectedHex) { bytes.toHex() }
}

class EndianTest {
    @Nested inner class BigEndianTests {
        val e = Endian.Big
        @Nested inner class GetShort {
            @Test fun `0`() {
                expect(0) { e.getShort("00000000".fromHex(), 1) }
            }
            @Test fun `0x0102`() {
                expect(0x0102.toShort()) { e.getShort("00010200".fromHex(), 1) }
            }
            @Test fun `0xdead`() {
                expect(0xdead.toShort()) { e.getShort("00dead00".fromHex(), 1) }
            }
        }
        @Nested inner class SetShortInt {
            @Test fun `0`() {
                expect4("00000000") { e.setShort(it, 1, 0) }
            }
            @Test fun `0x0102`() {
                expect4("00010200") { e.setShort(it, 1, 0x0102) }
            }
            @Test fun `0xdead`() {
                expect4("00dead00") { e.setShort(it, 1, 0xdead) }
            }
            @Test fun `0xdeadbeef`() {
                expect4("00beef00") { e.setShort(it, 1, 0xdeadbeef.toInt()) }
            }
        }
        @Nested inner class SetShortShort {
            @Test fun `0`() {
                expect4("00000000") { e.setShort(it, 1, 0.toShort()) }
            }
            @Test fun `0x0102`() {
                expect4("00010200") { e.setShort(it, 1, 0x0102.toShort()) }
            }
            @Test fun `0xdead`() {
                expect4("00dead00") { e.setShort(it, 1, 0xdead.toShort()) }
            }
        }
        @Nested inner class GetUShort {
            @Test fun `0`() {
                expect(0.toUShort()) { e.getUShort("00000000".fromHex(), 1) }
            }
            @Test fun `0x0102`() {
                expect(0x0102.toUShort()) { e.getUShort("00010200".fromHex(), 1) }
            }
            @Test fun `0xdead`() {
                expect(0xdead.toUShort()) { e.getUShort("00dead00".fromHex(), 1) }
            }
        }
        @Nested inner class SetUShortUInt {
            @Test fun `0`() {
                expect4("00000000") { e.setUShort(it, 1, 0.toUInt()) }
            }
            @Test fun `0x0102`() {
                expect4("00010200") { e.setUShort(it, 1, 0x0102.toUInt()) }
            }
            @Test fun `0xdead`() {
                expect4("00dead00") { e.setUShort(it, 1, 0xdead.toUInt()) }
            }
            @Test fun `0xdeadbeef`() {
                expect4("00beef00") { e.setUShort(it, 1, 0xdeadbeef.toUInt()) }
            }
        }
        @Nested inner class SetUShortUShort {
            @Test fun `0`() {
                expect4("00000000") { e.setUShort(it, 1, 0.toUShort()) }
            }
            @Test fun `0x0102`() {
                expect4("00010200") { e.setUShort(it, 1, 0x0102.toUShort()) }
            }
            @Test fun `0xdead`() {
                expect4("00dead00") { e.setUShort(it, 1, 0xdead.toUShort()) }
            }
        }
        @Nested inner class GetInt {
            @Test fun `0`() {
                expect(0) { e.getInt("000000000000".fromHex(), 1) }
            }
            @Test fun `0x01020304`() {
                expect(0x01020304) { e.getInt("000102030400".fromHex(), 1) }
            }
            @Test fun `0xdeadbeef`() {
                expect(0xdeadbeef.toInt()) { e.getInt("00deadbeef".fromHex(), 1) }
            }
        }
        @Nested inner class SetInt {
            @Test fun `0`() {
                expect6("000000000000") { e.setInt(it, 1, 0) }
            }
            @Test fun `0x01020304`() {
                expect6("000102030400") { e.setInt(it, 1, 0x01020304) }
            }
            @Test fun `0xdeadbeef`() {
                expect6("00deadbeef00") { e.setInt(it, 1, 0xdeadbeef.toInt()) }
            }
        }
        @Nested inner class GetUInt {
            @Test fun `0`() {
                expect(0.toUInt()) { e.getUInt("000000000000".fromHex(), 1) }
            }
            @Test fun `0x01020304`() {
                expect(0x01020304.toUInt()) { e.getUInt("000102030400".fromHex(), 1) }
            }
            @Test fun `0xdeadbeef`() {
                expect(0xdeadbeef.toUInt()) { e.getUInt("00deadbeef".fromHex(), 1) }
            }
        }
        @Nested inner class SetUInt {
            @Test fun `0`() {
                expect6("000000000000") { e.setUInt(it, 1, 0.toUInt()) }
            }
            @Test fun `0x01020304`() {
                expect6("000102030400") { e.setUInt(it, 1, 0x01020304.toUInt()) }
            }
            @Test fun `0xdeadbeef`() {
                expect6("00deadbeef00") { e.setUInt(it, 1, 0xdeadbeef.toUInt()) }
            }
        }
        @Nested inner class GetLong {
            @Test fun `0`() {
                expect(0) { e.getLong("00000000000000000000".fromHex(), 1) }
            }
            @Test fun `0x0102030405060708`() {
                expect(0x0102030405060708) { e.getLong("00010203040506070800".fromHex(), 1) }
            }
            @Test fun `0xdeadbeefaabbccdd`() {
                expect(deadbeefaabbccdd.toLong()) { e.getLong("00deadbeefaabbccdd".fromHex(), 1) }
            }
        }
        @Nested inner class setLong {
            @Test fun `0`() {
                expect10("00000000000000000000") { e.setLong(it, 1, 0) }
            }
            @Test fun `0x0102030405060708`() {
                expect10("00010203040506070800") { e.setLong(it, 1, 0x0102030405060708L) }
            }
            @Test fun `0xdeadbeefaabbccdd`() {
                expect10("00deadbeefaabbccdd00") { e.setLong(it, 1, deadbeefaabbccdd.toLong()) }
            }
        }
        @Nested inner class getULong {
            @Test fun `0`() {
                expect(0.toULong()) { e.getULong("00000000000000000000".fromHex(), 1) }
            }
            @Test fun `0x0102030405060708`() {
                expect(0x0102030405060708.toULong()) { e.getULong("00010203040506070800".fromHex(), 1) }
            }
            @Test fun `0xdeadbeefaabbccdd`() {
                expect(deadbeefaabbccdd) { e.getULong("00deadbeefaabbccdd".fromHex(), 1) }
            }
        }
        @Nested inner class setULong {
            @Test fun `0`() {
                expect10("00000000000000000000") { e.setULong(it, 1, 0.toULong()) }
            }
            @Test fun `0x0102030405060708`() {
                expect10("00010203040506070800") { e.setULong(it, 1, 0x0102030405060708L.toULong()) }
            }
            @Test fun `0xdeadbeefaabbccdd`() {
                expect10("00deadbeefaabbccdd00") { e.setULong(it, 1, deadbeefaabbccdd) }
            }
        }
    }
    @Nested inner class Little {
        val e = Endian.Little
        @Nested inner class getShort {
            @Test fun `0`() {
                expect(0) { e.getShort("00000000".fromHex(), 1) }
            }
            @Test fun `0x0102`() {
                expect(0x0201.toShort()) { e.getShort("00010200".fromHex(), 1) }
            }
            @Test fun `0xdead`() {
                expect(0xadde.toShort()) { e.getShort("00dead00".fromHex(), 1) }
            }
        }
        @Nested inner class setShortInt {
            @Test fun `0`() {
                expect4("00000000") { e.setShort(it, 1, 0) }
            }
            @Test fun `0x0102`() {
                expect4("00020100") { e.setShort(it, 1, 0x0102) }
            }
            @Test fun `0xdead`() {
                expect4("00adde00") { e.setShort(it, 1, 0xdead) }
            }
            @Test fun `0xdeadbeef`() {
                expect4("00efbe00") { e.setShort(it, 1, 0xdeadbeef.toInt()) }
            }
        }
        @Nested inner class setShortShort {
            @Test fun `0`() {
                expect4("00000000") { e.setShort(it, 1, 0.toShort()) }
            }
            @Test fun `0x0102`() {
                expect4("00020100") { e.setShort(it, 1, 0x0102.toShort()) }
            }
            @Test fun `0xdead`() {
                expect4("00adde00") { e.setShort(it, 1, 0xdead.toShort()) }
            }
        }
        @Nested inner class getUShort {
            @Test fun `0`() {
                expect(0.toUShort()) { e.getUShort("00000000".fromHex(), 1) }
            }
            @Test fun `0x0102`() {
                expect(0x0201.toUShort()) { e.getUShort("00010200".fromHex(), 1) }
            }
            @Test fun `0xdead`() {
                expect(0xadde.toUShort()) { e.getUShort("00dead00".fromHex(), 1) }
            }
        }
        @Nested inner class setUShort_UInt {
            @Test fun `0`() {
                expect4("00000000") { e.setUShort(it, 1, 0.toUInt()) }
            }
            @Test fun `0x0102`() {
                expect4("00020100") { e.setUShort(it, 1, 0x0102.toUInt()) }
            }
            @Test fun `0xdead`() {
                expect4("00adde00") { e.setUShort(it, 1, 0xdead.toUInt()) }
            }
            @Test fun `0xdeadbeef`() {
                expect4("00efbe00") { e.setUShort(it, 1, 0xdeadbeef.toUInt()) }
            }
        }
        @Nested inner class setUShort_UShort {
            @Test fun `0`() {
                expect4("00000000") { e.setUShort(it, 1, 0.toUShort()) }
            }
            @Test fun `0x0102`() {
                expect4("00020100") { e.setUShort(it, 1, 0x0102.toUShort()) }
            }
            @Test fun `0xdead`() {
                expect4("00adde00") { e.setUShort(it, 1, 0xdead.toUShort()) }
            }
        }
        @Nested inner class getInt {
            @Test fun `0`() {
                expect(0) { e.getInt("000000000000".fromHex(), 1) }
            }
            @Test fun `0x01020304`() {
                expect(0x04030201) { e.getInt("000102030400".fromHex(), 1) }
            }
            @Test fun `0xdeadbeef`() {
                expect(0xefbeadde.toInt()) { e.getInt("00deadbeef".fromHex(), 1) }
            }
        }
        @Nested inner class setInt {
            @Test fun `0`() {
                expect6("000000000000") { e.setInt(it, 1, 0) }
            }
            @Test fun `0x01020304`() {
                expect6("000403020100") { e.setInt(it, 1, 0x01020304) }
            }
            @Test fun `0xdeadbeef`() {
                expect6("00efbeadde00") { e.setInt(it, 1, 0xdeadbeef.toInt()) }
            }
        }
        @Nested inner class getUInt {
            @Test fun `0`() {
                expect(0.toUInt()) { e.getUInt("000000000000".fromHex(), 1) }
            }
            @Test fun `0x01020304`() {
                expect(0x04030201.toUInt()) { e.getUInt("000102030400".fromHex(), 1) }
            }
            @Test fun `0xdeadbeef`() {
                expect(0xefbeadde.toUInt()) { e.getUInt("00deadbeef".fromHex(), 1) }
            }
        }
        @Nested inner class setUInt {
            @Test fun `0`() {
                expect6("000000000000") { e.setUInt(it, 1, 0.toUInt()) }
            }
            @Test fun `0x01020304`() {
                expect6("000403020100") { e.setUInt(it, 1, 0x01020304.toUInt()) }
            }
            @Test fun `0xdeadbeef`() {
                expect6("00efbeadde00") { e.setUInt(it, 1, 0xdeadbeef.toUInt()) }
            }
        }
        @Nested inner class getLong {
            @Test fun `0`() {
                expect(0) { e.getLong("00000000000000000000".fromHex(), 1) }
            }
            @Test fun `0x0102030405060708`() {
                expect(0x0807060504030201) { e.getLong("00010203040506070800".fromHex(), 1) }
            }
            @Test fun `0xdeadbeefaabbccdd`() {
                expect(ddccbbaaefbeadde.toLong()) { e.getLong("00deadbeefaabbccdd".fromHex(), 1) }
            }
        }
        @Nested inner class setLong {
            @Test fun `0`() {
                expect10("00000000000000000000") { e.setLong(it, 1, 0) }
            }
            @Test fun `0x0102030405060708`() {
                expect10("00080706050403020100") { e.setLong(it, 1, 0x0102030405060708L) }
            }
            @Test fun `0xdeadbeefaabbccdd`() {
                expect10("00ddccbbaaefbeadde00") { e.setLong(it, 1, deadbeefaabbccdd.toLong()) }
            }
        }
        @Nested inner class getULong {
            @Test fun `0`() {
                expect(0.toULong()) { e.getULong("00000000000000000000".fromHex(), 1) }
            }
            @Test fun `0x0102030405060708`() {
                expect(0x0807060504030201.toULong()) { e.getULong("00010203040506070800".fromHex(), 1) }
            }
            @Test fun `0xdeadbeefaabbccdd`() {
                expect(ddccbbaaefbeadde) { e.getULong("00deadbeefaabbccdd".fromHex(), 1) }
            }
        }
        @Nested inner class setULong {
            @Test fun `0`() {
                expect10("00000000000000000000") { e.setULong(it, 1, 0.toULong()) }
            }
            @Test fun `0x0102030405060708`() {
                expect10("00080706050403020100") { e.setULong(it, 1, 0x0102030405060708L.toULong()) }
            }
            @Test fun `0xdeadbeefaabbccdd`() {
                expect10("00ddccbbaaefbeadde00") { e.setULong(it, 1, deadbeefaabbccdd) }
            }
        }
    }
}

// workaround for https://youtrack.jetbrains.com/issue/KT-4749
private val deadbeefaabbccdd = "deadbeefaabbccdd".toULong(16)
private val ddccbbaaefbeadde = "ddccbbaaefbeadde".toULong(16)
