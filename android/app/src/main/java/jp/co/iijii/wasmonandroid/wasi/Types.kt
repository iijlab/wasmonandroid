package jp.co.iijii.wasmonandroid.wasi

/**
 * This is not documented in WASI specification.
 * Used to represent the initial value.
 */
const val INVALID_UBYTE: Byte = -1

/**
 * This is not documented in WASI specification.
 * Used to represent the initial value.
 */
const val INVALID_SIZE: Size = -1

/**
 * This is not documented in WASI specification.
 * Used to represent the initial value.
 */
const val INVALID_ULONG: Long = -1

typealias Inode = Long

typealias Size = Int

typealias Device = Long

typealias Linkcount = Long

typealias Filesize = Long

typealias Timestamp = Long

typealias Exitcode = Int

typealias Fd = Int

// These are not defined in the current WASI's witx.
const val FD_STDIN: Fd = 0
const val FD_STDOUT: Fd = 1
const val FD_STDERR: Fd = 2
