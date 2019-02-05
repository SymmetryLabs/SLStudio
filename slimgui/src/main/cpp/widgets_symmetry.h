#pragma once
#include <jni.h>
#include "imgui.h"

ImVec4 RGBA(double r, double g, double b, double a);
ImVec4 RGB(double r, double g, double b);
ImVec4 RGB(int rgb);

bool Knob(const char *label, float *v, float min, float max,
          int mod_count, float modulated_value,
          const float *mod_min, const float *mod_max, const jint *mod_colors);
