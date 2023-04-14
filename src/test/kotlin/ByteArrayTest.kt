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

            setShort(1, 0)
            setShort(1, 0, Endian.Little)
            setShort(1, 0.toShort())
            setShort(1, 0.toShort(), Endian.Little)
            setUShort(1, 0.toUInt())
            setUShort(1, 0.toUInt(), Endian.Little)
            setUShort(1, 0.toUShort())
            setUShort(1, 0.toUShort(), Endian.Little)
        }
    }
})
