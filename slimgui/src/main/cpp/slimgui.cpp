#include <GLFW/glfw3.h>
#include "GL/gl3w.h"
#include <iostream>

#include "com_symmetrylabs_slstudio_ui_v2_UI.h"
#include "handle.hpp"
#include "imgui.h"
#include "imgui_internal.h"
#include "imgui_impl_opengl3.h"
#include "imgui_impl_glfw.h"
#include "jniutils.hpp"
#include "widgets_symmetry.h"

#define MAX_INPUT_LENGTH 511

static float LoadedDensity = 0;
static jobject DragObjectReference = nullptr;

void LoadSlimguiStyle(float density) {
    ImGuiStyle style;
    ImVec4* colors = style.Colors;
    colors[ImGuiCol_Text]                   = RGB(0xD0D5E0);
    colors[ImGuiCol_TextDisabled]           = ImVec4(0.50f, 0.50f, 0.50f, 1.00f);
    colors[ImGuiCol_WindowBg]               = ImVec4(0, 0, 0, 0.8f);
    colors[ImGuiCol_ChildBg]                = ImVec4(1.00f, 1.00f, 1.00f, 0.00f);
    colors[ImGuiCol_PopupBg]                = RGB(0x0C1019);
    colors[ImGuiCol_Border]                 = RGB(0x252A35);
    colors[ImGuiCol_BorderShadow]           = RGB(0x0B0E15);
    colors[ImGuiCol_FrameBg]                = RGB(0x252A35);
    colors[ImGuiCol_FrameBgHovered]         = RGB(0x51545D);
    colors[ImGuiCol_FrameBgActive]          = RGB(0x3B3F49);
    colors[ImGuiCol_TitleBg]                = RGB(0x101521);
    colors[ImGuiCol_TitleBgActive]          = RGB(0x252A35);
    colors[ImGuiCol_TitleBgCollapsed]       = RGB(0x101521);
    colors[ImGuiCol_MenuBarBg]              = RGB(0x101521);
    colors[ImGuiCol_ScrollbarBg]            = RGB(0x252A35);
    colors[ImGuiCol_ScrollbarGrab]          = RGB(0x7C7F85);
    colors[ImGuiCol_ScrollbarGrabHovered]   = RGB(0xBDBFC2);
    colors[ImGuiCol_ScrollbarGrabActive]    = RGB(0xA8A9AE);
    colors[ImGuiCol_CheckMark]              = RGB(0xD0D5E0);
    colors[ImGuiCol_SliderGrab]             = RGB(0x72757B);
    colors[ImGuiCol_SliderGrabActive]       = RGB(0x72757B);
    colors[ImGuiCol_Button]                 = RGB(0x252A35);
    colors[ImGuiCol_ButtonHovered]          = RGB(0x666A71);
    colors[ImGuiCol_ButtonActive]           = RGB(0x92949A);
    colors[ImGuiCol_Header]                 = RGB(0x252A35);
    colors[ImGuiCol_HeaderHovered]          = RGB(0x666A71);
    colors[ImGuiCol_HeaderActive]           = RGB(0x7C7F85);
    colors[ImGuiCol_Separator]              = RGB(0x252A35);
    colors[ImGuiCol_SeparatorHovered]       = RGB(0x51545D);
    colors[ImGuiCol_SeparatorActive]        = RGB(0x8BD6EE);
    colors[ImGuiCol_ResizeGrip]             = RGB(0x252A35);
    colors[ImGuiCol_ResizeGripHovered]      = RGB(0x51545D);
    colors[ImGuiCol_ResizeGripActive]       = RGB(0x88D6EE);
    colors[ImGuiCol_PlotLines]              = RGB(0x00A5DB);
    colors[ImGuiCol_PlotLinesHovered]       = RGB(0x45BDE4);
    colors[ImGuiCol_PlotHistogram]          = RGB(0x00A5DB);
    colors[ImGuiCol_PlotHistogramHovered]   = RGB(0x45BDE4);
    colors[ImGuiCol_TextSelectedBg]         = ImVec4(0.87f, 0.87f, 0.87f, 0.35f);
    colors[ImGuiCol_ModalWindowDarkening]   = RGB(0xA19DA3);
    colors[ImGuiCol_DragDropTarget]         = ImVec4(1.00f, 1.00f, 0.00f, 0.90f);
    colors[ImGuiCol_NavHighlight]           = ImVec4(0.60f, 0.60f, 0.60f, 1.00f);
    colors[ImGuiCol_NavWindowingHighlight]  = ImVec4(1.00f, 1.00f, 1.00f, 0.70f);
    colors[ImGuiCol_Tab]                    = RGB(0x252A35);
    colors[ImGuiCol_TabHovered]             = RGB(0x3B3F49);
    colors[ImGuiCol_TabActive]              = RGB(0x51545D);
    colors[ImGuiCol_TabUnfocused]           = RGB(0x252A35);
    colors[ImGuiCol_TabUnfocusedActive]     = RGB(0x3B3F49);
    colors[ImGuiCol_DockingPreview]         = RGB(0x00A5DB);

    style.WindowRounding = 2.f;
    style.WindowBorderSize = 0.f;
    style.FrameRounding = 2.f;
    style.GrabRounding = 2.f;

    if (density != 0) {
        style.ScaleAllSizes(density);
        ImGui::GetIO().FontGlobalScale = density;
    }
    LoadedDensity = density;

    ImGui::GetStyle() = style;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_init(JNIEnv *env, jclass cls, jlong windowHandle, jboolean useMacBehaviors) {
    jfieldID fid;

    fid = env->GetStaticFieldID(cls, "TREE_FLAG_LEAF", "I");
    env->SetStaticIntField(cls, fid, ImGuiTreeNodeFlags_Leaf);
    fid = env->GetStaticFieldID(cls, "TREE_FLAG_DEFAULT_OPEN", "I");
    env->SetStaticIntField(cls, fid, ImGuiTreeNodeFlags_DefaultOpen);
    fid = env->GetStaticFieldID(cls, "TREE_FLAG_SELECTED", "I");
    env->SetStaticIntField(cls, fid, ImGuiTreeNodeFlags_Selected);
    fid = env->GetStaticFieldID(cls, "WINDOW_HORIZ_SCROLL", "I");
    env->SetStaticIntField(cls, fid, ImGuiWindowFlags_HorizontalScrollbar);
    fid = env->GetStaticFieldID(cls, "WINDOW_NO_RESIZE", "I");
    env->SetStaticIntField(cls, fid, ImGuiWindowFlags_NoResize);
    fid = env->GetStaticFieldID(cls, "WINDOW_NO_MOVE", "I");
    env->SetStaticIntField(cls, fid, ImGuiWindowFlags_NoMove);
    fid = env->GetStaticFieldID(cls, "WINDOW_NO_TITLE_BAR", "I");
    env->SetStaticIntField(cls, fid, ImGuiWindowFlags_NoTitleBar);
    fid = env->GetStaticFieldID(cls, "WINDOW_NO_DOCKING", "I");
    env->SetStaticIntField(cls, fid, ImGuiWindowFlags_NoDocking);
    fid = env->GetStaticFieldID(cls, "WINDOW_NO_BACKGROUND", "I");
    env->SetStaticIntField(cls, fid, ImGuiWindowFlags_NoBackground);
    fid = env->GetStaticFieldID(cls, "WINDOW_ALWAYS_AUTO_RESIZE", "I");
    env->SetStaticIntField(cls, fid, ImGuiWindowFlags_AlwaysAutoResize);
    fid = env->GetStaticFieldID(cls, "WINDOW_NO_DECORATION", "I");
    env->SetStaticIntField(cls, fid, ImGuiWindowFlags_NoDecoration);
    fid = env->GetStaticFieldID(cls, "WINDOW_NO_SCROLL_WITH_MOUSE", "I");
    env->SetStaticIntField(cls, fid, ImGuiWindowFlags_NoScrollWithMouse);
    fid = env->GetStaticFieldID(cls, "WINDOW_FORCE_HORIZ_SCROLL", "I");
    env->SetStaticIntField(cls, fid, ImGuiWindowFlags_AlwaysHorizontalScrollbar);
    fid = env->GetStaticFieldID(cls, "WINDOW_NO_SCROLLBAR", "I");
    env->SetStaticIntField(cls, fid, ImGuiWindowFlags_NoScrollbar);
    fid = env->GetStaticFieldID(cls, "COLOR_WIDGET", "I");
    env->SetStaticIntField(cls, fid, ImGuiCol_FrameBg);
    fid = env->GetStaticFieldID(cls, "COLOR_WIDGET_HOVERED", "I");
    env->SetStaticIntField(cls, fid, ImGuiCol_FrameBgHovered);
    fid = env->GetStaticFieldID(cls, "COLOR_HEADER", "I");
    env->SetStaticIntField(cls, fid, ImGuiCol_Header);
    fid = env->GetStaticFieldID(cls, "COLOR_HEADER_ACTIVE", "I");
    env->SetStaticIntField(cls, fid, ImGuiCol_HeaderActive);
    fid = env->GetStaticFieldID(cls, "COLOR_HEADER_HOVERED", "I");
    env->SetStaticIntField(cls, fid, ImGuiCol_HeaderHovered);
    fid = env->GetStaticFieldID(cls, "COLOR_BUTTON", "I");
    env->SetStaticIntField(cls, fid, ImGuiCol_Button);
    fid = env->GetStaticFieldID(cls, "COLOR_BUTTON_ACTIVE", "I");
    env->SetStaticIntField(cls, fid, ImGuiCol_ButtonActive);
    fid = env->GetStaticFieldID(cls, "COLOR_BUTTON_HOVERED", "I");
    env->SetStaticIntField(cls, fid, ImGuiCol_ButtonHovered);
    fid = env->GetStaticFieldID(cls, "COLOR_WINDOW_BORDER", "I");
    env->SetStaticIntField(cls, fid, ImGuiCol_Border);
    fid = env->GetStaticFieldID(cls, "COND_ALWAYS", "I");
    env->SetStaticIntField(cls, fid, ImGuiCond_Always);
    fid = env->GetStaticFieldID(cls, "COND_ONCE", "I");
    env->SetStaticIntField(cls, fid, ImGuiCond_Once);
    fid = env->GetStaticFieldID(cls, "COND_APPEARING", "I");
    env->SetStaticIntField(cls, fid, ImGuiCond_Appearing);

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

    ImGuiIO &io = ImGui::GetIO();
    io.ConfigFlags |= ImGuiConfigFlags_DockingEnable;
    io.ConfigWindowsResizeFromEdges = true;
    /* Keep this off for now, it causes a window sizing bug (#2386)
    io.ConfigDockingTabBarOnSingleWindows = true;
    */
    io.ConfigMacOSXBehaviors = useMacBehaviors != 0;

    /* Disable auto load/save of window positions for now */
    io.IniFilename = nullptr;

    return 1;
}

JNIEXPORT jlong JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_addFont(JNIEnv *env, jclass, jstring jname, jobject jbuf, jfloat fsize) {
    JniString name(env, jname);
    ImFontConfig fc;
    strncpy(fc.Name, name, sizeof(fc.Name) - 1);
    /* Need to make a copy, because ImGui wants to own the data (it frees it at the end of the run) */
    void *jdata = env->GetDirectBufferAddress(jbuf);
    jlong size = env->GetDirectBufferCapacity(jbuf);
    void *data = malloc(size);
    memcpy(data, jdata, size);
    return static_cast<jlong>(
        reinterpret_cast<intptr_t>(
            ImGui::GetIO().Fonts->AddFontFromMemoryTTF(data, size, fsize, &fc)));
}

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_pushFont(JNIEnv *, jclass, jlong fontHandle) {
    ImGui::PushFont(reinterpret_cast<ImFont*>(static_cast<intptr_t>(fontHandle)));
}

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_popFont(JNIEnv *, jclass) {
    ImGui::PopFont();
}

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_newFrame(JNIEnv *env, jclass cls) {
    jfloat dens = env->GetStaticFloatField(cls, env->GetStaticFieldID(cls, "density", "F"));
    if (dens != LoadedDensity) {
        LoadSlimguiStyle(dens);
    }

    ImGuiContext &g = *ImGui::GetCurrentContext();
    if (DragObjectReference != nullptr) {
        if (!g.DragDropActive || *((jobject*) g.DragDropPayload.Data) != DragObjectReference) {
            env->DeleteGlobalRef(DragObjectReference);
            DragObjectReference = nullptr;
        }
    }

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

    InitChannelWindowDockSpace();

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

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_pushColor(
    JNIEnv *, jclass, jint key, jint jcolor) {
    ImGui::PushStyleColor(key, RGB(jcolor));
}

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_popColor(JNIEnv *, jclass, jint count) {
    ImGui::PopStyleColor(count);
}

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_pushWidth(JNIEnv *, jclass, jfloat width) {
    ImGui::PushItemWidth(width);
}

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_popWidth(JNIEnv *, jclass) {
    ImGui::PopItemWidth();
}

JNIEXPORT jfloat JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_calcWidth(JNIEnv *, jclass) {
    return ImGui::CalcItemWidth();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_setNextWindowPosition(JNIEnv *env, jclass, jfloat x, jfloat y, jfloat pivotX, jfloat pivotY) {
    ImGui::SetNextWindowPos(ImVec2(x, y), ImGuiCond_Always, ImVec2(pivotX, pivotY));
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_setNextWindowDefaults(
    JNIEnv *env, jclass, jfloat x, jfloat y, jfloat w, jfloat h) {
    ImGui::SetNextWindowSize(ImVec2(LoadedDensity * w, LoadedDensity * h), ImGuiCond_FirstUseEver);
    ImGui::SetNextWindowPos(ImVec2(x, y), ImGuiCond_FirstUseEver);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_setNextWindowDefaultToCursor(
    JNIEnv *env, jclass, jfloat w, jfloat h) {
    ImGui::SetNextWindowSize(ImVec2(LoadedDensity * w, LoadedDensity * h), ImGuiCond_FirstUseEver);
    ImGui::SetNextWindowPos(ImGui::GetCursorScreenPos(), ImGuiCond_FirstUseEver);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_setNextWindowSize(JNIEnv *, jclass, jfloat w, jfloat h) {
    ImGui::SetNextWindowSize(ImVec2(LoadedDensity * w, LoadedDensity * h));
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_setNextWindowContentSize(JNIEnv *, jclass, jfloat w, jfloat h) {
    ImGui::SetNextWindowContentSize(ImVec2(LoadedDensity * w, LoadedDensity * h));
}

JNIEXPORT jobject JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_getContentRegionSize(JNIEnv *env, jclass) {
    ImVec2 size = ImGui::GetContentRegionMax();
    jclass resCls = env->FindClass("com/symmetrylabs/slstudio/ui/v2/UI$Size");
    jobject res = env->NewObject(resCls, env->GetMethodID(resCls, "<init>", "()V"));
    env->SetFloatField(res, env->GetFieldID(resCls, "width", "F"), size.x);
    env->SetFloatField(res, env->GetFieldID(resCls, "height", "F"), size.y);
    return res;
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_begin(JNIEnv *env, jclass, jstring jstr, jint flags) {
    JniString str(env, jstr);
    ImGui::Begin(str, nullptr, flags);
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_beginClosable(JNIEnv *env, jclass, jstring jstr, jint flags) {
    JniString str(env, jstr);
    bool isOpen = true;
    ImGui::Begin(str, &isOpen, flags);
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
Java_com_symmetrylabs_slstudio_ui_v2_UI_beginTable(JNIEnv *env, jclass, jint num, jstring jlabel) {
    JniString label(env, jlabel);
    ImGui::Columns(num, label);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_nextCell(JNIEnv *, jclass) {
    ImGui::NextColumn();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_endTable(JNIEnv *, jclass) {
    ImGui::Columns(1);
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_separator(JNIEnv *, jclass) {
    ImGui::Separator();
}

JNIEXPORT void JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_spacing(JNIEnv *, jclass, jfloat w, jfloat h) {
    ImGui::Dummy(ImVec2(w, h));
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_beginChild(
    JNIEnv *env, jclass, jstring jid, jboolean border, jint flags, jint w, jint h) {
    JniString id(env, jid);
    bool res = ImGui::BeginChild(id, ImVec2(LoadedDensity * w, LoadedDensity * h), border == 1, flags);
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
Java_com_symmetrylabs_slstudio_ui_v2_UI_button(JNIEnv *env, jclass, jstring jstr, jfloat w, jfloat h) {
    JniString str(env, jstr);
    bool res = ImGui::Button(str, ImVec2{w, h});
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
Java_com_symmetrylabs_slstudio_ui_v2_UI_selectable(JNIEnv *env, jclass, jstring jlabel, jboolean v, jfloat height) {
    JniString label(env, jlabel);
    bool res = v == 1 ? true : false;
    ImGui::PushStyleVar(ImGuiStyleVar_SelectableTextAlign, ImVec2(0, 0.5f));
    ImGui::Selectable(label, &res, 0, ImVec2(0, height));
    ImGui::PopStyleVar();
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

JNIEXPORT jint JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_colorPicker(JNIEnv *env, jclass, jstring jlabel, jint rgb) {
    JniString label(env, jlabel);
    float rgbchans[] = {
        ((float) ((rgb >> 16) & 0xFF)) / 255.f,
        ((float) ((rgb >>  8) & 0xFF)) / 255.f,
        ((float) ((rgb >>  0) & 0xFF)) / 255.f,
    };
    ImGui::ColorEdit3(label, rgbchans, ImGuiColorEditFlags_NoOptions);
    return
        0xFF000000 |
        (((int) (rgbchans[0] * 255.f)) << 16) |
        (((int) (rgbchans[1] * 255.f)) <<  8) |
        (((int) (rgbchans[2] * 255.f)) <<  0);
}

JNIEXPORT jfloatArray JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_colorPickerHSV(JNIEnv *env, jclass, jstring jlabel, jfloat h, jfloat s, jfloat v) {
    JniString label(env, jlabel);
    float hsv[] = {h / 360.f, s / 100.f, v / 100.f};
    ImGui::ColorEdit3(label, hsv, ImGuiColorEditFlags_InputHSV | ImGuiColorEditFlags_ShowHSV | ImGuiColorEditFlags_Float | ImGuiColorEditFlags_NoOptions);
    hsv[0] *= 360.f;
    hsv[1] *= 100.f;
    hsv[2] *= 100.f;
    jfloatArray res = env->NewFloatArray(3);
    env->SetFloatArrayRegion(res, 0, 3, hsv);
    return res;
}

JNIEXPORT jfloat JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_sliderFloat(
    JNIEnv * env, jclass, jstring jlabel, jfloat v, jfloat v0, jfloat v1) {
    JniString label(env, jlabel);
    ImGui::SliderFloat(label, &v, v0, v1);
    return v;
}

JNIEXPORT jfloat JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_vertSliderFloat(
    JNIEnv * env, jclass, jstring jlabel, jfloat v, jfloat v0, jfloat v1, jstring jfmtstr, jfloat width, jfloat height) {
    JniString label(env, jlabel);
    JniString fmtstr(env, jfmtstr);
    ImGui::VSliderFloat(label, ImVec2(width, height), &v, v0, v1, fmtstr);
    return v;
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

JNIEXPORT jfloat JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_floatBox(JNIEnv *env, jclass, jstring jlabel, jfloat v, jfloat speed, jfloat min, jfloat max, jstring jfmtstr) {
    JniString label(env, jlabel);
    JniString fmtstr(env, jfmtstr);
    jfloat res = v;
    ImGui::DragFloat(label, &res, speed, min, max, fmtstr);
    return res;
}

JNIEXPORT jfloat JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_knobFloat(JNIEnv *env, jclass cls, jstring jlabel, jfloat vf, jfloat vn) {
    JniString label(env, jlabel);
    Knob(label, &vn, vf, vn, 0, nullptr, nullptr, nullptr);
    return vn;
}

JNIEXPORT jfloat JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_knobModulatedFloat(
    JNIEnv *env, jclass cls, jstring jlabel, jfloat vf, jfloat vn,
    jfloat modulated, jint modCount, jfloatArray jmins, jfloatArray jmaxs, jintArray jcolors) {
    JniString label(env, jlabel);
    JniFloatArray mins(env, jmins);
    JniFloatArray maxs(env, jmaxs);
    JniIntArray colors(env, jcolors);
    Knob(label, &vn, vf, modulated, modCount, mins, maxs, colors);
    return vn;
}

JNIEXPORT jobject JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_collapsibleSection(JNIEnv *env, jclass, jstring jlabel, jboolean allowClose, jint flags) {
    JniString label(env, jlabel);
    bool display = true;
    bool isOpen = false;
    if (allowClose) {
        isOpen = ImGui::CollapsingHeader(label, &display, flags);
    } else {
        isOpen = ImGui::CollapsingHeader(label, flags);
    }
    jclass resCls = env->FindClass("com/symmetrylabs/slstudio/ui/v2/UI$CollapseResult");
    jobject res = env->NewObject(resCls, env->GetMethodID(resCls, "<init>", "()V"));
    env->SetBooleanField(res, env->GetFieldID(resCls, "isOpen", "Z"), isOpen ? 1 : 0);
    env->SetBooleanField(res, env->GetFieldID(resCls, "shouldRemove", "Z"), display ? 0 : 1);
    return res;
}

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_histogram(
    JNIEnv *env, jclass, jstring jlabel, jfloatArray jvals, jfloat min, jfloat max, jint size) {
    JniFloatArray hist(env, jvals);
    JniString label(env, jlabel);
    ImGui::PlotHistogram(label, hist, hist.size(), 0, NULL, min, max, ImVec2(0, size));
}

JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_colorButton(JNIEnv *env, jclass, jstring jlabel, jfloat h, jfloat s, jfloat v) {
    JniString label(env, jlabel);
    ImVec4 rgb;
    ImGui::ColorConvertHSVtoRGB(h / 360, s / 100, v / 100, rgb.x, rgb.y, rgb.z);
    rgb.w = 1;
    return ImGui::ColorButton(label, rgb) ? 1 : 0;
}

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_plot(
    JNIEnv *env, jclass, jstring jlabel, jfloatArray jvals, jfloat min, jfloat max, jint size) {
    JniFloatArray hist(env, jvals);
    JniString label(env, jlabel);
    ImGui::PlotLines(label, hist, hist.size(), 0, NULL, min, max, ImVec2(0, size));
}

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_image(JNIEnv *, jclass, jint texId, jfloat w, jfloat h, jfloat u0, jfloat v0, jfloat u1, jfloat v1) {
    ImGui::Image((ImTextureID)(intptr_t)texId, ImVec2(w, h), ImVec2(u0, v0), ImVec2(u1, v1));
}

JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_imageButton(JNIEnv *, jclass, jint texId, jfloat w, jfloat h, jfloat u0, jfloat v0, jfloat u1, jfloat v1) {
    return ImGui::ImageButton((ImTextureID)(intptr_t)texId, ImVec2(w, h), ImVec2(u0, v0), ImVec2(u1, v1)) ? 1 : 0;
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
Java_com_symmetrylabs_slstudio_ui_v2_UI_menuItemToggle(
    JNIEnv *env, jclass, jstring jlabel, jstring jshortcut,
    jboolean selected, jboolean enabled) {
    const char *shortcut =
        jshortcut == NULL ? NULL : env->GetStringUTFChars(jshortcut, 0);
    JniString label(env, jlabel);

    bool sel = selected == 1;
    ImGui::MenuItem(label, shortcut, &sel, enabled == 1);

    if (shortcut != NULL) {
        env->ReleaseStringUTFChars(jshortcut, shortcut);
    }
    return sel ? 1 : 0;
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

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_setNextTreeNodeOpen(JNIEnv *, jclass, jboolean open, jint when) {
    ImGui::SetNextTreeNodeOpen(open != 0, when);
}

JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_beginDragDropSource(JNIEnv *, jclass, jint flags) {
    return ImGui::BeginDragDropSource(flags) ? 1 : 0;
}

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_endDragDropSource(JNIEnv *, jclass) {
    ImGui::EndDragDropSource();
}

JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_setDragDropPayload(JNIEnv *env, jclass, jstring jtype, jobject obj) {
    JniString type(env, jtype);
    jobject ref = env->NewGlobalRef(obj);
    /* this reference is deleted in newFrame when the drag event is over */
    DragObjectReference = ref;
    return ImGui::SetDragDropPayload(type, &ref, sizeof ref) ? 1 : 0;
}

JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_beginDragDropTarget(JNIEnv *, jclass) {
    return ImGui::BeginDragDropTarget() ? 1 : 0;
}

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_endDragDropTarget(JNIEnv *, jclass) {
    ImGui::EndDragDropTarget();
}

JNIEXPORT jobject JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_acceptDragDropPayload(JNIEnv *env, jclass, jstring jtype, jint flags) {
    JniString type(env, jtype);
    const ImGuiPayload *payload = ImGui::AcceptDragDropPayload(type, flags);
    if (payload == nullptr) {
        return nullptr;
    }
    jobject res = *((jobject*) payload->Data);
    return res;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_isItemClicked(JNIEnv *, jclass, jint button, jboolean allowMouseHold) {
    if (allowMouseHold) {
        return ImGui::IsItemHovered() && ImGui::IsMouseDown(button);
    }
    return ImGui::IsItemClicked(button) ? 1 : 0;
}

JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_isItemDoubleClicked(JNIEnv *, jclass, jint button) {
    return ImGui::IsItemHovered(ImGuiHoveredFlags_None) && ImGui::IsMouseDoubleClicked(button) ? 1 : 0;
}

JNIEXPORT jboolean JNICALL
Java_com_symmetrylabs_slstudio_ui_v2_UI_isItemActive(JNIEnv *, jclass) {
    return ImGui::IsItemActive() ? 1 : 0;
}

JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_isAltDown(JNIEnv *, jclass) {
    return ImGui::GetIO().KeyAlt ? 1 : 0;
}

JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_isCtrlDown(JNIEnv *, jclass) {
    return ImGui::GetIO().KeyCtrl ? 1 : 0;
}

JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_isShiftDown(JNIEnv *, jclass) {
    return ImGui::GetIO().KeyShift ? 1 : 0;
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

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_openPopup(JNIEnv *env, jclass, jstring jlabel) {
    JniString label(env, jlabel);
    ImGui::OpenPopup(label);
}

JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_beginPopup(JNIEnv *env, jclass, jstring jlabel, jboolean modal, jint flags) {
    JniString label(env, jlabel);
    bool res;
    if (modal) {
        res = ImGui::BeginPopupModal(label, nullptr, flags);
    } else {
        res = ImGui::BeginPopup(label, flags);
    }
    return res ? 1 : 0;
}

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_endPopup(JNIEnv *env, jclass) {
    ImGui::EndPopup();
}

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_closePopup(JNIEnv *env, jclass) {
    ImGui::CloseCurrentPopup();
}

JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_beginContextMenu(JNIEnv *env, jclass, jstring jlabel) {
    JniString label(env, jlabel);
    return ImGui::BeginPopupContextItem(label) ? 1 : 0;
}

JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_contextMenuItem(JNIEnv *env, jclass, jstring jlabel) {
    JniString label(env, jlabel);
    return ImGui::Selectable(label) ? 1 : 0;
}

JNIEXPORT void JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_endContextMenu(JNIEnv *, jclass) {
    ImGui::EndPopup();
}

JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_showMetricsWindow(JNIEnv *, jclass) {
    bool open = true;
    ImGui::ShowMetricsWindow(&open);
    return open ? 1 : 0;
}

JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_showStyleEditor(JNIEnv *, jclass) {
    bool open = true;
    ImGui::Begin("Style editor", &open);
    ImGui::ShowStyleEditor();
    if (ImGui::Button("Restore default slimgui style")) {
        LoadSlimguiStyle(LoadedDensity);
    }
    ImGui::End();
    return open ? 1 : 0;
}

JNIEXPORT jboolean JNICALL Java_com_symmetrylabs_slstudio_ui_v2_UI_showAboutWindow(JNIEnv *, jclass) {
    bool open = true;
    ImGui::ShowAboutWindow(&open);
    return open ? 1 : 0;
}
