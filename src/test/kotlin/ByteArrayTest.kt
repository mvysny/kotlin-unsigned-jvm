package com.github.mvysny.unsigned

import org.junit.jupiter.api.Test

class ByteArrayTest {
    @Test fun api() {
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

            getUInt(1)
            getUInt(1, Endian.Little)
            setUInt(1, 0x01020304.toUInt())
            setUInt(1, 0x01020304.toUInt(), Endian.Little)

            getLong(1)
            getLong(1, Endian.Little)
            setLong(1, 0)
            setLong(1, 0, Endian.Little)

            getULong(1)
            getULong(1, Endian.Little)
            setULong(1, 0.toULong())
            setULong(1, 0.toULong(), Endian.Little)
        }
    }
}
