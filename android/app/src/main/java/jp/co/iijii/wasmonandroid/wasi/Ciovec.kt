package jp.co.iijii.wasmonandroid.wasi

import com.github.michaelbull.result.Result
import com.sun.jna.Pointer
import com.sun.jna.Structure

@Structure.FieldOrder("buf", "bufLen")
class Ciovec : Structure, Structure.ByValue {
    constructor(p: Pointer): super(p) {
        read()
    }
    @Suppress("UNUSED")
    private constructor(): super()

    @Suppress("EXPECTED_PRIVATE_DECLARATION")
    @JvmField
    var buf: WasiByteArray = WasiByteArray()

    @Suppress("EXPECTED_PRIVATE_DECLARATION")
    @JvmField
    var bufLen: Size = INVALID_SIZE

    fun peek(mem: WasiMemory): Result<ByteArray, ErrnoT> =
        buf.deref(mem, bufLen)

    companion object {
        private val NULL = Pointer(0)
    }
}