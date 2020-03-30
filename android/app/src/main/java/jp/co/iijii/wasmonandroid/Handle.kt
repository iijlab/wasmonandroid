package jp.co.iijii.wasmonandroid
import com.github.michaelbull.result.Result
import jp.co.iijii.wasmonandroid.wasi.Filesize
import jp.co.iijii.wasmonandroid.wasi.Size
import jp.co.iijii.wasmonandroid.wasi.Timestamp
import jp.co.iijii.wasmonandroid.wasi.Whence

interface Handle {
    fun read()
    fun seek(pos: Long, whence: Whence): Result<Unit, Exception>
    fun write(bytes: ByteArray): Result<Size, Exception>
    fun flush()

    var lastAccessed: Timestamp
    var lastModified: Timestamp
    var createdTime: Timestamp

    var size: Filesize
}
