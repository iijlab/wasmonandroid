package jp.co.iijii.wasmonandroid.wasi

/**
 * This is not documented in WASI specification.
 * Used internally. Most of the fields are translated from InodeVal in wasmer.
 */
class InodeValue(
    val stat: Filestat,
    val isPreopen: Boolean,
    val name: String,
    val fsEntry: FsEntry
) {
    companion object {
        fun root(ino: Inode, entry: FsEntry.Dir): InodeValue =
            InodeValue(Filestat.directory(ino), true, "/", entry)

        fun preopenDir(name: String, stat: Filestat, entry: FsEntry.Dir): InodeValue =
            InodeValue(stat, true, name, entry)
    }
}
