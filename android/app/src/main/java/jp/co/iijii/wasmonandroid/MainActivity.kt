package jp.co.iijii.wasmonandroid

import android.os.Bundle
import android.view.View
import android.widget.*
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

    var selectedWasmFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogInit.init()

        setContentView(R.layout.activity_main)

        val wasmFiles =
            assets.list("")!!.toList().filter { it.endsWith(".wasm") }.map(filesDir::resolve)
        val statusView = findViewById<TextView>(R.id.status_view)

        wasmFiles.filter { !it.exists() }.also { nonExistingWasmFiles ->
            if (nonExistingWasmFiles.isNotEmpty()) {
                statusView.text =
                    resources.getString(R.string.copying_sample_wasm_files)
                launch(Dispatchers.IO) {
                    nonExistingWasmFiles.forEach { wasmFile ->
                        assets.open(wasmFile.name).use { src ->
                            FileOutputStream(wasmFile).use { dest ->
                                src.copyTo(dest)
                            }
                        }
                    }
                }
                statusView.text =
                    resources.getString(R.string.initializing)
            }
        }

        findViewById<Spinner>(R.id.select_wasm_file).also { selectWasm ->
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                wasmFiles.map { it.name }
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                selectWasm.adapter = adapter
            }

            selectWasm.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedWasmFile = null
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedWasmFile = wasmFiles[position]
                }
            }
        }

        findViewById<Button>(R.id.run_wasm).also { button ->
            button.setOnClickListener {
                val wasmFile = selectedWasmFile ?: return@setOnClickListener

                statusView.text = resources.getString(R.string.running_wasm)
                val consoleView = findViewById<EditText>(R.id.console_view)
                launch {
                    withContext(Dispatchers.Main) {
                        val preopens = listOf(
                            PreopenDirectory(
                                "test1",
                                null,
                                read = true,
                                write = true,
                                create = true
                            ),
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
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
