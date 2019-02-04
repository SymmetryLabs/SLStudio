#pragma once
#include "imgui.h"

ImVec4 RGBA(double r, double g, double b, double a);
ImVec4 RGB(double r, double g, double b);
ImVec4 RGB(int rgb);
bool Knob(const char *label, float *v, float min, float max, bool isMod, float modulated);
