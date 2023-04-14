package com.github.mvysny.unsigned

import com.github.mvysny.dynatest.DynaTest
import kotlin.test.expect

fun expect4(expectedHex: String, block: (ByteArray) -> Unit) {
    val bytes = ByteArray(4)
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
    }
})
