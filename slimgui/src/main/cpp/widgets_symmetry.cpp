#define _USE_MATH_DEFINES // necessary to get M_PI on Windows
#include <math.h>

#include "imgui.h"

#define IMGUI_DEFINE_MATH_OPERATORS
#include "imgui_internal.h"

#include "widgets_symmetry.h"

#include <iostream>
#include <string>

using namespace ImGui;

int TinyNumberFormatString(char *buf, int max_len, float v) {
	if (v < 1e4) {
		std::string fmt = v < 100 ? "%.2f" : "%.1f";
		return ImFormatString(buf, max_len, fmt.c_str(), v);
	} else if (v < 1e6) {
		v = v / 1000;
		return ImFormatString(buf, max_len, "%.1fK", v);
	}
	v = v / 1000000;
	return ImFormatString(buf, max_len, "%.1fM", v);
}

ImVec4 RGB(int v) {
    int a = (v >> 24) & 0xFF;
    /* if no alpha was specified, they probably meant full-opaque. */
    if (a == 0) {
        a = 255;
    }
    return RGBA((double) ((v >> 16) & 0xFF), (double) ((v >> 8) & 0xFF), (double) (v & 0xFF), (double) a);
}

ImVec4 RGB(double r, double g, double b) {
    return RGBA(r, g, b, 1.0);
}

ImVec4 RGBA(double r, double g, double b, double a) {
    return ImVec4((float) r / 255.f, (float) g / 255.f, (float) b / 255.f, (float) a / 255.f);
}

static const ImVec4 KnobValueColor        = RGB(0x2EB5E1);
static const ImVec4 KnobValueHoveredColor = RGB(0x73CDEB);
static const ImVec4 KnobValueActiveColor  = RGB(0x00A5DB);
static const ImVec4 KnobModulatorColor    = RGB(0x930FA5);
static const float KnobRadius = 18;
static const float KnobThick = 11;
static const float KnobPadH = 5;
static const float KnobPadV = 0;
static const float KnobModThick = 3;

static inline float prop(float v, float min, float max) {
    return (v - min) / (max - min);
}

bool Knob(const char *label, float *v, float min, float max,
          int mod_count, float modulated,
          const float *mod_min, const float *mod_max, const jint *mod_colors) {
	auto window = GetCurrentWindow();
	if (window->SkipItems)
		return false;

	ImGuiContext &g = *GImGui;
	const ImGuiStyle &style = g.Style;
	auto pos = window->DC.CursorPos;
	const ImGuiID id = window->GetID(label);

	ImVec2 label_size = CalcTextSize(label, NULL, true);
	ImVec2 size{
		(KnobPadH + KnobRadius) * 2,
		(KnobPadV + KnobRadius) * 2 + label_size.y + KnobThick + style.ItemInnerSpacing.y};
	const ImRect bb{pos, pos + size};

	ItemSize(bb);
	if (!ItemAdd(bb, id))
		return false;

	FocusableItemRegister(window, id);
	const bool hovered = ItemHoverable(bb, id);

	bool changed = false;
	bool pressed = false;

	if (hovered && g.IO.MouseClicked[0]) {
		SetActiveID(id, window);
		SetFocusID(id, window);
		FocusWindow(window);
		g.DragCurrentAccum = 0.f;
		pressed = true;
	} else if (g.ActiveId == id && !g.IO.MouseDown[0]) {
		ClearActiveID();
	} else if (g.ActiveId == id) {
		pressed = true;

		if (g.ActiveIdSource == ImGuiInputSource_Mouse && IsMousePosValid()) {
			g.DragCurrentAccum += -g.IO.MouseDelta.y * (g.IO.KeyShift ? 0.01f : 1.f) * (max - min) * g.DragSpeedDefaultRatio;
			changed = true;
		}

		if (changed) {
			/* roundoff-aware accumulation; we'll still hold on to things that
			   roundoff means we can't add to v. We'll get to it on the next frame,
			   hopefully. */
			float new_v = *v + g.DragCurrentAccum;
			g.DragCurrentAccum -= new_v - *v;
			*v = new_v < min ? min : new_v > max ? max : new_v;
		}
	}

	float t = prop(*v, min, max);
	float tmod = prop(modulated, min, max);

	/* we want min and max to be 45 from -Y, 0 angle in PathArcTo means +X and it goes
		 clockwise, so we want 135 to 135 + 270. */
	float a_lo = 135.f / 180.f * M_PI;
	float a_hi = (135.f + 270.f) / 180.f * M_PI;
	float a = a_lo + t * (a_hi - a_lo);
    float a_mod = mod_count > 0 ? a_lo + tmod * (a_hi - a_lo) : a;

	ImVec2 center{pos.x + 0.5f * size.x + KnobPadH, pos.y + 0.5f * size.y + KnobPadV};

    float thick = KnobThick;
    float rad = KnobRadius;

    for (int i = 0; i < mod_count; i++) {
        float lo = mod_min[i];
        float hi = mod_max[i];

        float tlo = prop(lo, min, max);
        tlo = tlo < 0.f ? 0.f : tlo > 1.f ? 1.f : tlo;

        float thi = prop(hi, min, max);
        thi = thi < 0.f ? 0.f : thi > 1.f ? 1.f : thi;

        float start = a_lo + tlo * (a_hi - a_lo);
        float end = a_lo + thi * (a_hi - a_lo);

        window->DrawList->PathClear();
        window->DrawList->PathArcTo(center, KnobRadius + KnobThick / 2 - KnobModThick * i, start, end, 40);
        window->DrawList->PathStroke(GetColorU32(RGB(mod_colors[i])), false, KnobModThick);
        if (i == 0) {
            thick -= KnobModThick;
        } else if (i == 1) {
            thick -= KnobModThick / 2;
            rad -= KnobModThick / 2;
        } else {
            rad -= KnobModThick;
        }
    }

	window->DrawList->PathClear();
	window->DrawList->PathArcTo(center, rad, a_lo, a_mod, 40);
	window->DrawList->PathStroke(
		GetColorU32(g.ActiveId == id ? KnobValueActiveColor : g.HoveredId == id ? KnobValueHoveredColor : KnobValueColor), false, thick);

	window->DrawList->PathClear();
	window->DrawList->PathArcTo(center, rad, a_mod, a_hi, 40);
	window->DrawList->PathStroke(
		GetColorU32(g.ActiveId == id ? ImGuiCol_FrameBgActive : g.HoveredId == id ? ImGuiCol_FrameBgHovered : ImGuiCol_FrameBg),
		false, thick);

	window->DrawList->PathClear();
	window->DrawList->PathArcTo(center, KnobRadius, a - 0.15, a + 0.15, 10);
	window->DrawList->PathStroke(
		GetColorU32(g.ActiveId == id ? ImGuiCol_SliderGrabActive : ImGuiCol_SliderGrab), false, 13);

	ImVec2 text_pos {
		center.x - KnobRadius - KnobThick / 2,
		pos.y + 2 * KnobRadius + KnobThick + KnobPadV + style.ItemInnerSpacing.y};
	ImVec2 text_clip {
		center.x + KnobRadius + KnobThick / 2,
		text_pos.y + label_size.y + style.ItemInnerSpacing.y};
	if (pressed) {
		char value_buf[64];
		int len = TinyNumberFormatString(value_buf, IM_ARRAYSIZE(value_buf), *v);
		RenderTextClipped(text_pos, text_clip, value_buf, value_buf + len, NULL, ImVec2{0.5f, 0});
	} else {
		RenderTextClipped(text_pos, text_clip, label, NULL, NULL, ImVec2{0.5f, 0});
	}
	return changed;
}
