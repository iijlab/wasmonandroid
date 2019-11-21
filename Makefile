.PHONY: install build clean

install: build
	cp cargo/target/aarch64-linux-android/release/libgreeting.so android/app/src/main/jniLibs/arm64-v8a/libgreeting.so
	cp cargo/target/armv7-linux-androideabi/release/libgreeting.so android/app/src/main/jniLibs/armeabi-v7a/libgreeting.so
	cp cargo/target/i686-linux-android/release/libgreeting.so android/app/src/main/jniLibs/x86/libgreeting.so

build:
	cd cargo && \
		cargo build -Z unstable-options --target aarch64-linux-android --out-dir ../android/app/src/main/jniLibs/arm64-v8a && \
		cargo build -Z unstable-options --target i686-linux-android --out-dir ../android/app/src/main/jniLibs/x86
		#cargo build -Z unstable-options --target armv7-linux-androideabi --out-dir ../android/app/src/main/jniLibs/armeabi-v7a && \

clean:
	cd cargo && cargo clean
