package jp.co.iijii.wasmonandroid

import jp.co.iijii.wasmonandroid.wasi.WasiEnv

class Wasmer {
    companion object {
        @JvmStatic
        external fun runWasm(wasmPath: String, wasiEnv: WasiEnv)
    }
}