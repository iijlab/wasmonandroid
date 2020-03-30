package jp.co.iijii.wasmonandroid.wasi

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.sun.jna.Pointer

/** Ref.
https://github.com/wasmerio/wasmer/blob/2c44b700c883c03ca6efbb311591a1847f16d9dd/lib/runtime-core/src/memory/ptr.rs#L54-L65
*/
class WasiMemory(private val asPtr: Long, private val sizeInBytes: Long) {
    fun makeNativePointer(memoryOffset: Size, allocatedSize: Size): Result<Pointer, ErrnoT> {
        if (memoryOffset + allocatedSize >= sizeInBytes) {
            return Err(Errno.fault)
        }
        return Ok(Pointer(asPtr + memoryOffset))
    }
}
