package com.github.mvysny.unsigned

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

class PartsTest {
    @Nested inner class UShort {
        @Test fun hibyte() {
            expect(0.toUByte()) { 0.toUShort().hibyte }
            expect(0.toUByte()) { 1.toUShort().hibyte }
            expect(0.toUByte()) { 0x80.toUShort().hibyte }
            expect(0.toUByte()) { 0xFF.toUShort().hibyte }
            expect(1.toUByte()) { 0x100.toUShort().hibyte }
            expect(1.toUByte()) { 0x180.toUShort().hibyte }
            expect(0x80.toUByte()) { 0x8000.toUShort().hibyte }
            expect(0x80.toUByte()) { 0x8080.toUShort().hibyte }
            expect(0x80.toUByte()) { 0x80FF.toUShort().hibyte }
            expect(0xFF.toUByte()) { 0xFFFF.toUShort().hibyte }
        }

        @Test fun lobyte() {
            expect(0.toUByte()) { 0.toUShort().lobyte }
            expect(1.toUByte()) { 1.toUShort().lobyte }
            expect(0x80.toUByte()) { 0x80.toUShort().lobyte }
            expect(0xff.toUByte()) { 0xFF.toUShort().lobyte }
            expect(0.toUByte()) { 0x100.toUShort().lobyte }
            expect(0x80.toUByte()) { 0x180.toUShort().lobyte }
            expect(0.toUByte()) { 0x8000.toUShort().lobyte }
            expect(0x80.toUByte()) { 0x8080.toUShort().lobyte }
            expect(0xff.toUByte()) { 0x80FF.toUShort().lobyte }
            expect(0xFF.toUByte()) { 0xFFFF.toUShort().lobyte }
        }
    }
}
