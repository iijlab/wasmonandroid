package jp.co.iijii.wasmonandroid.wasi

import com.sun.jna.Pointer
import com.sun.jna.Structure

@Structure.FieldOrder("prType", "u")
class Prestat : Structure {
    constructor(p: Pointer): super(p) {
        read()
    }
    private constructor(): super()

    @Suppress("EXPECTED_PRIVATE_DECLARATION")
    @JvmField
    var prType: PreopentypeT = Preopentype.invalid

    @Suppress("EXPECTED_PRIVATE_DECLARATION")
    @JvmField
    var u: PrestatU = PrestatU()

    override fun read() {
        super.read()
        when (prType) {
            Preopentype.dir ->
                u.setType(PrestatDir::class.java)
            else ->
                throw IllegalArgumentException("Unknown \$prType: $prType")
        }
        u.read()
    }

    companion object {
        fun dir(prNameLen: Int): Prestat {
            val dir = PrestatDir()
            // TODO: Correct character encoding?
            dir.prNameLen = prNameLen

            val u = PrestatU()
            u.dir = dir

            val prestat = Prestat()
            prestat.prType = Preopentype.dir
            prestat.u = u

            return prestat
        }
    }
}