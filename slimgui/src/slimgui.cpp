#include <GLFW/glfw3.h>
#include <iostream>

#include "com_symmetrylabs_slstudio_ui_gdx_UI.h"
#include "handle.hpp"
#include "imgui.h"
#include "examples/imgui_impl_opengl2.h"
#include "examples/imgui_impl_glfw.h"

#define MAX_INPUT_LENGTH 511

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_init(JNIEnv *env, jclass cls, jlong windowHandle) {
	jfieldID fid;

	fid = env->GetStaticFieldID(cls, "TREE_FLAG_LEAF", "I");
	env->SetStaticIntField(cls, fid, ImGuiTreeNodeFlags_Leaf);
	fid = env->GetStaticFieldID(cls, "TREE_FLAG_DEFAULT_OPEN", "I");
	env->SetStaticIntField(cls, fid, ImGuiTreeNodeFlags_DefaultOpen);
	fid = env->GetStaticFieldID(cls, "TREE_FLAG_SELECTED", "I");
	env->SetStaticIntField(cls, fid, ImGuiTreeNodeFlags_Selected);

	glfwInit();
	GLFWwindow* window = reinterpret_cast<GLFWwindow*>(windowHandle);
	ImGui::CreateContext();
	bool ok = ImGui_ImplGlfw_InitForOpenGL(window, false);
	if (!ok) {
		std::cout << "failed to init glfw" << std::endl;
		return 0;
	}
	ok = ImGui_ImplOpenGL2_Init();
	if (!ok) {
		std::cout << "failed to init opengl2" << std::endl;
		return 0;
	}
	std::cout << "successfully initialized" << std::endl;
	return 1;
}

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_gdx_UI_newFrame(JNIEnv *, jclass) {
	ImGui_ImplOpenGL2_NewFrame();
	ImGui_ImplGlfw_NewFrame();
	ImGui::NewFrame();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_render(JNIEnv *, jclass) {
	ImGui::Render();
	ImGui_ImplOpenGL2_RenderDrawData(ImGui::GetDrawData());
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_shutdown(JNIEnv *, jclass) {
	ImGui_ImplOpenGL2_Shutdown();
	ImGui_ImplGlfw_Shutdown();
	ImGui::DestroyContext();
	return 1;
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_begin(JNIEnv *env, jclass, jstring jstr) {
	const char *str = env->GetStringUTFChars(jstr, 0);
	ImGui::Begin(str);
	env->ReleaseStringUTFChars(jstr, str);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_end(JNIEnv *, jclass) {
	ImGui::End();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_text(JNIEnv *env, jclass, jstring jstr) {
	const char *str = env->GetStringUTFChars(jstr, 0);
	ImGui::Text(str);
	env->ReleaseStringUTFChars(jstr, str);
}

JNIEXPORT jstring JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_inputText(
	JNIEnv *env, jclass, jstring jlabel, jstring jstr) {
	const char *label = env->GetStringUTFChars(jlabel, 0);
	const char *str = env->GetStringUTFChars(jstr, 0);
	char input_buf[MAX_INPUT_LENGTH + 1] = {0};
	strncpy(input_buf, str, MAX_INPUT_LENGTH);
	ImGui::InputText(label, input_buf, MAX_INPUT_LENGTH + 1);
	env->ReleaseStringUTFChars(jstr, str);
	env->ReleaseStringUTFChars(jlabel, label);
	return env->NewStringUTF(input_buf);
}

JNIEXPORT jfloat JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_sliderFloat(
	JNIEnv * env, jclass, jstring jlabel, jfloat v, jfloat v0, jfloat v1) {
	const char *label = env->GetStringUTFChars(jlabel, 0);
	jfloat res = v;
	ImGui::SliderFloat(label, &res, v0, v1);
	env->ReleaseStringUTFChars(jlabel, label);
	return res;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_treeNode(
	JNIEnv *env, jclass, jstring jid, jint flags, jstring jlabel) {
	const char *id = env->GetStringUTFChars(jid, 0);
	const char *label = env->GetStringUTFChars(jlabel, 0);
	bool res = ImGui::TreeNodeEx(id, flags, label);
	env->ReleaseStringUTFChars(jid, id);
	env->ReleaseStringUTFChars(jlabel, label);
	return res ? 1 : 0;
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_treePop(JNIEnv *, jclass) {
	ImGui::TreePop();
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_isItemClicked(JNIEnv *, jclass, jint button) {
	return ImGui::IsItemClicked(button) ? 1 : 0;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_wantCaptureKeyboard(JNIEnv *, jclass) {
	return ImGui::GetIO().WantCaptureKeyboard;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_wantCaptureMouse(JNIEnv *, jclass) {
	return ImGui::GetIO().WantCaptureMouse;
}

void update_modifiers() {
	ImGuiIO &io = ImGui::GetIO();
	io.KeyCtrl = io.KeysDown[GLFW_KEY_LEFT_CONTROL] || io.KeysDown[GLFW_KEY_RIGHT_CONTROL];
	io.KeyShift = io.KeysDown[GLFW_KEY_LEFT_SHIFT] || io.KeysDown[GLFW_KEY_RIGHT_SHIFT];
	io.KeyAlt = io.KeysDown[GLFW_KEY_LEFT_ALT] || io.KeysDown[GLFW_KEY_RIGHT_ALT];
	io.KeySuper = io.KeysDown[GLFW_KEY_LEFT_SUPER] || io.KeysDown[GLFW_KEY_RIGHT_SUPER];
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_keyDown(JNIEnv *, jclass, jint keycode) {
	ImGui::GetIO().KeysDown[keycode] = true;
	update_modifiers();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_keyUp(JNIEnv *, jclass, jint keycode) {
	ImGui::GetIO().KeysDown[keycode] = false;
	update_modifiers();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_addInputCharacter(JNIEnv *, jclass, jchar c) {
	ImGui::GetIO().AddInputCharacter((ImWchar) c);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_scrolled(JNIEnv *, jclass, jfloat amount) {
	ImGui::GetIO().MouseWheel -= amount;
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_gdx_UI_showDemoWindow(JNIEnv *, jclass) {
	ImGui::ShowDemoWindow();
}
