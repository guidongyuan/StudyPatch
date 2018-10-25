#include <jni.h>

// 执行合并差分包，实际上就是bspatch.c中的main()方法
// 由于native-lib.cpp为c++，bspatch.c为c，所以需要使用extern
extern "C" {
extern int main(int argc,char * argv[]);
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_guidongyuan_studypatch_MainActivity_native_1bspatch(JNIEnv *env, jobject instance,
                                                            jstring oldApk_, jstring newApk_,
                                                            jstring patchFile_) {
    const char *oldApk = env->GetStringUTFChars(oldApk_, 0);
    const char *newApk = env->GetStringUTFChars(newApk_, 0);
    const char *pathFile = env->GetStringUTFChars(patchFile_, 0);

    char * argv[4] = {"", const_cast<char *>(oldApk), const_cast<char *>(newApk),
                      const_cast<char *>(pathFile)};

    main(4, argv);

    env->ReleaseStringUTFChars(oldApk_, oldApk);
    env->ReleaseStringUTFChars(newApk_, newApk);
    env->ReleaseStringUTFChars(patchFile_, pathFile);
}
