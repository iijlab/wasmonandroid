package jp.co.iijii.wasmonandroid.wasi

import com.sun.jna.Pointer

// TODO: Replace with something like WasmPointer
class SizePointer(asLong: Long) {
    private val ptr = Pointer(asLong)
    fun poke(size: Size) = ptr.setInt(0, size)
}