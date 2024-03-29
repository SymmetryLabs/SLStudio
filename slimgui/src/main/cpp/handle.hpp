#pragma once

jfieldID getHandleField(JNIEnv *env, jobject obj) {
	jclass c = env->GetObjectClass(obj);
	return env->GetFieldID(c, "nativeHandle", "J");
}

template <typename T>
T *getHandle(JNIEnv *env, jobject obj) {
	jlong handle = env->GetLongField(obj, getHandleField(env, obj));
	return reinterpret_cast<T *>(handle);
}

template <typename T>
void setHandle(JNIEnv *env, jobject obj, T *t) {
	jlong handle = reinterpret_cast<jlong>(t);
	env->SetLongField(obj, getHandleField(env, obj), handle);
}
