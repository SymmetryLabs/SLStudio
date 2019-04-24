#pragma once
#include <jni.h>
#include "imgui.h"

ImVec4 RGBA(double r, double g, double b, double a);
ImVec4 RGB(double r, double g, double b);
ImVec4 RGB(int rgb);

ImVec2 CursorPos();

bool Knob(const char *label, float *vn, float vdisplay, float modulated_value, int mod_count,
          const float *mod_min, const float *mod_max, const jint *mod_colors);

extern ImGuiID dockspace_id;

void InitChannelWindowDockSpace();
