package jp.co.iijii.wasmonandroid

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity(), CoroutineScope {
    companion object {
        init {
            System.loadLibrary("greeting"); }
    }

    private val job = Job()
    override val coroutineContext = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogInit.init()

        setContentView(R.layout.activity_main)

        val gcdWasm = File(filesDir, "gcd.wasm")

        if (!gcdWasm.exists()) {
            findViewById<TextView>(R.id.initial_view).text =
                resources.getString(R.string.copying_sample_wasm_files)
            launch(Dispatchers.IO) {
                assets.open("gcd.wasm").use { src ->
                    FileOutputStream(gcdWasm).use { dest ->
                        src.copyTo(dest)
                    }
                }
            }
        }

        findViewById<TextView>(R.id.initial_view).text =
            resources.getString(R.string.running_wasm)

        launch {
            withContext(Dispatchers.IO) {
                Wasmer.runWasm(gcdWasm.absolutePath)
            }

            // TODO: Show result of wasm
            findViewById<TextView>(R.id.initial_view).text = "Finished"
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
