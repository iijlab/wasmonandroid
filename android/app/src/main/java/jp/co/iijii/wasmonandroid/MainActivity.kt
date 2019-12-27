package jp.co.iijii.wasmonandroid

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    companion object
    {
        init { System.loadLibrary("greeting"); }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogInit.init()

        setContentView(R.layout.activity_main)
        val result = RustGreetings().sayHello("Android on Rust!")
        findViewById<TextView>(R.id.greeting_view).text = result

        val gcdWasm = File(filesDir, "gcd.wasm")

        if (!gcdWasm.exists()) {
            assets.open("gcd.wasm").use { src ->
                FileOutputStream(gcdWasm).use { dest ->
                    src.copyTo(dest)
                }
            }
        }

        Wasmer.runWasm(gcdWasm.absolutePath)
    }
}
