#include "imgui.h"

#define IMGUI_DEFINE_MATH_OPERATORS
#include "imgui_internal.h"

#include <string>

static const float knob_radius = 18;
static const float knob_thick = 10;
static const float knob_pad_h = 5;
static const float knob_pad_v = 0;

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

bool Knob(const char *label, float *v, float min, float max) {
	auto window = GetCurrentWindow();
	if (window->SkipItems)
		return false;

	ImGuiContext &g = *GImGui;
	const ImGuiStyle &style = g.Style;
	auto pos = window->DC.CursorPos;
	const ImGuiID id = window->GetID(label);

	ImVec2 label_size = CalcTextSize(label, NULL, true);
	ImVec2 size{
		(knob_pad_h + knob_radius) * 2,
		(knob_pad_v + knob_radius) * 2 + label_size.y + knob_thick + style.ItemInnerSpacing.y};
	const ImRect bb{pos, pos + size};

	ItemSize(bb);
	if (!ItemAdd(bb, id))
		return false;

	FocusableItemRegister(window, id);
	const bool hovered = ItemHoverable(bb, id);

	float t = (*v - min) / (max - min);
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

	/* we want min and max to be 45 from -Y, 0 angle in PathArcTo means +X and it goes
		 clockwise, so we want 135 to 135 + 270. */
	float a_lo = 135.f / 180.f * M_PI;
	float a_hi = (135.f + 270.f) / 180.f * M_PI;
	float a = a_lo + t * (a_hi - a_lo);

	ImVec2 center{pos.x + 0.5f * size.x + knob_pad_h, pos.y + 0.5f * size.y + knob_pad_v};

	window->DrawList->PathClear();
	window->DrawList->PathArcTo(center, knob_radius, a_lo, a_hi, 40);
	window->DrawList->PathStroke(
		GetColorU32(g.ActiveId == id ? ImGuiCol_FrameBgActive : g.HoveredId == id ? ImGuiCol_FrameBgHovered : ImGuiCol_FrameBg),
		false, knob_thick);

	window->DrawList->PathClear();
	window->DrawList->PathArcTo(center, knob_radius, a - 0.15, a + 0.15, 10);
	window->DrawList->PathStroke(
		GetColorU32(g.ActiveId == id ? ImGuiCol_SliderGrabActive : ImGuiCol_SliderGrab), false, 13);

	ImVec2 text_pos {
		center.x - knob_radius - knob_thick / 2,
		pos.y + 2 * knob_radius + knob_thick + knob_pad_v + style.ItemInnerSpacing.y};
	ImVec2 text_clip {
		center.x + knob_radius + knob_thick / 2,
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
