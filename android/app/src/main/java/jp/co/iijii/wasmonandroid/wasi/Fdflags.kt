package jp.co.iijii.wasmonandroid.wasi

typealias FdflagsT = Short

class Fdflags {
    companion object {
        const val none: FdflagsT = 0

        const val append: FdflagsT = 1 shl 0
        const val dsync: FdflagsT = 1 shl 1
        const val nonblock: FdflagsT = 1 shl 2
        const val rsync: FdflagsT = 1 shl 3
        const val sync: FdflagsT = 1 shl 4
    }
}