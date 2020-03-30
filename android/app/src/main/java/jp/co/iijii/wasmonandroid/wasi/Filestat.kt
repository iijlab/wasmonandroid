package jp.co.iijii.wasmonandroid.wasi

import com.sun.jna.Pointer
import com.sun.jna.Structure

@Structure.FieldOrder("dev", "ino", "filetype", "nlink", "size", "atim", "mtim", "ctim")
class Filestat : Structure {
    // Perhaps used when initializing Structure
    @Suppress("UNUSED")
    constructor(p: Pointer) : super(p) {
        read()
    }

    private constructor() : super()

    @Suppress("EXPECTED_PRIVATE_DECLARATION")
    @JvmField
    var dev: Device = INVALID_ULONG

    @Suppress("EXPECTED_PRIVATE_DECLARATION")
    @JvmField
    var ino: Inode = INVALID_ULONG

    @Suppress("EXPECTED_PRIVATE_DECLARATION")
    @JvmField
    var filetype: FiletypeT = INVALID_UBYTE

    @Suppress("EXPECTED_PRIVATE_DECLARATION")
    @JvmField
    var nlink: Linkcount = INVALID_ULONG

    @Suppress("EXPECTED_PRIVATE_DECLARATION")
    @JvmField
    var size: Filesize = INVALID_ULONG

    @Suppress("EXPECTED_PRIVATE_DECLARATION")
    @JvmField
    var atim: Timestamp = INVALID_ULONG

    @Suppress("EXPECTED_PRIVATE_DECLARATION")
    @JvmField
    var mtim: Timestamp = INVALID_ULONG

    @Suppress("EXPECTED_PRIVATE_DECLARATION")
    @JvmField
    var ctim: Timestamp = INVALID_ULONG

    companion object {
        fun directory(ino: Inode): Filestat {
            val stat = Filestat()
            stat.ino = ino
            stat.filetype = Filetype.directory

            // TODO: Set these properly
            stat.dev = 0
            stat.nlink = 0
            stat.size = 0
            stat.atim = 0
            stat.mtim = 0
            stat.ctim = 0

            return stat
        }

        fun characterDevice(ino: Long): Filestat {
            val stat = Filestat()
            stat.ino = ino
            stat.filetype = Filetype.character_device

            stat.dev = 0
            stat.nlink = 1
            stat.size = 0
            stat.atim = 0
            stat.mtim = 0
            stat.ctim = 0

            return stat
        }
    }
}