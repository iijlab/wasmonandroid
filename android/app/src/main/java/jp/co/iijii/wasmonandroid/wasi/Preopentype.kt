package jp.co.iijii.wasmonandroid.wasi

typealias PreopentypeT = Byte

// TODO: Convert to a true enum class with http://technofovea.com/blog/archives/815
class Preopentype {
    companion object {
        /**
         * This is not documented in WASI specification.
         * Used to represent the initial value.
         */
        const val invalid: PreopentypeT = -1

        const val dir: PreopentypeT = 0
    }
}
