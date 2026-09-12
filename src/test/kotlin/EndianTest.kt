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
        @Nested inner class getFloat {
            @Test fun one() {
                expect(1f) { e.getFloat("003f80000000".fromHex(), 1) }
            }
            @Test fun minusTwo() {
                expect(-2f) { e.getFloat("00c000000000".fromHex(), 1) }
            }
            @Test fun minusZero() {
                expect(-0f) { e.getFloat("008000000000".fromHex(), 1) }
            }
            @Test fun minValue() {
                expect(Float.MIN_VALUE) { e.getFloat("000000000100".fromHex(), 1) }
            }
            @Test fun infinity() {
                expect(Float.POSITIVE_INFINITY) { e.getFloat("007f80000000".fromHex(), 1) }
            }
            @Test fun nan() {
                expect(CANONICAL_NAN_32) { e.getFloat("007fc0000000".fromHex(), 1).toRawBits() }
            }
        }
        @Nested inner class setFloat {
            @Test fun one() {
                expect6("003f80000000") { e.setFloat(it, 1, 1f) }
            }
            @Test fun minusTwo() {
                expect6("00c000000000") { e.setFloat(it, 1, -2f) }
            }
            @Test fun minusZero() {
                expect6("008000000000") { e.setFloat(it, 1, -0f) }
            }
            @Test fun minValue() {
                expect6("000000000100") { e.setFloat(it, 1, Float.MIN_VALUE) }
            }
            @Test fun infinity() {
                expect6("007f80000000") { e.setFloat(it, 1, Float.POSITIVE_INFINITY) }
            }
            @Test fun nan() {
                expect6("007fc0000000") { e.setFloat(it, 1, Float.NaN) }
            }
        }
        @Nested inner class getDouble {
            @Test fun one() {
                expect(1.0) { e.getDouble("003ff000000000000000".fromHex(), 1) }
            }
            @Test fun minusZero() {
                expect(-0.0) { e.getDouble("00800000000000000000".fromHex(), 1) }
            }
            @Test fun pi() {
                expect(Math.PI) { e.getDouble("00400921fb54442d1800".fromHex(), 1) }
            }
            @Test fun minValue() {
                expect(Double.MIN_VALUE) { e.getDouble("00000000000000000100".fromHex(), 1) }
            }
            @Test fun infinity() {
                expect(Double.POSITIVE_INFINITY) { e.getDouble("007ff000000000000000".fromHex(), 1) }
            }
            @Test fun negativeInfinity() {
                expect(Double.NEGATIVE_INFINITY) { e.getDouble("00fff000000000000000".fromHex(), 1) }
            }
            @Test fun nan() {
                expect(CANONICAL_NAN_64) { e.getDouble("007ff800000000000000".fromHex(), 1).toRawBits() }
            }
        }
        @Nested inner class setDouble {
            @Test fun one() {
                expect10("003ff000000000000000") { e.setDouble(it, 1, 1.0) }
            }
            @Test fun minusZero() {
                expect10("00800000000000000000") { e.setDouble(it, 1, -0.0) }
            }
            @Test fun pi() {
                expect10("00400921fb54442d1800") { e.setDouble(it, 1, Math.PI) }
            }
            @Test fun minValue() {
                expect10("00000000000000000100") { e.setDouble(it, 1, Double.MIN_VALUE) }
            }
            @Test fun infinity() {
                expect10("007ff000000000000000") { e.setDouble(it, 1, Double.POSITIVE_INFINITY) }
            }
            @Test fun negativeInfinity() {
                expect10("00fff000000000000000") { e.setDouble(it, 1, Double.NEGATIVE_INFINITY) }
            }
            @Test fun nan() {
                expect10("007ff800000000000000") { e.setDouble(it, 1, Double.NaN) }
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
        @Nested inner class getFloat {
            @Test fun one() {
                expect(1f) { e.getFloat("000000803f00".fromHex(), 1) }
            }
            @Test fun minusTwo() {
                expect(-2f) { e.getFloat("00000000c000".fromHex(), 1) }
            }
            @Test fun minusZero() {
                expect(-0f) { e.getFloat("000000008000".fromHex(), 1) }
            }
            @Test fun minValue() {
                expect(Float.MIN_VALUE) { e.getFloat("000100000000".fromHex(), 1) }
            }
            @Test fun infinity() {
                expect(Float.POSITIVE_INFINITY) { e.getFloat("000000807f00".fromHex(), 1) }
            }
            @Test fun nan() {
                expect(CANONICAL_NAN_32) { e.getFloat("000000c07f00".fromHex(), 1).toRawBits() }
            }
        }
        @Nested inner class setFloat {
            @Test fun one() {
                expect6("000000803f00") { e.setFloat(it, 1, 1f) }
            }
            @Test fun minusTwo() {
                expect6("00000000c000") { e.setFloat(it, 1, -2f) }
            }
            @Test fun minusZero() {
                expect6("000000008000") { e.setFloat(it, 1, -0f) }
            }
            @Test fun minValue() {
                expect6("000100000000") { e.setFloat(it, 1, Float.MIN_VALUE) }
            }
            @Test fun infinity() {
                expect6("000000807f00") { e.setFloat(it, 1, Float.POSITIVE_INFINITY) }
            }
            @Test fun nan() {
                expect6("000000c07f00") { e.setFloat(it, 1, Float.NaN) }
            }
        }
        @Nested inner class getDouble {
            @Test fun one() {
                expect(1.0) { e.getDouble("00000000000000f03f00".fromHex(), 1) }
            }
            @Test fun minusZero() {
                expect(-0.0) { e.getDouble("00000000000000008000".fromHex(), 1) }
            }
            @Test fun pi() {
                expect(Math.PI) { e.getDouble("00182d4454fb21094000".fromHex(), 1) }
            }
            @Test fun minValue() {
                expect(Double.MIN_VALUE) { e.getDouble("00010000000000000000".fromHex(), 1) }
            }
            @Test fun infinity() {
                expect(Double.POSITIVE_INFINITY) { e.getDouble("00000000000000f07f00".fromHex(), 1) }
            }
            @Test fun negativeInfinity() {
                expect(Double.NEGATIVE_INFINITY) { e.getDouble("00000000000000f0ff00".fromHex(), 1) }
            }
            @Test fun nan() {
                expect(CANONICAL_NAN_64) { e.getDouble("00000000000000f87f00".fromHex(), 1).toRawBits() }
            }
        }
        @Nested inner class setDouble {
            @Test fun one() {
                expect10("00000000000000f03f00") { e.setDouble(it, 1, 1.0) }
            }
            @Test fun minusZero() {
                expect10("00000000000000008000") { e.setDouble(it, 1, -0.0) }
            }
            @Test fun pi() {
                expect10("00182d4454fb21094000") { e.setDouble(it, 1, Math.PI) }
            }
            @Test fun minValue() {
                expect10("00010000000000000000") { e.setDouble(it, 1, Double.MIN_VALUE) }
            }
            @Test fun infinity() {
                expect10("00000000000000f07f00") { e.setDouble(it, 1, Double.POSITIVE_INFINITY) }
            }
            @Test fun negativeInfinity() {
                expect10("00000000000000f0ff00") { e.setDouble(it, 1, Double.NEGATIVE_INFINITY) }
            }
            @Test fun nan() {
                expect10("00000000000000f87f00") { e.setDouble(it, 1, Double.NaN) }
            }
        }
    }

    /**
     * The float accessors never canonicalize: [Endian.setFloat] goes through [Float.toRawBits], not
     * [Float.toBits]. That is enforced by the source rather than by a test — a NaN payload can only
     * be *produced* through [Float.Companion.fromBits], whose own javadoc declines to guarantee that
     * `fromBits(x).toRawBits() == x`, so any assertion here is testing the platform, not the library.
     *
     * These probes are kept because a platform that mangles a *quiet* NaN payload is worth finding
     * out about. If one ever goes red on a new JDK or OS, delete it — do not "fix" the accessors.
     */
    @Nested inner class NaNPayloadIsAPlatformProperty {
        @Test fun float() {
            val payload = Float.fromBits(PAYLOAD_NAN_32)
            expect6("007fc0dead00") { Endian.Big.setFloat(it, 1, payload) }
            expect(PAYLOAD_NAN_32) { Endian.Big.getFloat("007fc0dead00".fromHex(), 1).toRawBits() }
        }
        @Test fun double() {
            val payload = Double.fromBits(PAYLOAD_NAN_64)
            expect10("007ff8deadbeefcafe00") { Endian.Big.setDouble(it, 1, payload) }
            expect(PAYLOAD_NAN_64) { Endian.Big.getDouble("007ff8deadbeefcafe00".fromHex(), 1).toRawBits() }
        }
    }
}

// workaround for https://youtrack.jetbrains.com/issue/KT-4749
private val deadbeefaabbccdd = "deadbeefaabbccdd".toULong(16)
private val ddccbbaaefbeadde = "ddccbbaaefbeadde".toULong(16)

// `expect` boxes, and java.lang.Float.equals compares floatToIntBits — which canonicalizes NaN, so
// `expect(Float.NaN) { … }` passes even when the payload was mangled. Assert NaNs via toRawBits().
private const val CANONICAL_NAN_32: Int = 0x7fc00000
private const val CANONICAL_NAN_64: Long = 0x7ff8000000000000
private const val PAYLOAD_NAN_32: Int = 0x7fc0dead
private const val PAYLOAD_NAN_64: Long = 0x7ff8deadbeefcafe
