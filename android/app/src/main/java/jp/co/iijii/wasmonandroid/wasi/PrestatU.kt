package jp.co.iijii.wasmonandroid.wasi

import com.sun.jna.Pointer
import com.sun.jna.Union

class PrestatU : Union {
    constructor(p: Pointer): super(p)
    constructor() : super()

    @JvmField
    var dir: PrestatDir = PrestatDir()

    init {
        setType(PrestatDir::class.java)
        read()
    }
}
