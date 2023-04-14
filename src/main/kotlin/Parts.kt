package com.github.mvysny.unsigned

public inline val UShort.hibyte: UByte get() = (toInt() ushr 8).toUByte()
public inline val UShort.lobyte: UByte get() = toUByte()
