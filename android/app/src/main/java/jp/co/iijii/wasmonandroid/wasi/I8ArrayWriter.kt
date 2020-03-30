package jp.co.iijii.wasmonandroid.wasi

import com.sun.jna.Pointer

// TODO: Replace with something like WasmPointer
class I8ArrayWriter(asLong: Long) {
    private val ptr = Pointer(asLong)

    fun write(values: ByteArray, length: Int): ErrnoT {
        if (values.size > length) {
            return Errno.overflow
        }
        ptr.write(0, values, 0, length)
        return Errno.success
    }
}