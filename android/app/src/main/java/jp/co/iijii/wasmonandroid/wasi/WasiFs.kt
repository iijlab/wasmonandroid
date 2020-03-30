package jp.co.iijii.wasmonandroid.wasi

import android.util.Log
import com.github.michaelbull.result.*
import jp.co.iijii.wasmonandroid.Handle
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * This is not documented in WASI specification.
 * Used internally. Most of the fields are translated from WasiFs in wasmer.
 */
class WasiFs(stdin: Handle, stdout: Handle, stderr: Handle, preopens: List<PreopenDirectory>) {
    companion object {
        private const val fdStdin = 0
        private const val fdStdout = 1
        private const val fdStderr = 2
    }

    private val preopenFds: List<Fd>
    private val inodes: ConcurrentHashMap<Inode, InodeValue> = ConcurrentHashMap()
    private val fdMap: MutableMap<Fd, OpenInode> = HashMap()

    // TODO: According to WASI spec, FDs (except for stdin/stdout/stderr and preopen ones)
    //       should not be consecutive numbers.
    // 0: stdin, 1: stdout, 2: stderr
    private val fdCounter: AtomicInteger = AtomicInteger(3)

    // The reason why the initial value is 2:
    // https://stackoverflow.com/questions/12768371/why-is-root-directory-always-stored-in-inode-two
    private val inodeCounter: AtomicLong = AtomicLong(2)

    init {
        createStdDev(stdin, "stdin", Rights.stdinDefault, fdStdin)
        createStdDev(stdout, "stdout", Rights.stdoutDefault, fdStdout)
        createStdDev(stderr, "stderr", Rights.stderrDefault, fdStderr)

        preopenFds = ArrayList()
        val rootIno = inodeCounter.getAndIncrement()
        val rootEntry = FsEntry.root()
        preopenFds.add(
            ({
                val rights = Rights.none
                inodes[rootIno] = InodeValue.root(rootIno, rootEntry)

                createFd(rights, rights, rootIno)
            })()
        )

        for (preopen in preopens) {
            val ino = inodeCounter.getAndIncrement()
            inodes[ino] =
                InodeValue.preopenDir(
                    preopen.guestPath,
                    Filestat.directory(ino),
                    FsEntry.preopenDir(rootIno, preopen.path)
                )

            val rights = ({
                val r = if (preopen.read) {
                    Rights.readable
                } else {
                    Rights.none
                }

                val w = if (preopen.write) {
                    Rights.writable
                } else {
                    Rights.none
                }

                val c = if (preopen.create) {
                    Rights.creatable
                } else {
                    Rights.none
                }

                r or w or c
            })()

            rootEntry.addChild(preopen.guestPath, ino)
            preopenFds.add(createFd(rights, rights, ino))
        }
    }

    fun getPrestat(fd: Fd): Result<Prestat, ErrnoT> {
        val oi = fdMap[fd] ?: return Err(Errno.badf)
        val inodeValue = inodes[oi.inode]
        if (inodeValue?.isPreopen != true) {
            return Err(Errno.badf)
        }
        return Ok(Prestat.dir(inodeValue.name.length))
    }

    fun getPrestatDirName(fd: Fd): Result<String, ErrnoT> {
        val oi = fdMap[fd] ?: return Err(Errno.badf)
        val inodeValue = inodes[oi.inode] ?: return Err(Errno.badf)
        if (inodeValue.fsEntry !is FsEntry.Dir) return Err(Errno.notdir)
        return Ok(inodeValue.fsEntry.path)
    }

    fun writeToFd(mem: WasiMemory, fd: Fd, iovs: Array<Ciovec>): Result<Size, ErrnoT> {
        val openInode = fdMap[fd] ?: return Err(Errno.badf)
        val inodeValue = inodes[openInode.inode] ?: return Err(Errno.badf)
        if (Rights.denies(openInode.rights, Rights.fd_write)) {
            return Err(Errno.acces)
        }
        when (inodeValue.fsEntry) {
            is FsEntry.File -> {
                val handle = inodeValue.fsEntry.handle ?: return Err(Errno.inval)
                handle.seek(openInode.offset, Whence.Set).mapError {
                    Log.d("WASI", "Seek failed: $it")
                }
                var bytesWritten = 0
                for (iov in iovs) {
                    Log.d("WasiFs", "Writing $iov")
                    val result = iov.peek(mem).andThen { bytes ->
                        handle.write(bytes).mapError { ex ->
                            Log.e("WASI", "Error from Handle::write.", ex)
                            Errno.io
                        }
                    }
                    when (result) {
                        is Ok -> bytesWritten += result.value
                        is Err -> return Err(result.error)
                    }
                }

                handle.flush()

                openInode.offset += bytesWritten
                return Ok(bytesWritten)
            }
            is FsEntry.Dir ->
                return Err(Errno.isdir)
        }
    }

    private fun createStdDev(
        stdin: Handle,
        name: String,
        rights: RightsT,
        fd: Fd
    ) {
        val ino = inodeCounter.getAndIncrement()
        val stat = Filestat.characterDevice(ino)
        inodes[ino] = InodeValue(stat, true, name, FsEntry.File(stdin, ""))
        fdMap[fd] = OpenInode(rights, Rights.none, ino)
    }

    private fun createFd(
        rights: RightsT,
        rightsInherited: RightsT,
        ino: Inode
    ): Fd {
        val fdn = fdCounter.getAndIncrement()
        fdMap[fdn] = OpenInode(rights, rightsInherited, ino)
        return fdn
    }

}
