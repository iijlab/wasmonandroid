package jp.co.iijii.wasmonandroid.wasi

typealias ErrnoT = Short

// TODO: Convert to a true enum class with http://technofovea.com/blog/archives/815
class Errno {
    companion object {
        const val success: ErrnoT = 0
        const val _2big: ErrnoT = 1
        const val acces: ErrnoT = 2
        const val addrinuse: ErrnoT = 3
        const val addrnotavail: ErrnoT = 4
        const val afnosupport: ErrnoT = 5
        const val again: ErrnoT = 6
        const val already: ErrnoT = 7
        const val badf: ErrnoT = 8
        const val badmsg: ErrnoT = 9
        const val busy: ErrnoT = 10
        const val canceled: ErrnoT = 11
        const val child: ErrnoT = 12
        const val connaborted: ErrnoT = 13
        const val connrefused: ErrnoT = 14
        const val connreset: ErrnoT = 15
        const val deadlk: ErrnoT = 16
        const val destaddrreq: ErrnoT = 17
        const val dom: ErrnoT = 18
        const val dquot: ErrnoT = 19
        const val exist: ErrnoT = 20
        const val fault: ErrnoT = 21
        const val fbig: ErrnoT = 22
        const val hostunreach: ErrnoT = 23
        const val idrm: ErrnoT = 24
        const val ilseq: ErrnoT = 25
        const val inprogress: ErrnoT = 26
        const val intr: ErrnoT = 27
        const val inval: ErrnoT = 28
        const val io: ErrnoT = 29
        const val isconn: ErrnoT = 30
        const val isdir: ErrnoT = 31
        const val loop: ErrnoT = 32
        const val mfile: ErrnoT = 33
        const val mlink: ErrnoT = 34
        const val msgsize: ErrnoT = 35
        const val multihop: ErrnoT = 36
        const val nametoolong: ErrnoT = 37
        const val netdown: ErrnoT = 38
        const val netreset: ErrnoT = 39
        const val netunreach: ErrnoT = 40
        const val nfile: ErrnoT = 41
        const val nobufs: ErrnoT = 42
        const val nodev: ErrnoT = 43
        const val noent: ErrnoT = 44
        const val noexec: ErrnoT = 45
        const val nolck: ErrnoT = 46
        const val nolink: ErrnoT = 47
        const val nomem: ErrnoT = 48
        const val nomsg: ErrnoT = 49
        const val noprotoopt: ErrnoT = 50
        const val nospc: ErrnoT = 51
        const val nosys: ErrnoT = 52
        const val notconn: ErrnoT = 53
        const val notdir: ErrnoT = 54
        const val notempty: ErrnoT = 55
        const val notrecoverable: ErrnoT = 56
        const val notsock: ErrnoT = 57
        const val notsup: ErrnoT = 58
        const val notty: ErrnoT = 59
        const val nxio: ErrnoT = 60
        const val overflow: ErrnoT = 61
        const val ownerdead: ErrnoT = 62
        const val perm: ErrnoT = 63
        const val pipe: ErrnoT = 64
        const val proto: ErrnoT = 65
        const val protonosupport: ErrnoT = 66
        const val prototype: ErrnoT = 67
        const val range: ErrnoT = 68
        const val rofs: ErrnoT = 69
        const val spipe: ErrnoT = 70
        const val srch: ErrnoT = 71
        const val stale: ErrnoT = 72
        const val timedout: ErrnoT = 73
        const val txtbsy: ErrnoT = 74
        const val xdev: ErrnoT = 75
        const val notcapable: ErrnoT = 76
    }
}