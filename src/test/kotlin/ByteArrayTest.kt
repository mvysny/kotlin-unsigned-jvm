package com.github.mvysny.unsigned

import com.github.mvysny.dynatest.DynaTest

class ByteArrayTest : DynaTest({
    test("api") {
        ByteArray(100).apply {
            getByte(0)
            setByte(0, 0)
            setByte(0, 0.toByte())
            getUByte(0)
            setUByte(0, 0.toUByte())
            setUByte(0, 0.toUInt())

            getShort(1)
            getShort(1, Endian.Little)
            setShort(1, 0)
            setShort(1, 0, Endian.Little)
            setShort(1, 0.toShort())
            setShort(1, 0.toShort(), Endian.Little)
            getUShort(1)
            getUShort(1, Endian.Little)
            setUShort(1, 0.toUInt())
            setUShort(1, 0.toUInt(), Endian.Little)
            setUShort(1, 0.toUShort())
            setUShort(1, 0.toUShort(), Endian.Little)

            getInt(1)
            getInt(1, Endian.Little)
            setInt(1, 0x01020304)
            setInt(1, 0x01020304, Endian.Little)
        }
    }
})
