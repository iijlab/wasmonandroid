.PHONY: install build clean

install: build
	cp cargo/target/aarch64-linux-android/release/libgreeting.so android/app/src/main/jniLibs/arm64/libgreeting.so
	cp cargo/target/armv7-linux-androideabi/release/libgreeting.so android/app/src/main/jniLibs/armeabi/libgreeting.so
	cp cargo/target/i686-linux-android/release/libgreeting.so android/app/src/main/jniLibs/x86/libgreeting.so

build:
	cd cargo && \
		cargo build --target aarch64-linux-android --release && \
		cargo build --target armv7-linux-androideabi --release && \
		cargo build --target i686-linux-android --release

clean:
	cd cargo && cargo clean
