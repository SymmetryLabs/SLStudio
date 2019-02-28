#include <jni.h>

/* A struct that converts jstrings to c-style strings and uses RAII to make sure
   that all jstrings that are converted have their references released when the
   JniString object goes out of scope. */
struct JniString {
    JNIEnv *env;
    jstring jstr;
    const char *str;

    JniString(JNIEnv *env, jstring jstr) : env(env), jstr(jstr) {
        str = jstr == nullptr ? nullptr : env->GetStringUTFChars(jstr, 0);
    }

    ~JniString() {
        if (jstr != nullptr) {
            env->ReleaseStringUTFChars(jstr, str);
        }
    }

    operator const char*() {
        return str;
    }

    operator const std::string() {
        return std::string(str);
    }
};

/* Same as JniString, but for read-only float arrays */
struct JniFloatArray {
    JNIEnv *env;
    jfloatArray jarr;
    float *arr;

    JniFloatArray(JNIEnv *env, jfloatArray jarr) : env(env), jarr(jarr) {
        arr = env->GetFloatArrayElements(jarr, nullptr);
    }

    ~JniFloatArray() {
        env->ReleaseFloatArrayElements(jarr, arr, JNI_ABORT);
    }

    operator float*() {
        return arr;
    }

    jsize size() {
        return env->GetArrayLength(jarr);
    }
};

/* Same as JniString, but for read-only float arrays */
struct JniIntArray {
    JNIEnv *env;
    jintArray jarr;
    jint *arr;

    JniIntArray(JNIEnv *env, jintArray jarr) : env(env), jarr(jarr) {
        arr = env->GetIntArrayElements(jarr, nullptr);
    }

    ~JniIntArray() {
        env->ReleaseIntArrayElements(jarr, arr, JNI_ABORT);
    }

    operator jint*() {
        return arr;
    }

    jsize size() {
        return env->GetArrayLength(jarr);
    }
};
