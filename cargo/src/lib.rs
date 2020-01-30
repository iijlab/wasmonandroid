use std::ffi::{CStr, CString};
use std::os::raw::c_char;

extern crate log;

#[no_mangle]
pub extern "C" fn rust_greeting(to: *const c_char) -> *mut c_char {
    let c_str = unsafe { CStr::from_ptr(to) };
    let recipient = match c_str.to_str() {
        Err(_) => "there",
        Ok(string) => string,
    };

    CString::new("Hello ".to_owned() + recipient)
        .unwrap()
        .into_raw()
}

/// Expose the JNI interface for android below
#[cfg(target_os = "android")]
#[allow(non_snake_case)]
pub mod android {
    extern crate android_logger;
    extern crate jni;

    use wasmer_runtime;
    use wasmer_runtime_core;
    use wasmer_wasi;

    use std::ffi::{c_void, CString};
    use std::fs::read;
    use std::path::PathBuf;

    use log::Level;
    use log::{error, info};

    use jni::objects::{JClass, JString};
    use jni::sys::jstring;
    use jni::JNIEnv;

    use std::ops::Deref;
    use std::panic;

    use super::*;

    #[no_mangle]
    pub unsafe extern "C" fn Java_jp_co_iijii_wasmonandroid_RustGreetings_greeting(
        env: JNIEnv,
        _: JClass,
        java_pattern: JString,
    ) -> jstring {
        // Our Java companion code might pass-in "world" as a string, hence the name.
        let world = rust_greeting(
            env.get_string(java_pattern)
                .expect("invalid pattern string")
                .as_ptr(),
        );
        // Retake pointer so that we can use it below and allow memory to be freed when it goes out of scope.
        let world_ptr = CString::from_raw(world);
        let output = env
            .new_string(world_ptr.to_str().unwrap())
            .expect("Couldn't create java string!");

        output.into_inner()
    }

    #[no_mangle]
    pub extern "C" fn Java_jp_co_iijii_wasmonandroid_Wasmer_runWasm(
        env: JNIEnv,
        _: JClass,
        jwasm_path: JString,
    ) -> () {
        let import_object = generate_import_object(vec![], vec![], vec![], vec![]);

        let wasm_path: &String = &env
            .get_string(jwasm_path)
            .expect("invalid Java String")
            .into();
        let wasm_bin = read(wasm_path).expect(&format!("Failed to read {}", wasm_path));

        let instance = wasmer_runtime::instantiate(&wasm_bin, &import_object).unwrap();

        instance.dyn_func("_start").unwrap().call(&[]).unwrap();
    }

    #[no_mangle]
    pub extern "C" fn Java_jp_co_iijii_wasmonandroid_LogInit_init(_env: JNIEnv, _: JClass) {
        android_logger::init_once(android_logger::Config::default().with_min_level(Level::Debug));

        // https://stackoverflow.com/questions/42456497/stdresultresult-panic-to-log
        panic::set_hook(Box::new(|panic_info| {
            let (filename, line) = panic_info
                .location()
                .map(|loc| (loc.file(), loc.line()))
                .unwrap_or(("<unknown>", 0));

            let cause = panic_info
                .payload()
                .downcast_ref::<String>()
                .map(String::deref);

            let cause = cause.unwrap_or_else(|| {
                panic_info
                    .payload()
                    .downcast_ref::<&str>()
                    .map(|s| *s)
                    .unwrap_or("<cause unknown>")
            });

            error!("A panic occurred at {}:{}: {}", filename, line, cause);
        }));
    }

    struct WasiState

    /// Creates a Wasi [`wasmer_runtime::ImportObject`] with [`wasmer::WasiState`] with the latest snapshot
    /// of WASI.
    // TODO: customize for Java
    fn generate_import_object(
        args: Vec<Vec<u8>>,
        envs: Vec<Vec<u8>>,
        preopened_files: Vec<PathBuf>,
        mapped_dirs: Vec<(String, PathBuf)>,
    ) -> wasmer_runtime::ImportObject {
        let state_gen = move || {
            // TODO: look into removing all these unnecessary clones
            fn state_destructor(data: *mut c_void) {
                unsafe {
                    drop(Box::from_raw(data as *mut wasmer_wasi::WasiState));
                }
            }
            let preopened_files = preopened_files.clone();
            let mapped_dirs = mapped_dirs.clone();

            let state = Box::new(wasmer_wasi::WasiState {
                // WasiFs for Android
                fs: wasmer_wasi::WasiFs::new(&preopened_files, &mapped_dirs)
                    .expect("Could not create WASI FS"),
                args: args.clone(),
                envs: envs.clone(),
            });

            (
                Box::into_raw(state) as *mut c_void,
                state_destructor as fn(*mut c_void),
            )
        };

        generate_import_object_snapshot1_inner(state_gen)
    }

