package com.github.mvysny.unsigned

/**
 * Describes endianness to be used when accessing or updating a sequence of bytes.
 */
public enum class Endian {
    Big {
        override fun setShort(bytes: ByteArray, byteOffset: Int, value: Int) {
            bytes[byteOffset] = (value ushr 8).toByte()
            bytes[byteOffset + 1] = value.toByte()
        }
    },
    Little {
        override fun setShort(bytes: ByteArray, byteOffset: Int, value: Int) {
            bytes[byteOffset + 1] = (value ushr 8).toByte()
            bytes[byteOffset] = value.toByte()
        }
    };

    /**
     * Sets the two bytes starting at the specified [byteOffset] in this
     * object to the two's complement binary representation of the specified
     * [value].
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 2` must be less than or equal to the length of [bytes].
     */
    public fun setShort(bytes: ByteArray, byteOffset: Int, value: Short) {
        setShort(bytes, byteOffset, value.toInt())
    }

    /**
     * Sets the two bytes starting at the specified [byteOffset] in this
     * object to the two's complement binary representation of the specified
     * [value]. Only the lowest 16 bits are considered, high 16 bits are ignored.
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 2` must be less than or equal to the length of [bytes].
     */
    public abstract fun setShort(bytes: ByteArray, byteOffset: Int, value: Int)

    /**
     * Sets the two bytes starting at the specified [byteOffset] in this
     * object to the two's complement binary representation of the specified
     * [value].
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 2` must be less than or equal to the length of [bytes].
     */
    public fun setUShort(bytes: ByteArray, byteOffset: Int, value: UShort) {
        setShort(bytes, byteOffset, value.toShort());
    }

    /**
     * Sets the two bytes starting at the specified [byteOffset] in this
     * object to the two's complement binary representation of the specified
     * [value]. Only the lowest 16 bits are considered, high 16 bits are ignored.
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 2` must be less than or equal to the length of [bytes].
     */
    public fun setUShort(bytes: ByteArray, byteOffset: Int, value: UInt) {
        setShort(bytes, byteOffset, value.toShort())
    }
}
