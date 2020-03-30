package jp.co.iijii.wasmonandroid.wasi

typealias FiletypeT = Byte

class Filetype {
    companion object {
        val unknown: FiletypeT = 0
        val block_device: FiletypeT = 1
        val character_device: FiletypeT = 2
        val directory: FiletypeT = 3
        val regular_file: FiletypeT = 4
        val socket_dgram: FiletypeT = 5
        val socket_stream: FiletypeT = 6
        val symbolic_link: FiletypeT = 7
    }
}
