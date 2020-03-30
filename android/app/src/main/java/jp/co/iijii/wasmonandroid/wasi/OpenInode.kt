package jp.co.iijii.wasmonandroid.wasi

/**
 * This is not documented in WASI specification.
 * Used internally.
 */
class OpenInode(
    val rights: RightsT,
    val rightsInherited: RightsT,
    val inode: Inode
) {
    var offset = 0L
}
