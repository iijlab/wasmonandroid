# WASM on Android

*This product is work in progress.*

## Goals

- Antomation on Android devices in any languages that can be compiled into WASM.
    - Similar to [SL4A](https://github.com/damonkohler/sl4a) for WASM.
- Provide a frontend for several popular WebAssembly runtimes for easier experiment with WASM.
    - Currently only [Wasmer](https://github.com/wasmerio/wasmer) (with its singlepass backend).  
      So this product's supported architectures are limited to ones it supportes (`x86_64` and `AArch64` so far).
