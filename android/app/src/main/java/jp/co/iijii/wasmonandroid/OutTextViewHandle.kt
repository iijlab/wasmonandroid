package jp.co.iijii.wasmonandroid

import android.util.Log
import android.widget.TextView
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import jp.co.iijii.wasmonandroid.wasi.Filesize
import jp.co.iijii.wasmonandroid.wasi.Size
import jp.co.iijii.wasmonandroid.wasi.Timestamp
import jp.co.iijii.wasmonandroid.wasi.Whence
import java.lang.UnsupportedOperationException

class OutTextViewHandle(private val textView: TextView) :
    Handle {
    override fun read() {
        TODO("Not yet implemented")
    }

    override fun seek(pos: Long, whence: Whence) =
        Err(UnsupportedOperationException("Seek not supported"))

    override fun write(bytes: ByteArray): Result<Size, Exception> {
        // TODO: Character encoding. bytes may not be valid UTF-8 due to split writing
        textView.append(bytes.toString(Charsets.UTF_8))
        return Ok(bytes.size)
    }

    override fun flush() {
        Log.i("WasiFs", "Buffering is not yet implemented")
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