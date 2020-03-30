package jp.co.iijii.wasmonandroid.wasi

import com.sun.jna.Structure

@Structure.FieldOrder("prNameLen")
class PrestatDir: Structure() {

    @JvmField
    var prNameLen: Size = INVALID_SIZE
}
