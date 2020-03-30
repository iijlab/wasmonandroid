package jp.co.iijii.wasmonandroid

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import jp.co.iijii.wasmonandroid.wasi.PreopenDirectory
import jp.co.iijii.wasmonandroid.wasi.WasiEnv
import jp.co.iijii.wasmonandroid.wasi.WasiFs
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity(), CoroutineScope {
    companion object {
        init {
            System.loadLibrary("greeting")
        }
    }

    private val job = Job()
    override val coroutineContext = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogInit.init()

        setContentView(R.layout.activity_main)

        val wasmFilePath = "wasi-hello.wasm"
        val wasmFile = File(filesDir, wasmFilePath)

        val statusView = findViewById<TextView>(R.id.status_view)
        if (!wasmFile.exists()) {
            statusView.text =
                resources.getString(R.string.copying_sample_wasm_files)
            launch(Dispatchers.IO) {
                assets.open(wasmFilePath).use { src ->
                    FileOutputStream(wasmFile).use { dest ->
                        src.copyTo(dest)
                    }
                }
            }
        }

        statusView.text = resources.getString(R.string.running_wasm)
        val consoleView = findViewById<EditText>(R.id.console_view)
        launch {
            withContext(Dispatchers.Main) {
                val preopens = listOf(
                    PreopenDirectory("test1", null, read = true, write = true, create = true),
                    PreopenDirectory("test2", null, false, write = false, create = false)
                )
                Wasmer.runWasm(
                    wasmFile.absolutePath,
                    WasiEnv(
                        WasiFs(
                            stdin = NullHandle,
                            stdout = OutTextViewHandle(consoleView),
                            stderr = OutTextViewHandle(consoleView),
                            preopens = preopens
                        )
                    )
                )
                statusView.text = resources.getString(R.string.finished_running_wasm)
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
