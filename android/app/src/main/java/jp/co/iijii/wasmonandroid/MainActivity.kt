package jp.co.iijii.wasmonandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.widget.TextView


class MainActivity : AppCompatActivity() {
    companion object
    {
        init { System.loadLibrary("greeting"); }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val result = RustGreetings().sayHello("Android on Rust!")
        findViewById<TextView>(R.id.greeting_view).text = result
    }
}
