package jp.co.iijii.wasmonandroid

class RustGreetings {
    fun sayHello(to: String): String {
        return greeting(to)
    }

    companion object {
        private external fun greeting(pattern: String): String
    }
}