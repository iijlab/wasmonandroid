.PHONY: install build clean

build:
	cd cargo && \
		cargo build --target x86_64-linux-android  -Z unstable-options --out-dir ../android/app/src/main/jniLibs/x86_64 && \
		cargo build --target aarch64-linux-android -Z unstable-options --out-dir ../android/app/src/main/jniLibs/arm64-v8a && \
		echo All done!
		#cargo build -Z unstable-options --target armv7-linux-androideabi --out-dir ../android/app/src/main/jniLibs/armeabi-v7a && \
		#cargo build -Z unstable-options --target i686-linux-android --out-dir ../android/app/src/main/jniLibs/x86 && \

clean:
	cd cargo && cargo clean
