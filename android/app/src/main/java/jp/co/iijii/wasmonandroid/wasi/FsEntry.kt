package jp.co.iijii.wasmonandroid.wasi

import jp.co.iijii.wasmonandroid.Handle

/**
 * This is not documented in WASI specification.
 * Used internally. Most of the fields are translated from Kind in wasmer.
 */
sealed class FsEntry(val path: String) {
    class File(val handle: Handle?, path: String) : FsEntry(path)
    class Dir(val parent: Inode?, path: String, private val entries: MutableMap<String, Inode>) :
        FsEntry(path) {
        fun addChild(path: String, child: Inode) {
            entries[path] = child
        }
    }

    companion object {
        fun root(): Dir =
            Dir(null, "/", HashMap())

        fun preopenDir(parent: Inode, path: String): Dir =
            Dir(parent, path, HashMap())

    }
}
