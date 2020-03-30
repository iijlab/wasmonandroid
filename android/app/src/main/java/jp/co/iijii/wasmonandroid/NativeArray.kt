package jp.co.iijii.wasmonandroid

import com.sun.jna.Pointer
import com.sun.jna.Structure
import jp.co.iijii.wasmonandroid.wasi.Size

// TODO: Replace with something like WasmPointer
class NativeArray<T : Structure>(asLong: Long) {
    private val ptr = Pointer(asLong)

    @Suppress("UNCHECKED_CAST")
    fun derefArray(make: (Pointer) -> T, size: Size): Array<T> =
        make(ptr).toArray(size) as Array<T>
}