package jp.co.iijii.wasmonandroid.wasi

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import java.io.File

/**
 * This is not documented in WASI specification.
 * Used internally. Most of the fields are translated from PreopenedDir in wasmer.
 */
class PreopenDirectory (
    val path: String,
    private val alias: String?,
    val read: Boolean,
    val write: Boolean,
    val create: Boolean
) {

    val guestPath: String
        get() = alias ?: path

    companion object {
        fun new(
            path: String,
            alias: String? = null,
            read: Boolean = true,
            write: Boolean = true,
            create: Boolean = true
        ): Result<PreopenDirectory, WasiException> {
            if (!File(path).isDirectory) {
                return Err(WasiException("WASI only supports pre-opened directories right now. But $path isn't a directory!"))
            }
            return Ok(PreopenDirectory(path, alias, read, write, create))
        }
    }
}
