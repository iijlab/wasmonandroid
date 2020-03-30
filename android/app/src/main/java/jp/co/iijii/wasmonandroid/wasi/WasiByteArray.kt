package jp.co.iijii.wasmonandroid.wasi

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.sun.jna.Pointer
import com.sun.jna.Structure

@Structure.FieldOrder("memoryOffset")
class WasiByteArray : Structure, Structure.ByValue {
    @Suppress("UNUSED")
    constructor(p: Pointer) : super(p) {
        read()
    }

    constructor() : super(nullPointer)

    @Suppress("EXPECTED_PRIVATE_DECLARATION")
    @JvmField
    var memoryOffset: Size = 0

    fun deref(memory: WasiMemory, size: Size): Result<ByteArray, ErrnoT> =
        memory.makeNativePointer(memoryOffset, size).map { it.getByteArray(0, size) }

    companion object {
        private val nullPointer = Pointer(0)
    }
}