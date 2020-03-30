package jp.co.iijii.wasmonandroid.wasi

import android.util.Log
import com.github.michaelbull.result.mapBoth
import jp.co.iijii.wasmonandroid.NativeArray
import jp.co.iijii.wasmonandroid.NativePointer

// Methods of this class are called in cargo/src/lib.rs
class WasiEnv(private val fs: WasiFs) {

    @Suppress("unused")
    fun environGet(): ErrnoT {
        Log.i("WasiEnv", "Calling environGet. NOT IMPLEMENTED!")
        return Errno.success
    }

    @Suppress("unused")
    fun environSizesGet(): ErrnoT {
        Log.i("WasiEnv", "Calling environSizesGet. NOT IMPLEMENTED!")
        return Errno.success
    }

    @Suppress("unused")
    fun fdPrestatGet(fd: Fd, buf: NativePointer<Prestat>): ErrnoT {
        Log.i("WasiEnv", "START fdPrestatGet. fd: $fd")
        val dest = buf.derefStruct(::Prestat)
        return fs.getPrestat(fd).mapBoth(
            success = { src ->
                dest.prType = src.prType
                dest.u = src.u
                dest.write()
                Errno.success
            },
            failure = { errno -> errno }
        )
    }

    @Suppress("unused")
    fun fdPrestatDirName(fd: Fd, path: I8ArrayWriter, pathLen: Int): ErrnoT {
        Log.i("WasiEnv", "START fdPrestatDirName. fd: $fd, pathLen: $pathLen")
        return fs.getPrestatDirName(fd).mapBoth(
            success = { dirName ->
                path.write(dirName.toByteArray(), pathLen)
            },
            failure = { errno -> errno }
        )
    }

    @Suppress("unused")
    fun fdWrite(
        mem: WasiMemory,
        fd: Fd,
        iovs: NativeArray<Ciovec>,
        iovsSize: Size,
        nwrittenOut: SizePointer
    ): ErrnoT {
        Log.i("WasiEnv", "START fdWrite. fd: $fd, iovsSize: $iovsSize")
        return fs.writeToFd(mem, fd, iovs.derefArray(::Ciovec, iovsSize)).mapBoth(
            success = { nwritten ->
                nwrittenOut.poke(nwritten)
                Errno.success
            },
            failure = { errno -> errno }
        )
    }
}
