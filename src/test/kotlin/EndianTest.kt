package com.github.mvysny.unsigned

import com.github.mvysny.dynatest.DynaTest
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

class EndianTest : DynaTest({
    group("Big") {
        val e = Endian.Big
        group("getShort()") {
            test("0") {
                expect(0) { e.getShort("00000000".fromHex(), 1) }
            }
            test("0x0102") {
                expect(0x0102.toShort()) { e.getShort("00010200".fromHex(), 1) }
            }
            test("0xdead") {
                expect(0xdead.toShort()) { e.getShort("00dead00".fromHex(), 1) }
            }
        }
        group("setShort(Int)") {
            test("0") {
                expect4("00000000") { e.setShort(it, 1, 0) }
            }
            test("0x0102") {
                expect4("00010200") { e.setShort(it, 1, 0x0102) }
            }
            test("0xdead") {
                expect4("00dead00") { e.setShort(it, 1, 0xdead) }
            }
            test("0xdeadbeef") {
                expect4("00beef00") { e.setShort(it, 1, 0xdeadbeef.toInt()) }
            }
        }
        group("setShort(Short)") {
            test("0") {
                expect4("00000000") { e.setShort(it, 1, 0.toShort()) }
            }
            test("0x0102") {
                expect4("00010200") { e.setShort(it, 1, 0x0102.toShort()) }
            }
            test("0xdead") {
                expect4("00dead00") { e.setShort(it, 1, 0xdead.toShort()) }
            }
        }
        group("getUShort()") {
            test("0") {
                expect(0.toUShort()) { e.getUShort("00000000".fromHex(), 1) }
            }
            test("0x0102") {
                expect(0x0102.toUShort()) { e.getUShort("00010200".fromHex(), 1) }
            }
            test("0xdead") {
                expect(0xdead.toUShort()) { e.getUShort("00dead00".fromHex(), 1) }
            }
        }
        group("setUShort(UInt)") {
            test("0") {
                expect4("00000000") { e.setUShort(it, 1, 0.toUInt()) }
            }
            test("0x0102") {
                expect4("00010200") { e.setUShort(it, 1, 0x0102.toUInt()) }
            }
            test("0xdead") {
                expect4("00dead00") { e.setUShort(it, 1, 0xdead.toUInt()) }
            }
            test("0xdeadbeef") {
                expect4("00beef00") { e.setUShort(it, 1, 0xdeadbeef.toUInt()) }
            }
        }
        group("setUShort(UShort)") {
            test("0") {
                expect4("00000000") { e.setUShort(it, 1, 0.toUShort()) }
            }
            test("0x0102") {
                expect4("00010200") { e.setUShort(it, 1, 0x0102.toUShort()) }
            }
            test("0xdead") {
                expect4("00dead00") { e.setUShort(it, 1, 0xdead.toUShort()) }
            }
        }
        group("getInt()") {
            test("0") {
                expect(0) { e.getInt("000000000000".fromHex(), 1) }
            }
            test("0x01020304") {
                expect(0x01020304) { e.getInt("000102030400".fromHex(), 1) }
            }
            test("0xdeadbeef") {
                expect(0xdeadbeef.toInt()) { e.getInt("00deadbeef".fromHex(), 1) }
            }
        }
        group("setInt()") {
            test("0") {
                expect6("000000000000") { e.setInt(it, 1, 0) }
            }
            test("0x01020304") {
                expect6("000102030400") { e.setInt(it, 1, 0x01020304) }
            }
            test("0xdeadbeef") {
                expect6("00deadbeef00") { e.setInt(it, 1, 0xdeadbeef.toInt()) }
            }
        }
        group("getUInt()") {
            test("0") {
                expect(0.toUInt()) { e.getUInt("000000000000".fromHex(), 1) }
            }
            test("0x01020304") {
                expect(0x01020304.toUInt()) { e.getUInt("000102030400".fromHex(), 1) }
            }
            test("0xdeadbeef") {
                expect(0xdeadbeef.toUInt()) { e.getUInt("00deadbeef".fromHex(), 1) }
            }
        }
        group("setUInt()") {
            test("0") {
                expect6("000000000000") { e.setUInt(it, 1, 0.toUInt()) }
            }
            test("0x01020304") {
                expect6("000102030400") { e.setUInt(it, 1, 0x01020304.toUInt()) }
            }
            test("0xdeadbeef") {
                expect6("00deadbeef00") { e.setUInt(it, 1, 0xdeadbeef.toUInt()) }
            }
        }
        group("getLong()") {
            test("0") {
                expect(0) { e.getLong("00000000000000000000".fromHex(), 1) }
            }
            test("0x0102030405060708") {
                expect(0x0102030405060708) { e.getLong("00010203040506070800".fromHex(), 1) }
            }
            test("0xdeadbeefaabbccdd") {
                expect(deadbeefaabbccdd.toLong()) { e.getLong("00deadbeefaabbccdd".fromHex(), 1) }
            }
        }
        group("setLong()") {
            test("0") {
                expect10("00000000000000000000") { e.setLong(it, 1, 0) }
            }
            test("0x0102030405060708") {
                expect10("00010203040506070800") { e.setLong(it, 1, 0x0102030405060708L) }
            }
            test("0xdeadbeefaabbccdd") {
                expect10("00deadbeefaabbccdd00") { e.setLong(it, 1, deadbeefaabbccdd.toLong()) }
            }
        }
        group("getULong()") {
            test("0") {
                expect(0.toULong()) { e.getULong("00000000000000000000".fromHex(), 1) }
            }
            test("0x0102030405060708") {
                expect(0x0102030405060708.toULong()) { e.getULong("00010203040506070800".fromHex(), 1) }
            }
            test("0xdeadbeefaabbccdd") {
                expect(deadbeefaabbccdd) { e.getULong("00deadbeefaabbccdd".fromHex(), 1) }
            }
        }
    }
    group("Little") {
        val e = Endian.Little
        group("getShort()") {
            test("0") {
                expect(0) { e.getShort("00000000".fromHex(), 1) }
            }
            test("0x0102") {
                expect(0x0201.toShort()) { e.getShort("00010200".fromHex(), 1) }
            }
            test("0xdead") {
                expect(0xadde.toShort()) { e.getShort("00dead00".fromHex(), 1) }
            }
        }
        group("setShort(Int)") {
            test("0") {
                expect4("00000000") { e.setShort(it, 1, 0) }
            }
            test("0x0102") {
                expect4("00020100") { e.setShort(it, 1, 0x0102) }
            }
            test("0xdead") {
                expect4("00adde00") { e.setShort(it, 1, 0xdead) }
            }
            test("0xdeadbeef") {
                expect4("00efbe00") { e.setShort(it, 1, 0xdeadbeef.toInt()) }
            }
        }
        group("setShort(Short)") {
            test("0") {
                expect4("00000000") { e.setShort(it, 1, 0.toShort()) }
            }
            test("0x0102") {
                expect4("00020100") { e.setShort(it, 1, 0x0102.toShort()) }
            }
            test("0xdead") {
                expect4("00adde00") { e.setShort(it, 1, 0xdead.toShort()) }
            }
        }
        group("getUShort()") {
            test("0") {
                expect(0.toUShort()) { e.getUShort("00000000".fromHex(), 1) }
            }
            test("0x0102") {
                expect(0x0201.toUShort()) { e.getUShort("00010200".fromHex(), 1) }
            }
            test("0xdead") {
                expect(0xadde.toUShort()) { e.getUShort("00dead00".fromHex(), 1) }
            }
        }
        group("setUShort(UInt)") {
            test("0") {
                expect4("00000000") { e.setUShort(it, 1, 0.toUInt()) }
            }
            test("0x0102") {
                expect4("00020100") { e.setUShort(it, 1, 0x0102.toUInt()) }
            }
            test("0xdead") {
                expect4("00adde00") { e.setUShort(it, 1, 0xdead.toUInt()) }
            }
            test("0xdeadbeef") {
                expect4("00efbe00") { e.setUShort(it, 1, 0xdeadbeef.toUInt()) }
            }
        }
        group("setUShort(UShort)") {
            test("0") {
                expect4("00000000") { e.setUShort(it, 1, 0.toUShort()) }
            }
            test("0x0102") {
                expect4("00020100") { e.setUShort(it, 1, 0x0102.toUShort()) }
            }
            test("0xdead") {
                expect4("00adde00") { e.setUShort(it, 1, 0xdead.toUShort()) }
            }
        }
        group("getInt()") {
            test("0") {
                expect(0) { e.getInt("000000000000".fromHex(), 1) }
            }
            test("0x01020304") {
                expect(0x04030201) { e.getInt("000102030400".fromHex(), 1) }
            }
            test("0xdeadbeef") {
                expect(0xefbeadde.toInt()) { e.getInt("00deadbeef".fromHex(), 1) }
            }
        }
        group("setInt()") {
            test("0") {
                expect6("000000000000") { e.setInt(it, 1, 0) }
            }
            test("0x01020304") {
                expect6("000403020100") { e.setInt(it, 1, 0x01020304) }
            }
            test("0xdeadbeef") {
                expect6("00efbeadde00") { e.setInt(it, 1, 0xdeadbeef.toInt()) }
            }
        }
        group("getUInt()") {
            test("0") {
                expect(0.toUInt()) { e.getUInt("000000000000".fromHex(), 1) }
            }
            test("0x01020304") {
                expect(0x04030201.toUInt()) { e.getUInt("000102030400".fromHex(), 1) }
            }
            test("0xdeadbeef") {
                expect(0xefbeadde.toUInt()) { e.getUInt("00deadbeef".fromHex(), 1) }
            }
        }
        group("setUInt()") {
            test("0") {
                expect6("000000000000") { e.setUInt(it, 1, 0.toUInt()) }
            }
            test("0x01020304") {
                expect6("000403020100") { e.setUInt(it, 1, 0x01020304.toUInt()) }
            }
            test("0xdeadbeef") {
                expect6("00efbeadde00") { e.setUInt(it, 1, 0xdeadbeef.toUInt()) }
            }
        }
        group("getLong()") {
            test("0") {
                expect(0) { e.getLong("00000000000000000000".fromHex(), 1) }
            }
            test("0x0102030405060708") {
                expect(0x0807060504030201) { e.getLong("00010203040506070800".fromHex(), 1) }
            }
            test("0xdeadbeefaabbccdd") {
                expect(ddccbbaaefbeadde.toLong()) { e.getLong("00deadbeefaabbccdd".fromHex(), 1) }
            }
        }
        group("setLong()") {
            test("0") {
                expect10("00000000000000000000") { e.setLong(it, 1, 0) }
            }
            test("0x0102030405060708") {
                expect10("00080706050403020100") { e.setLong(it, 1, 0x0102030405060708L) }
            }
            test("0xdeadbeefaabbccdd") {
                expect10("00ddccbbaaefbeadde00") { e.setLong(it, 1, deadbeefaabbccdd.toLong()) }
            }
        }
        group("getULong()") {
            test("0") {
                expect(0.toULong()) { e.getULong("00000000000000000000".fromHex(), 1) }
            }
            test("0x0102030405060708") {
                expect(0x0807060504030201.toULong()) { e.getULong("00010203040506070800".fromHex(), 1) }
            }
            test("0xdeadbeefaabbccdd") {
                expect(ddccbbaaefbeadde) { e.getULong("00deadbeefaabbccdd".fromHex(), 1) }
            }
        }
    }
})

// workaround for https://youtrack.jetbrains.com/issue/KT-4749
private val deadbeefaabbccdd = "deadbeefaabbccdd".toULong(16)
private val ddccbbaaefbeadde = "ddccbbaaefbeadde".toULong(16)
