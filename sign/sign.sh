alias java8=/Users/youxingz/Library/Java/JavaVirtualMachines/corretto-1.8.0_362/Contents/Home/bin/java
java8 -jar signapk.jar platform.x509.pem platform.pk8 ../sample/build/outputs/apk/debug/sample-debug.apk ./signed-sample-debug.apk
adb install ./signed-sample-debug.apk
