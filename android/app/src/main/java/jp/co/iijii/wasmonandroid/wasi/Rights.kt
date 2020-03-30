package jp.co.iijii.wasmonandroid.wasi

typealias RightsT = Long

class Rights {
    companion object {
        const val none: RightsT = 0

        const val fd_datasync: RightsT = 1
        const val fd_read: RightsT = 1 shl 1
        const val fd_seek: RightsT = 1 shl 2
        const val fd_fdstat_set_flags: RightsT = 1 shl 3
        const val fd_sync: RightsT = 1 shl 4
        const val fd_tell: RightsT = 1 shl 5
        const val fd_write: RightsT = 1 shl 6
        const val fd_advise: RightsT = 1 shl 7
        const val fd_allocate: RightsT = 1 shl 8
        const val path_create_directory: RightsT = 1 shl 9
        const val path_create_file: RightsT = 1 shl 10
        const val path_link_source: RightsT = 1 shl 11
        const val path_link_target: RightsT = 1 shl 12
        const val path_open: RightsT = 1 shl 13
        const val fd_readdir: RightsT = 1 shl 14
        const val path_readlink: RightsT = 1 shl 15
        const val path_rename_source: RightsT = 1 shl 16
        const val path_rename_target: RightsT = 1 shl 17
        const val path_filestat_get: RightsT = 1 shl 18
        const val path_filestat_set_size: RightsT = 1 shl 19
        const val path_filestat_set_times: RightsT = 1 shl 20
        const val fd_filestat_get: RightsT = 1 shl 21
        const val fd_filestat_set_size: RightsT = 1 shl 22
        const val fd_filestat_set_times: RightsT = 1 shl 23
        const val path_symlink: RightsT = 1 shl 24
        const val path_remove_directory: RightsT = 1 shl 25
        const val path_unlink_file: RightsT = 1 shl 26
        const val poll_fd_readwrite: RightsT = 1 shl 27
        const val sock_shutdown: RightsT = 1 shl 28

        val readable: RightsT =
            listOf(
                fd_read,
                path_open,
                fd_readdir,
                path_readlink,
                path_filestat_get,
                fd_filestat_get,
                path_link_source,
                path_rename_source,
                poll_fd_readwrite,
                sock_shutdown
            ).reduce { x, y -> x or y }

        val writable: RightsT =
            listOf(
                fd_fdstat_set_flags,
                fd_write,
                fd_sync,
                fd_allocate,
                path_open,
                path_rename_target,
                path_filestat_set_size,
                path_filestat_set_times,
                fd_filestat_set_size,
                fd_filestat_set_times,
                path_remove_directory,
                path_unlink_file,
                poll_fd_readwrite,
                sock_shutdown
            ).reduce { x, y -> x or y }

        val creatable: RightsT =
            listOf(
                path_create_directory,
                path_create_file,
                path_link_target,
                path_open,
                path_rename_target
            ).reduce { x, y -> x or y }

        val stdinDefault: RightsT =
            listOf(
                fd_datasync,
                fd_read,
                fd_sync,
                fd_advise,
                fd_filestat_get,
                poll_fd_readwrite
            ).reduce { x, y -> x or y }

        val stdoutDefault: RightsT =
            listOf(
                fd_datasync,
                fd_write,
                fd_sync,
                fd_advise,
                fd_filestat_get,
                poll_fd_readwrite
            ).reduce { x, y -> x or y }

        val stderrDefault: RightsT = stdoutDefault

        fun denies(rightsSet: RightsT, targetRights: RightsT): Boolean =
            rightsSet and targetRights == 0L
    }
}