    /// Combines a state generating function with the import list for snapshot 1
    fn generate_import_object_snapshot1_inner<F>(state_gen: F) -> wasmer_runtime::ImportObject
    where
        F: Fn() -> (*mut c_void, fn(*mut c_void)) + Send + Sync + 'static,
    {
        wasmer_runtime::imports! {
                state_gen,
                "wasi_snapshot_preview1" => {
                    //"args_get" => wasmer_runtime::func!(args_get),
                    //"args_sizes_get" => wasmer_runtime::func!(args_sizes_get),
                    //"clock_res_get" => wasmer_runtime::func!(clock_res_get),
                    //"clock_time_get" => wasmer_runtime::func!(clock_time_get),
                    "environ_get" => wasmer_runtime::func!(environ_get),
                    "environ_sizes_get" => wasmer_runtime::func!(environ_sizes_get),
                    //"fd_advise" => wasmer_runtime::func!(fd_advise),
                    //"fd_allocate" => wasmer_runtime::func!(fd_allocate),
                    //"fd_close" => wasmer_runtime::func!(fd_close),
                    //"fd_datasync" => wasmer_runtime::func!(fd_datasync),
                    //"fd_fdstat_get" => wasmer_runtime::func!(fd_fdstat_get),
                    //"fd_fdstat_set_flags" => wasmer_runtime::func!(fd_fdstat_set_flags),
                    //"fd_fdstat_set_rights" => wasmer_runtime::func!(fd_fdstat_set_rights),
                    //"fd_filestat_get" => wasmer_runtime::func!(fd_filestat_get),
                    //"fd_filestat_set_size" => wasmer_runtime::func!(fd_filestat_set_size),
                    //"fd_filestat_set_times" => wasmer_runtime::func!(fd_filestat_set_times),
                    //"fd_pread" => wasmer_runtime::func!(fd_pread),
                    "fd_prestat_get" => wasmer_runtime::func!(fd_prestat_get),
                    "fd_prestat_dir_name" => wasmer_runtime::func!(fd_prestat_dir_name),
                    //"fd_pwrite" => wasmer_runtime::func!(fd_pwrite),
                    //"fd_read" => wasmer_runtime::func!(fd_read),
                    //"fd_readdir" => wasmer_runtime::func!(fd_readdir),
                    //"fd_renumber" => wasmer_runtime::func!(fd_renumber),
                    //"fd_seek" => wasmer_runtime::func!(fd_seek),
                    //"fd_sync" => wasmer_runtime::func!(fd_sync),
                    //"fd_tell" => wasmer_runtime::func!(fd_tell),
                    "fd_write" => wasmer_runtime::func!(fd_write),
                    //"path_create_directory" => wasmer_runtime::func!(path_create_directory),
                    //"path_filestat_get" => wasmer_runtime::func!(path_filestat_get),
                    //"path_filestat_set_times" => wasmer_runtime::func!(path_filestat_set_times),
                    //"path_link" => wasmer_runtime::func!(path_link),
                    //"path_open" => wasmer_runtime::func!(path_open),
                    //"path_readlink" => wasmer_runtime::func!(path_readlink),
                    //"path_remove_directory" => wasmer_runtime::func!(path_remove_directory),
                    //"path_rename" => wasmer_runtime::func!(path_rename),
                    //"path_symlink" => wasmer_runtime::func!(path_symlink),
                    //"path_unlink_file" => wasmer_runtime::func!(path_unlink_file),
                    //"poll_oneoff" => wasmer_runtime::func!(poll_oneoff),
                    "proc_exit" => wasmer_runtime::func!(proc_exit),
                    //"proc_raise" => wasmer_runtime::func!(proc_raise),
                    //"random_get" => wasmer_runtime::func!(random_get),
                    //"sched_yield" => wasmer_runtime::func!(sched_yield),
                    //"sock_recv" => wasmer_runtime::func!(sock_recv),
                    //"sock_send" => wasmer_runtime::func!(sock_send),
                    //"sock_shutdown" => wasmer_runtime::func!(sock_shutdown),
                },
        }
    }
}
