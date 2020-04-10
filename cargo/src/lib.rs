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

#[cfg(target_os = "android")]
#[allow(non_snake_case)]
pub mod android {
    extern crate android_logger;
    extern crate jni;

    use wasmer_runtime::{func, imports, instantiate, Array, Ctx, Func, ImportObject, WasmPtr};
    use wasmer_runtime_core::memory::Memory;
    use wasmer_wasi::types::{__wasi_ciovec_t, __wasi_exitcode_t, __wasi_fd_t, __wasi_prestat_t};
    use wasmer_wasi::ExitCode;

    use std::convert::Infallible;
    use std::ffi::{c_void, CString};
    use std::fs::read;
    use std::ops::Deref;
    use std::sync::Arc;
    use std::{mem, panic};

    use log::Level;
    use log::{debug, error};

    use jni::descriptors::Desc;
    use jni::errors::ErrorKind;
    use jni::objects::{JClass, JObject, JString, JValue};
    use jni::strings::JNIString;
    use jni::sys::{jint, jlong, jstring};
    use jni::JNIEnv;

    use super::*;

    pub type WasiErrno = u16;

    pub const WASI_ERRNO_FAULT: WasiErrno = 21;
    pub const WASI_ERRNO_NOTRECOVERABLE: WasiErrno = 56;

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
        jwasi_env: JObject,
    ) -> () {
        let wasm_path: &String = &env
            .get_string(jwasm_path)
            .expect("invalid Java String")
            .into();
        let wasm_bin = read(wasm_path).expect(&format!("Failed to read {}", wasm_path));

        let import_object = generate_import_object(env, jwasi_env);

        let instance = instantiate(&wasm_bin, &import_object).unwrap();

        debug!("Calling _start");
        instance
            .exports
            .get::<Func<(), ()>>("_start")
            .unwrap()
            .call()
            .unwrap();
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

    /// Creates a Wasi [`ImportObject`] with [`wasmer::WasiState`] with the latest snapshot
    /// of WASI.
    fn generate_import_object(env: JNIEnv, jwasi_env: JObject) -> ImportObject {
        let state_gen = move || {
            fn state_destructor(data: *mut c_void) {
                unsafe {
                    drop(Box::from_raw(data as *mut i32));
                }
            }

            let state = Box::new(0);

            (
                Box::into_raw(state) as *mut c_void,
                state_destructor as fn(*mut c_void),
            )
        };

        generate_import_object_snapshot1_inner(env, jwasi_env, state_gen)
    }

    /// Combines a state generating function with the import list for snapshot 1
    fn generate_import_object_snapshot1_inner<F>(
        env: JNIEnv,
        jwasi_env0: JObject,
        state_gen: F,
    ) -> ImportObject
    where
        F: Fn() -> (*mut c_void, fn(*mut c_void)) + Send + Sync + 'static,
    {
        let jvm0 = Arc::new(env.get_java_vm().unwrap());
        let jwasi_env0 = Arc::new(env.new_global_ref(jwasi_env0).unwrap());

        let jvm = jvm0.clone();
        let jwasi_env = jwasi_env0.clone();
        let environ_get = move |_ctx: &mut Ctx,
                                environ: WasmPtr<WasmPtr<u8, Array>, Array>,
                                environ_buf: WasmPtr<u8, Array>|
              -> WasiErrno {
            let env = jvm
                .attach_current_thread()
                .expect("Failed o get JNIEnv for environ_get");
            call_jwasi_env_method(&env, jwasi_env.as_obj(), "environGet", "()S", &[])
        };

        let jvm = jvm0.clone();
        let jwasi_env = jwasi_env0.clone();
        let environ_sizes_get = move |_ctx: &mut Ctx,
                                      environ_count: WasmPtr<u32>,
                                      environ_buf_size: WasmPtr<u32>|
              -> WasiErrno {
            let env = jvm
                .attach_current_thread()
                .expect("Failed o get JNIEnv for environ_sizes_get");
            call_jwasi_env_method(&env, jwasi_env.as_obj(), "environSizesGet", "()S", &[])
        };

        let jvm = jvm0.clone();
        let jwasi_env = jwasi_env0.clone();
        let fd_prestat_get =
            move |ctx: &mut Ctx, fd: __wasi_fd_t, buf: WasmPtr<__wasi_prestat_t>| -> WasiErrno {
                let env = jvm
                    .attach_current_thread()
                    .expect("Failed o get JNIEnv for fd_prestat_get");
                debug!("wasi::fd_prestat_get: fd={}", fd);
                call_jwasi_env_method(
                    &env,
                    jwasi_env.as_obj(),
                    "fdPrestatGet",
                    "(ILjp/co/iijii/wasmonandroid/NativePointer;)S",
                    &[
                        JValue::Int(fd as jint),
                        JValue::Object(
                            unsafe {
                                to_jni_ptr(
                                    &env,
                                    &ctx.memory(0),
                                    "jp/co/iijii/wasmonandroid/NativePointer",
                                    buf,
                                )
                            }
                            .unwrap(),
                        ),
                    ],
                )
            };

        let jvm = jvm0.clone();
        let jwasi_env = jwasi_env0.clone();
        let fd_prestat_dir_name = move |ctx: &mut Ctx,
                                        fd: __wasi_fd_t,
                                        path: WasmPtr<u8, Array>,
                                        path_len: u32|
              -> WasiErrno {
            debug!(
                "wasi::fd_prestat_dir_name: fd={}, path_len={}",
                fd, path_len
            );
            let env = jvm
                .attach_current_thread()
                .expect("Failed o get JNIEnv for fd_prestat_dir_name");
            call_jwasi_env_method(
                &env,
                jwasi_env.as_obj(),
                "fdPrestatDirName",
                "(ILjp/co/iijii/wasmonandroid/wasi/I8ArrayWriter;I)S",
                &[
                    JValue::Int(fd as jint),
                    JValue::Object(
                        unsafe {
                            to_jni_ptr(
                                &env,
                                &ctx.memory(0),
                                "jp/co/iijii/wasmonandroid/wasi/I8ArrayWriter",
                                path,
                            )
                        }
                        .unwrap(),
                    ),
                    JValue::Int(path_len as jint),
                ],
            )
        };

        let jvm = jvm0.clone();
        let jwasi_env = jwasi_env0.clone();
        let fd_write = move |ctx: &mut Ctx,
                             fd: __wasi_fd_t,
                             iovs: WasmPtr<__wasi_ciovec_t, Array>,
                             iovs_len: u32,
                             nwritten: WasmPtr<u32>|
              -> WasiErrno {
            debug!("wasi::fd_write: fd={}, iovs_len={}", fd, iovs_len);
            let env = jvm
                .attach_current_thread()
                .expect("Failed o get JNIEnv for fd_write");
            let memory = ctx.memory(0);
            call_jwasi_env_method(
                &env,
                jwasi_env.as_obj(),
                "fdWrite",
                "(Ljp/co/iijii/wasmonandroid/wasi/WasiMemory;ILjp/co/iijii/wasmonandroid/NativeArray;ILjp/co/iijii/wasmonandroid/wasi/SizePointer;)S",
                &[
                    JValue::Object(
                        make_wasi_memory(
                            &env,
                            &memory,
                        )
                    ),
                    JValue::Int(fd as jint),
                    JValue::Object(
                        unsafe {
                            to_jni_ptr(
                                &env,
                                &memory,
                                "jp/co/iijii/wasmonandroid/NativeArray",
                                iovs,
                            )
                        }
                        .unwrap(),
                    ),
                    JValue::Int(iovs_len as jint),
                    JValue::Object(
                        unsafe {
                            to_jni_ptr(
                                &env,
                                &memory,
                                "jp/co/iijii/wasmonandroid/wasi/SizePointer",
                                nwritten,
                            )
                        }
                        .unwrap(),
                    ),
                ],
            )
        };

        imports! {
                state_gen,
                "wasi_snapshot_preview1" => {
                    //"args_get" => func!(args_get),
                    //"args_sizes_get" => func!(args_sizes_get),
                    //"clock_res_get" => func!(clock_res_get),
                    //"clock_time_get" => func!(clock_time_get),
                    "environ_get" => { func!(environ_get) },
                    "environ_sizes_get" => func!(environ_sizes_get),
                    //"fd_advise" => func!(fd_advise),
                    //"fd_allocate" => func!(fd_allocate),
                    //"fd_close" => func!(fd_close),
                    //"fd_datasync" => func!(fd_datasync),
                    //"fd_fdstat_get" => func!(fd_fdstat_get),
                    //"fd_fdstat_set_flags" => func!(fd_fdstat_set_flags),
                    //"fd_fdstat_set_rights" => func!(fd_fdstat_set_rights),
                    //"fd_filestat_get" => func!(fd_filestat_get),
                    //"fd_filestat_set_size" => func!(fd_filestat_set_size),
                    //"fd_filestat_set_times" => func!(fd_filestat_set_times),
                    //"fd_pread" => func!(fd_pread),
                    "fd_prestat_get" => func!(fd_prestat_get),
                    "fd_prestat_dir_name" => func!(fd_prestat_dir_name),
                    //"fd_pwrite" => func!(fd_pwrite),
                    //"fd_read" => func!(fd_read),
                    //"fd_readdir" => func!(fd_readdir),
                    //"fd_renumber" => func!(fd_renumber),
                    //"fd_seek" => func!(fd_seek),
                    //"fd_sync" => func!(fd_sync),
                    //"fd_tell" => func!(fd_tell),
                    "fd_write" => func!(fd_write),
                    //"path_create_directory" => func!(path_create_directory),
                    //"path_filestat_get" => func!(path_filestat_get),
                    //"path_filestat_set_times" => func!(path_filestat_set_times),
                    //"path_link" => func!(path_link),
                    //"path_open" => func!(path_open),
                    //"path_readlink" => func!(path_readlink),
                    //"path_remove_directory" => func!(path_remove_directory),
                    //"path_rename" => func!(path_rename),
                    //"path_symlink" => func!(path_symlink),
                    //"path_unlink_file" => func!(path_unlink_file),
                    //"poll_oneoff" => func!(poll_oneoff),
                    "proc_exit" => func!(proc_exit),
                    //"proc_raise" => func!(proc_raise),
                    //"random_get" => func!(random_get),
                    //"sched_yield" => func!(sched_yield),
                    //"sock_recv" => func!(sock_recv),
                    //"sock_send" => func!(sock_send),
                    //"sock_shutdown" => func!(sock_shutdown),
                },
        }
    }

    pub fn proc_exit(ctx: &mut Ctx, code: __wasi_exitcode_t) -> Result<Infallible, ExitCode> {
        debug!("wasi::proc_exit, {}", code);
        // Err(ExitCode { code })
        unimplemented!()
    }

    fn call_jwasi_env_method<S, T>(
        env: &JNIEnv,
        jwasi_env: JObject,
        name: S,
        sig: T,
        args: &[JValue],
    ) -> WasiErrno
    where
        S: Into<JNIString>,
        T: Into<JNIString> + AsRef<str>,
    {
        let result = env.call_method(jwasi_env, name, sig, args);
        match result {
            Ok(JValue::Short(errno)) => errno as WasiErrno,
            Err(jerr) => {
                let k = jerr.kind();
                if let ErrorKind::JavaException = k {
                    let jex = env
                        .exception_occurred()
                        .expect("Failed to get Java Exception!");
                    env.throw(jex).expect("Failed to throw Java Exception!");
                } else {
                    error!("Error from JNI: {:?}", k);
                }
                WASI_ERRNO_NOTRECOVERABLE
            }
            other => panic!(
                "Unexpected result from system call method of WasiEnv: {:?}",
                other
            ),
        }
    }

    #[inline]
    fn make_wasi_memory<'a>(env: &JNIEnv<'a>, memory: &Memory) -> JObject<'a> {
        let size = memory.size().bytes().0 as jlong;
        let raw_ptr = memory.view::<u8>().as_ptr() as jlong;
        env.new_object(
            "jp/co/iijii/wasmonandroid/wasi/WasiMemory",
            "(JJ)V",
            &[JValue::Long(raw_ptr), JValue::Long(size)],
        )
        .expect("Can't create a WasiMemory object!")
    }

    /// Ref.
    /// <https://github.com/wasmerio/wasmer/blob/2c44b700c883c03ca6efbb311591a1847f16d9dd/lib/runtime-core/src/memory/ptr.rs#L54-L65>
    #[inline]
    unsafe fn to_jni_ptr<'a, 'c, C, T, Item>(
        env: &JNIEnv<'a>,
        memory: &Memory,
        class: C,
        ptr: WasmPtr<T, Item>,
    ) -> Result<JObject<'a>, WasiErrno>
    where
        C: Desc<'a, JClass<'c>>,
        T: Copy,
    {
        let offset = ptr.offset() as usize;
        if offset + mem::size_of::<T>() >= memory.size().bytes().0 {
            return Err(WASI_ERRNO_FAULT);
        }
        let raw_ptr = align_pointer(
            memory.view::<u8>().as_ptr().add(offset) as usize,
            mem::align_of::<T>(),
        ) as jlong;
        Ok(env
            .new_object(class, "(J)V", &[JValue::Long(raw_ptr)])
            .expect("Can't create a pointer object!"))
    }

    /// Ref.
    /// <https://github.com/wasmerio/wasmer/blob/2c44b700c883c03ca6efbb311591a1847f16d9dd/lib/runtime-core/src/memory/ptr.rs#L44-L49>
    #[inline(always)]
    fn align_pointer(ptr: usize, align: usize) -> usize {
        // clears bits below aligment amount (assumes power of 2) to align pointer
        debug_assert!(align.count_ones() == 1);
        ptr & !(align - 1)
    }
}
