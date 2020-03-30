package jp.co.iijii.wasmonandroid.wasi

enum class Whence(val asU8: Byte) {
    Set(0), Cur(1), End(2)
}