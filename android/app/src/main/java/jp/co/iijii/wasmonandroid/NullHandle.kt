package jp.co.iijii.wasmonandroid

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import jp.co.iijii.wasmonandroid.wasi.*
import java.io.IOException

object NullHandle : Handle {
    override fun read() {
        TODO("Not yet implemented")
    }

    override fun seek(pos: Long, whence: Whence): Result<Unit, IOException> = Ok(Unit)

    override fun write(bytes: ByteArray): Result<Size, IOException> = Ok(bytes.size)

    override fun flush() {
    }

    override var lastAccessed: Timestamp
        get() = TODO("Not yet implemented")
        set(value) {}
    override var lastModified: Timestamp
        get() = TODO("Not yet implemented")
        set(value) {}
    override var createdTime: Timestamp
        get() = TODO("Not yet implemented")
        set(value) {}
    override var size: Filesize
        get() = TODO("Not yet implemented")
        set(value) {}
}