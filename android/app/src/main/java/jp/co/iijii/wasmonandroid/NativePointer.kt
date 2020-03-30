package jp.co.iijii.wasmonandroid

import com.sun.jna.Pointer
import com.sun.jna.Structure

// TODO: Replace with WasmPointer
class NativePointer<T : Structure>(asLong: Long) {
    private val ptr = Pointer(asLong)
    fun derefStruct(make: (Pointer) -> T): T = make(ptr)
}
