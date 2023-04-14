package com.github.mvysny.unsigned

/**
 * Describes endianness to be used when accessing or updating a sequence of bytes.
 */
public enum class Endian {
    Big {
        override fun getShort(
            bytes: ByteArray,
            byteOffset: Int
        ): Short = ((bytes[byteOffset].toUByte().toUInt() shl 8) + bytes[byteOffset + 1].toUByte()).toShort()

        override fun setShort(bytes: ByteArray, byteOffset: Int, value: Int) {
            bytes[byteOffset] = (value ushr 8).toByte()
            bytes[byteOffset + 1] = value.toByte()
        }

        override fun getInt(bytes: ByteArray, byteOffset: Int): Int = (
                (bytes[byteOffset].toUByte().toUInt() shl 24) +
                        (bytes[byteOffset + 1].toUByte().toUInt() shl 16) +
                        (bytes[byteOffset + 2].toUByte().toUInt() shl 8) +
                        bytes[byteOffset + 3].toUByte()).toInt()

        override fun setInt(bytes: ByteArray, byteOffset: Int, value: Int) {
            bytes[byteOffset] = (value ushr 24).toByte()
            bytes[byteOffset + 1] = (value ushr 16).toByte()
            bytes[byteOffset + 2] = (value ushr 8).toByte()
            bytes[byteOffset + 3] = value.toByte()
        }
    },
    Little {
        override fun getShort(
            bytes: ByteArray,
            byteOffset: Int
        ): Short = ((bytes[byteOffset + 1].toUByte().toUInt() shl 8) + bytes[byteOffset].toUByte()).toShort()

        override fun setShort(bytes: ByteArray, byteOffset: Int, value: Int) {
            bytes[byteOffset + 1] = (value ushr 8).toByte()
            bytes[byteOffset] = value.toByte()
        }

        override fun getInt(bytes: ByteArray, byteOffset: Int): Int = (
                (bytes[byteOffset + 3].toUByte().toUInt() shl 24) +
                        (bytes[byteOffset + 2].toUByte().toUInt() shl 16) +
                        (bytes[byteOffset + 1].toUByte().toUInt() shl 8) +
                        bytes[byteOffset].toUByte()).toInt()

        override fun setInt(bytes: ByteArray, byteOffset: Int, value: Int) {
            bytes[byteOffset + 3] = (value ushr 24).toByte()
            bytes[byteOffset + 2] = (value ushr 16).toByte()
            bytes[byteOffset + 1] = (value ushr 8).toByte()
            bytes[byteOffset] = value.toByte()
        }
    };

    /**
     * Returns the (possibly negative) integer represented by the two bytes at
     * the specified [byteOffset] in this object, in two's complement binary
     * form.
     *
     * The return value will be between -2<sup>15</sup> and 2<sup>15</sup> - 1,
     * inclusive.
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 2` must be less than or equal to the length of this object.
     */
    public abstract fun getShort(bytes: ByteArray, byteOffset: Int): Short

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
     * Returns the positive integer represented by the two bytes starting
     * at the specified [byteOffset] in this object, in unsigned binary
     * form.
     *
     * The return value will be between 0 and  2^16 - 1, inclusive.
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 2` must be less than or equal to the length of this object.
     */
    public fun getUShort(bytes: ByteArray, byteOffset: Int): UShort = getShort(bytes, byteOffset).toUShort()

    /**
     * Sets the two bytes starting at the specified [byteOffset] in this object
     * to the unsigned binary representation of the specified [value],
     * which must fit in two bytes.
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 2` must be less than or equal to the length of [bytes].
     */
    public fun setUShort(bytes: ByteArray, byteOffset: Int, value: UShort) {
        setShort(bytes, byteOffset, value.toShort());
    }

    /**
     * Sets the two bytes starting at the specified [byteOffset] in this object
     * to the unsigned binary representation of the specified [value],
     * which must fit in two bytes.
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 2` must be less than or equal to the length of [bytes].
     */
    public fun setUShort(bytes: ByteArray, byteOffset: Int, value: UInt) {
        setShort(bytes, byteOffset, value.toShort())
    }

    /**
    * Returns the (possibly negative) integer represented by the four bytes at
    * the specified [byteOffset] in this object, in two's complement binary
    * form.
    *
    * The return value will be between -2^31 and 2^31 - 1,
    * inclusive.
    *
    * The [byteOffset] must be non-negative, and
    * `byteOffset + 4` must be less than or equal to the length of this object.
     */
    public abstract fun getInt(bytes: ByteArray, byteOffset: Int): Int

    /**
     * Sets the four bytes starting at the specified [byteOffset] in this
     * object to the two's complement binary representation of the specified
     * [value], which must fit in four bytes.
     *
     * In other words, [value] must lie
     * between -2^31 and 2^31 - 1, inclusive.
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 4` must be less than or equal to the length of this object.
     */
    public abstract fun setInt(bytes: ByteArray, byteOffset: Int, value: Int)
}
