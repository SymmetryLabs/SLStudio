#include <GLFW/glfw3.h>
#include "GL/gl3w.h"
#include <iostream>

#include "com_symmetrylabs_slstudio_ui_v2_UI.h"
#include "handle.hpp"
#include "imgui.h"
#include "imgui_impl_opengl3.h"
#include "imgui_impl_glfw.h"

#define MAX_INPUT_LENGTH 511

/* A struct that converts jstrings to c-style strings and uses RAII to make sure
   that all jstrings that are converted have their references released when the
   JniString object goes out of scope. */
struct JniString {
    JNIEnv *env;
    jstring jstr;
    const char *str;

    JniString(JNIEnv *env, jstring jstr) : env(env), jstr(jstr) {
        str = env->GetStringUTFChars(jstr, 0);
    }

    ~JniString() {
        env->ReleaseStringUTFChars(jstr, str);
    }

    operator const char*() {
        return str;
    }

    operator const std::string() {
        return std::string(str);
    }
};

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_init(JNIEnv *env, jclass cls, jlong windowHandle) {
	jfieldID fid;

	fid = env->GetStaticFieldID(cls, "TREE_FLAG_LEAF", "I");
	env->SetStaticIntField(cls, fid, ImGuiTreeNodeFlags_Leaf);
	fid = env->GetStaticFieldID(cls, "TREE_FLAG_DEFAULT_OPEN", "I");
	env->SetStaticIntField(cls, fid, ImGuiTreeNodeFlags_DefaultOpen);
	fid = env->GetStaticFieldID(cls, "TREE_FLAG_SELECTED", "I");
	env->SetStaticIntField(cls, fid, ImGuiTreeNodeFlags_Selected);
	fid = env->GetStaticFieldID(cls, "WINDOW_HORIZ_SCROLL", "I");
	env->SetStaticIntField(cls, fid, ImGuiWindowFlags_HorizontalScrollbar);

	if (gl3wInit()) {
		std::cout << "failed to init gl3w" << std::endl;
		return 0;
	}
	if (!gl3wIsSupported(4, 1)) {
		std::cout << "OpenGL 4.1 is not supported" << std::endl;
		return 0;
	}

	glfwInit();
	GLFWwindow* window = reinterpret_cast<GLFWwindow*>(windowHandle);
	ImGui::CreateContext();
	bool ok = ImGui_ImplGlfw_InitForOpenGL(window, false);
	if (!ok) {
		std::cout << "failed to init glfw" << std::endl;
		return 0;
	}
	ok = ImGui_ImplOpenGL3_Init("#version 150");
	if (!ok) {
		std::cout << "failed to init opengl3" << std::endl;
		return 0;
	}

	ImVec4* colors = ImGui::GetStyle().Colors;
	colors[ImGuiCol_Text]                   = ImVec4(1.00f, 1.00f, 1.00f, 1.00f);
	colors[ImGuiCol_TextDisabled]           = ImVec4(0.50f, 0.50f, 0.50f, 1.00f);
	colors[ImGuiCol_WindowBg]               = ImVec4(0.06f, 0.06f, 0.06f, 0.94f);
	colors[ImGuiCol_ChildBg]                = ImVec4(1.00f, 1.00f, 1.00f, 0.00f);
	colors[ImGuiCol_PopupBg]                = ImVec4(0.08f, 0.08f, 0.08f, 0.94f);
	colors[ImGuiCol_Border]                 = ImVec4(0.43f, 0.43f, 0.50f, 0.50f);
	colors[ImGuiCol_BorderShadow]           = ImVec4(0.00f, 0.00f, 0.00f, 0.00f);
	colors[ImGuiCol_FrameBg]                = ImVec4(0.20f, 0.21f, 0.22f, 0.54f);
	colors[ImGuiCol_FrameBgHovered]         = ImVec4(0.40f, 0.40f, 0.40f, 0.40f);
	colors[ImGuiCol_FrameBgActive]          = ImVec4(0.18f, 0.18f, 0.18f, 0.67f);
	colors[ImGuiCol_TitleBg]                = ImVec4(0.04f, 0.04f, 0.04f, 1.00f);
	colors[ImGuiCol_TitleBgActive]          = ImVec4(0.29f, 0.29f, 0.29f, 1.00f);
	colors[ImGuiCol_TitleBgCollapsed]       = ImVec4(0.00f, 0.00f, 0.00f, 0.51f);
	colors[ImGuiCol_MenuBarBg]              = ImVec4(0.14f, 0.14f, 0.14f, 1.00f);
	colors[ImGuiCol_ScrollbarBg]            = ImVec4(0.02f, 0.02f, 0.02f, 0.53f);
	colors[ImGuiCol_ScrollbarGrab]          = ImVec4(0.31f, 0.31f, 0.31f, 1.00f);
	colors[ImGuiCol_ScrollbarGrabHovered]   = ImVec4(0.41f, 0.41f, 0.41f, 1.00f);
	colors[ImGuiCol_ScrollbarGrabActive]    = ImVec4(0.51f, 0.51f, 0.51f, 1.00f);
	colors[ImGuiCol_CheckMark]              = ImVec4(0.94f, 0.94f, 0.94f, 1.00f);
	colors[ImGuiCol_SliderGrab]             = ImVec4(0.51f, 0.51f, 0.51f, 1.00f);
	colors[ImGuiCol_SliderGrabActive]       = ImVec4(0.86f, 0.86f, 0.86f, 1.00f);
	colors[ImGuiCol_Button]                 = ImVec4(0.44f, 0.44f, 0.44f, 0.40f);
	colors[ImGuiCol_ButtonHovered]          = ImVec4(0.46f, 0.47f, 0.48f, 1.00f);
	colors[ImGuiCol_ButtonActive]           = ImVec4(0.42f, 0.42f, 0.42f, 1.00f);
	colors[ImGuiCol_Header]                 = ImVec4(0.70f, 0.70f, 0.70f, 0.31f);
	colors[ImGuiCol_HeaderHovered]          = ImVec4(0.70f, 0.70f, 0.70f, 0.80f);
	colors[ImGuiCol_HeaderActive]           = ImVec4(0.48f, 0.50f, 0.52f, 1.00f);
	colors[ImGuiCol_Separator]              = ImVec4(0.43f, 0.43f, 0.50f, 0.50f);
	colors[ImGuiCol_SeparatorHovered]       = ImVec4(0.72f, 0.72f, 0.72f, 0.78f);
	colors[ImGuiCol_SeparatorActive]        = ImVec4(0.51f, 0.51f, 0.51f, 1.00f);
	colors[ImGuiCol_ResizeGrip]             = ImVec4(0.91f, 0.91f, 0.91f, 0.25f);
	colors[ImGuiCol_ResizeGripHovered]      = ImVec4(0.81f, 0.81f, 0.81f, 0.67f);
	colors[ImGuiCol_ResizeGripActive]       = ImVec4(0.46f, 0.46f, 0.46f, 0.95f);
	colors[ImGuiCol_PlotLines]              = ImVec4(0.61f, 0.61f, 0.61f, 1.00f);
	colors[ImGuiCol_PlotLinesHovered]       = ImVec4(1.00f, 0.43f, 0.35f, 1.00f);
	colors[ImGuiCol_PlotHistogram]          = ImVec4(0.73f, 0.60f, 0.15f, 1.00f);
	colors[ImGuiCol_PlotHistogramHovered]   = ImVec4(1.00f, 0.60f, 0.00f, 1.00f);
	colors[ImGuiCol_TextSelectedBg]         = ImVec4(0.87f, 0.87f, 0.87f, 0.35f);
	colors[ImGuiCol_ModalWindowDarkening]   = ImVec4(0.80f, 0.80f, 0.80f, 0.35f);
	colors[ImGuiCol_DragDropTarget]         = ImVec4(1.00f, 1.00f, 0.00f, 0.90f);
	colors[ImGuiCol_NavHighlight]           = ImVec4(0.60f, 0.60f, 0.60f, 1.00f);
	colors[ImGuiCol_NavWindowingHighlight]  = ImVec4(1.00f, 1.00f, 1.00f, 0.70f);
	colors[ImGuiCol_Tab]                    = ImVec4(0.44f, 0.44f, 0.44f, 0.40f);
	colors[ImGuiCol_TabHovered]             = ImVec4(0.46f, 0.47f, 0.48f, 1.00f);
	colors[ImGuiCol_TabActive]              = ImVec4(0.42f, 0.42f, 0.42f, 1.00f);
	colors[ImGuiCol_TabUnfocused]           = ImVec4(0.24f, 0.24f, 0.24f, 0.40f);
	colors[ImGuiCol_TabUnfocusedActive]     = ImVec4(0.22f, 0.22f, 0.22f, 1.00f);
	colors[ImGuiCol_DockingPreview]         = ImVec4(1.00f, 1.00f, 1.00f, 1.00f);

	ImGuiIO &io = ImGui::GetIO();
	io.ConfigFlags |= ImGuiConfigFlags_DockingEnable;
	io.ConfigWindowsResizeFromEdges = true;
	io.ConfigDockingTabBarOnSingleWindows = true;

	ImGui::PushStyleVar(ImGuiStyleVar_WindowRounding, 0.0f);

	return 1;
}

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_newFrame(JNIEnv *, jclass) {
	ImGui_ImplOpenGL3_NewFrame();
	ImGui_ImplGlfw_NewFrame();
	ImGui::NewFrame();

	ImGuiViewport* viewport = ImGui::GetMainViewport();
	ImGui::SetNextWindowPos(viewport->Pos);
	ImGui::SetNextWindowSize(viewport->Size);
	ImGui::SetNextWindowViewport(viewport->ID);
	ImGui::SetNextWindowBgAlpha(0.0f);

	/* All of this sets up the central "window" through which you see the previz; setting it
		 up like this means that you can use the docking utilities on the main window */
	ImGuiWindowFlags window_flags = ImGuiWindowFlags_MenuBar | ImGuiWindowFlags_NoDocking;
	window_flags |= ImGuiWindowFlags_NoTitleBar | ImGuiWindowFlags_NoCollapse | ImGuiWindowFlags_NoResize | ImGuiWindowFlags_NoMove;
	window_flags |= ImGuiWindowFlags_NoBringToFrontOnFocus | ImGuiWindowFlags_NoNavFocus;

	ImGui::PushStyleVar(ImGuiStyleVar_WindowBorderSize, 0.0f);
	ImGui::PushStyleVar(ImGuiStyleVar_WindowPadding, ImVec2(0.0f, 0.0f));
	ImGui::Begin("MainDockSpaceWindow", NULL, window_flags);
	ImGui::PopStyleVar(2);

	ImGuiID dockspace_id = ImGui::GetID("MainDockSpace");
	ImGuiDockNodeFlags dockspace_flags = ImGuiDockNodeFlags_PassthruDockspace;
	ImGui::DockSpace(dockspace_id, ImVec2(0.0f, 0.0f), dockspace_flags);
	ImGui::End();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_render(JNIEnv *, jclass) {
	ImGui::Render();
	ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_shutdown(JNIEnv *, jclass) {
	ImGui_ImplOpenGL3_Shutdown();
	ImGui_ImplGlfw_Shutdown();
	ImGui::DestroyContext();
	return 1;
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_setNextWindowDefaults(
	JNIEnv *env, jclass, jint x, jint y, jint w, jint h) {
	ImGui::SetNextWindowSize(ImVec2(w, h), ImGuiCond_FirstUseEver);
	ImGui::SetNextWindowPos(ImVec2(x, y), ImGuiCond_FirstUseEver);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_setNextWindowDefaultToCursor(
	JNIEnv *env, jclass, jint w, jint h) {
	ImGui::SetNextWindowSize(ImVec2(w, h), ImGuiCond_FirstUseEver);
	ImGui::SetNextWindowPos(ImGui::GetCursorScreenPos(), ImGuiCond_FirstUseEver);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_setNextWindowContentSize(JNIEnv *, jclass, jint w, jint h) {
	ImGui::SetNextWindowContentSize(ImVec2(w, h));
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_begin(JNIEnv *env, jclass, jstring jstr) {
    JniString str(env, jstr);
	ImGui::Begin(str);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_beginDocked(JNIEnv *env, jclass, jstring jstr) {
    JniString str(env, jstr);
	ImGuiIO &io = ImGui::GetIO();
	ImGui::SetNextWindowPos(ImVec2(0, io.DisplaySize.y), ImGuiCond_Always, ImVec2(1, 1));
	ImGui::SetNextWindowSize(ImVec2(io.DisplaySize.x, 300));
	int flags = ImGuiWindowFlags_NoMove |
		ImGuiWindowFlags_NoTitleBar |
		ImGuiWindowFlags_NoResize |
		ImGuiWindowFlags_AlwaysAutoResize |
		ImGuiWindowFlags_NoSavedSettings |
		ImGuiWindowFlags_NoFocusOnAppearing |
		ImGuiWindowFlags_NoNav;
	ImGui::Begin(str, NULL, flags);
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_beginClosable(JNIEnv *env, jclass, jstring jstr) {
    JniString str(env, jstr);
	bool isOpen = true;
	ImGui::Begin(str, &isOpen);
	return isOpen ? 1 : 0;
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_end(JNIEnv *, jclass) {
	ImGui::End();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_sameLine(JNIEnv *, jclass) {
	ImGui::SameLine();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_columnsStart(JNIEnv *env, jclass, jint num, jstring jlabel) {
    JniString label(env, jlabel);
	ImGui::Columns(num, label);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_nextColumn(JNIEnv *, jclass) {
	ImGui::NextColumn();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_columnsEnd(JNIEnv *, jclass) {
	ImGui::Columns(1);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_separator(JNIEnv *, jclass) {
	ImGui::Separator();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_spacing(JNIEnv *, jclass) {
	ImGui::Dummy(ImVec2(5, 5));
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_beginChild(
	JNIEnv *env, jclass, jstring jid, jboolean border, jint flags) {
    JniString id(env, jid);
	bool res = ImGui::BeginChild(id, ImVec2(0, 0), border == 1, flags);
	return res ? 1 : 0;

}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_endChild(JNIEnv *, jclass) {
	ImGui::EndChild();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_beginGroup(JNIEnv *, jclass) {
	ImGui::BeginGroup();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_endGroup(JNIEnv *, jclass) {
	ImGui::EndGroup();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_text(JNIEnv *env, jclass, jstring jstr) {
    JniString str(env, jstr);
	ImGui::Text("%s", str.str);
}


JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_labelText(JNIEnv *env, jclass, jstring jlabel, jstring jtext) {
	if (env->IsSameObject(jtext, NULL)) {
		env->ThrowNew(env->FindClass("java/lang/NullPointerException"), "supplied text in labelText is null");
		return;
	}
    JniString text(env, jtext);
    JniString label(env, jlabel);
	ImGui::LabelText(label, "%s", text.str);
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_button(JNIEnv *env, jclass, jstring jstr) {
    JniString str(env, jstr);
	bool res = ImGui::Button(str);
	return res ? 1 : 0;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_checkbox(JNIEnv *env, jclass, jstring jlabel, jboolean v) {
    JniString label(env, jlabel);
	bool res = v == 1 ? true : false;
	ImGui::Checkbox(label, &res);
	return res ? 1 : 0;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_selectable(JNIEnv *env, jclass, jstring jlabel, jboolean v) {
    JniString label(env, jlabel);
	bool res = v == 1 ? true : false;
	ImGui::Selectable(label, &res);
	return res ? 1 : 0;
}

JNIEXPORT jstring JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_inputText(JNIEnv *env, jclass, jstring jlabel, jstring jstr) {
    JniString label(env, jlabel);
    JniString str(env, jstr);
	char input_buf[MAX_INPUT_LENGTH + 1] = {0};
	strncpy(input_buf, str, MAX_INPUT_LENGTH);
	ImGui::InputText(label, input_buf, MAX_INPUT_LENGTH + 1);
	return env->NewStringUTF(input_buf);
}

JNIEXPORT jint JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_colorPicker(JNIEnv *env, jclass, jstring jlabel, jint jcolor) {
    JniString label(env, jlabel);
	unsigned int c = static_cast<unsigned int>(jcolor);
	float color[] {
		(float)((c >> 16) & 0xFF) / 255,
		(float)((c >>  8) & 0xFF) / 255,
		(float)((c      ) & 0xFF) / 255,
		255.f};
	ImGui::ColorEdit3(label, color, ImGuiColorEditFlags_HSV | ImGuiColorEditFlags_Float);
	unsigned int res =
		0xFF000000 |
		(0xFF & (int)(color[0] * 255)) << 16 |
		(0xFF & (int)(color[1] * 255)) <<  8 |
		(0xFF & (int)(color[2] * 255));
	return static_cast<jint>(res);
}

JNIEXPORT jfloat JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_sliderFloat(
	JNIEnv * env, jclass, jstring jlabel, jfloat v, jfloat v0, jfloat v1, jboolean vert) {
    JniString label(env, jlabel);
	jfloat res = v;
	if (vert) {
		ImGui::VSliderFloat(label, ImVec2(20, 120), &res, v0, v1);
	} else {
		ImGui::SliderFloat(label, &res, v0, v1);
	}
	return res;
}

JNIEXPORT jint JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_sliderInt(
	JNIEnv *env, jclass, jstring jlabel, jint v, jint v0, jint v1) {
    JniString label(env, jlabel);
	int res = v;
	ImGui::SliderInt(label, &res, v0, v1);
	return res;
}

JNIEXPORT jint JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_combo(
	JNIEnv *env, jclass, jstring jlabel, jint selected, jobjectArray joptions) {
	jsize optionsLen = env->GetArrayLength(joptions);
	const char **options = new const char*[optionsLen];
	for (int i = 0; i < optionsLen; i++) {
		jstring joption = (jstring) env->GetObjectArrayElement(joptions, i);
		options[i] = env->GetStringUTFChars(joption, 0);
	}
	const char *label = env->GetStringUTFChars(jlabel, 0);

	int res = selected;
	ImGui::Combo(label, &res, options, optionsLen);

	for (int i = 0; i < optionsLen; i++) {
		jstring joption = (jstring) env->GetObjectArrayElement(joptions, i);
		env->ReleaseStringUTFChars(joption, options[i]);
	}
	env->ReleaseStringUTFChars(jlabel, label);
	delete[] options;

	return res;
}

JNIEXPORT jfloat JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_floatBox(JNIEnv *env, jclass, jstring jlabel, jfloat v) {
    JniString label(env, jlabel);
	jfloat res = v;
	ImGui::DragFloat(label, &res);
	return res;
}

JNIEXPORT jfloat JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_knobFloat(JNIEnv *env, jclass cls, jstring jlabel, jfloat vf, jfloat v0, jfloat v1) {
    JniString label(env, jlabel);
    ImGui::Knob(label, &vf, v0, v1);
	if (ImGui::BeginPopupContextItem()) {
		ImGui::InputFloat(label, &vf, 0, 0, "%.2f", 0);
        vf = vf < v0 ? v0 : vf > v1 ? v1 : vf;
		ImGui::EndPopup();
	}
    return vf;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_beginMainMenuBar(JNIEnv *env, jclass) {
	return ImGui::BeginMainMenuBar() ? 1 : 0;
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_endMainMenuBar(JNIEnv *, jclass) {
	return ImGui::EndMainMenuBar();
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_beginMenu(JNIEnv *env, jclass, jstring jlabel) {
    JniString label(env, jlabel);
	bool res = ImGui::BeginMenu(label);
	return res ? 1 : 0;
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_endMenu(JNIEnv *, jclass) {
	ImGui::EndMenu();
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_menuItem(
	JNIEnv *env, jclass, jstring jlabel, jstring jshortcut,
	jboolean selected, jboolean enabled) {
	const char *shortcut =
		jshortcut == NULL ? NULL : env->GetStringUTFChars(jshortcut, 0);
    JniString label(env, jlabel);

	bool res = ImGui::MenuItem(label, shortcut, selected == 1, enabled == 1);

	if (shortcut != NULL) {
		env->ReleaseStringUTFChars(jshortcut, shortcut);
	}
	return res ? 1 : 0;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_treeNode(
	JNIEnv *env, jclass, jstring jid, jint flags, jstring jlabel) {
    JniString id(env, jid);
    JniString label(env, jlabel);
	bool res = ImGui::TreeNodeEx(id, flags, "%s", label.str);
	return res ? 1 : 0;
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_treePop(JNIEnv *, jclass) {
	ImGui::TreePop();
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_isItemClicked(JNIEnv *, jclass, jint button) {
	return ImGui::IsItemClicked(button) ? 1 : 0;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_isItemActive(JNIEnv *, jclass) {
	return ImGui::IsItemActive() ? 1 : 0;
}

JNIEXPORT jfloat JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_getFrameRate(JNIEnv *, jclass) {
	return ImGui::GetIO().Framerate;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_wantCaptureKeyboard(JNIEnv *, jclass) {
	return ImGui::GetIO().WantCaptureKeyboard;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_wantCaptureMouse(JNIEnv *, jclass) {
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
Java_com_symmetrylabs_slstudio_ui_v2_UI_keyDown(JNIEnv *, jclass, jint keycode) {
	ImGui::GetIO().KeysDown[keycode] = true;
	update_modifiers();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_keyUp(JNIEnv *, jclass, jint keycode) {
	ImGui::GetIO().KeysDown[keycode] = false;
	update_modifiers();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_addInputCharacter(JNIEnv *, jclass, jchar c) {
	ImGui::GetIO().AddInputCharacter((ImWchar) c);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_scrolled(JNIEnv *, jclass, jfloat amount) {
	ImGui::GetIO().MouseWheel -= amount;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_showDemoWindow(JNIEnv *, jclass) {
	bool open = true;
	ImGui::ShowDemoWindow(&open);
	return open ? 1 : 0;
}
