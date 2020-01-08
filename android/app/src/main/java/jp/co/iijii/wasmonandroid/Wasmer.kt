package jp.co.iijii.wasmonandroid

class Wasmer {
    companion object {
        @JvmStatic
        external fun runWasm(wasmPath: String)
    }
}