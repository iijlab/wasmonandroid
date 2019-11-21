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

    // HostRef is available only in the later version of wasmtime-api
    // use wasmtime_api::{Config, HostRef, Engine, Store, Module};
    use wasmtime_api as wasmtime;

    use std::cell::RefCell;
    use std::ffi::CString;
    use std::fs::read;
    use std::rc::Rc;

    use log::Level;
    use log::{debug, error};

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
    pub extern "C" fn Java_jp_co_iijii_wasmonandroid_Wasmtime_runWasm(
        env: JNIEnv,
        _: JClass,
        jwasm_path: JString,
    ) -> () {
        // Initialize.
        let engine = Rc::new(RefCell::new(wasmtime::Engine::new(
            wasmtime::Config::default(),
        )));
        let store = Rc::new(RefCell::new(wasmtime::Store::new(engine)));

        let wasm_path: &String = &env
            .get_string(jwasm_path)
            .expect("invalid Java String")
            .into();
        let wasm_bin = read(wasm_path).expect(&format!("Failed to read {}", wasm_path));

        let module = Rc::new(RefCell::new(
            wasmtime::Module::new(store.clone(), &wasm_bin)
                .expect(&format!("Can't compile {:?}", wasm_bin)),
        ));

        let gcd_index = module
            .borrow()
            .exports()
            .iter()
            .enumerate()
            .find(|(_, export)| export.name().to_string() == "gcd")
            .expect("gcd function not found!")
            .0;

        let instance = Rc::new(RefCell::new(
            wasmtime::Instance::new(store.clone(), module, &[])
                .expect("Failed to instantiate a module."),
        ));

        let gcd = instance.borrow().exports()[gcd_index]
            .borrow()
            .func()
            .clone();

        debug!("Running WASM");

        let result = gcd
            .borrow()
            .call(&[wasmtime::Val::from(6i32), wasmtime::Val::from(27i32)])
            .expect("Error calling a function of wasm");

        error!("{:?}", result);
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
}
