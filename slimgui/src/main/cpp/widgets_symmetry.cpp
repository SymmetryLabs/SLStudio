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

ImVec4 ARGB(int v) {
    int a = (v >> 24) & 0xFF;
    return RGBA((double) ((v >> 16) & 0xFF), (double) ((v >> 8) & 0xFF), (double) (v & 0xFF), (double) a);
}

ImVec4 RGB(double r, double g, double b) {
    return RGBA(r, g, b, 1.0);
}

ImVec4 RGBA(double r, double g, double b, double a) {
    return ImVec4((float) r / 255.f, (float) g / 255.f, (float) b / 255.f, (float) a / 255.f);
}

ImVec2 CursorPos() {
    return GetCurrentWindow()->DC.CursorPos;
}

static const ImVec4 KnobValueColor        = RGB(0x2EB5E1);
static const ImVec4 KnobValueHoveredColor = RGB(0x73CDEB);
static const ImVec4 KnobValueActiveColor  = RGB(0x00A5DB);
static const ImVec4 KnobModulatorColor    = RGB(0x930FA5);

bool Knob(const char *label, float *v, float vdisplay, float tmod, int mod_count,
          const float *mod_min, const float *mod_max, const jint *mod_colors, jint dot_color) {
    auto window = GetCurrentWindow();
    if (window->SkipItems)
        return false;

    ImGuiContext &g = *GImGui;
    const ImGuiStyle &style = g.Style;
    auto pos = window->DC.CursorPos;
    const ImGuiID id = window->GetID(label);

    ImVec2 label_size = CalcTextSize(label, NULL, true);
    ImVec2 size{
        (style.ItemInnerSpacing.x + style.KnobRadius) * 2,
        (style.ItemInnerSpacing.y + style.KnobRadius) * 2 + label_size.y + style.KnobThick};
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
            g.DragCurrentAccum += -g.IO.MouseDelta.y * (g.IO.KeyShift ? 0.01f : 1.f) * g.DragSpeedDefaultRatio;
            changed = true;
        }

        if (changed) {
            /* roundoff-aware accumulation; we'll still hold on to things that
               roundoff means we can't add to v. We'll get to it on the next frame,
               hopefully. */
            float new_v = *v + g.DragCurrentAccum;
            g.DragCurrentAccum -= new_v - *v;
            *v = new_v < 0 ? 0 : new_v > 1 ? 1 : new_v;
        }
    }

    float t = *v;

    /* we want min and max to be 45 from -Y, 0 angle in PathArcTo means +X and it goes
         clockwise, so we want 135 to 135 + 270. */
    float a_lo = 135.f / 180.f * M_PI;
    float a_hi = (135.f + 270.f) / 180.f * M_PI;
    float a = a_lo + t * (a_hi - a_lo);
    float a_mod = mod_count > 0 ? a_lo + tmod * (a_hi - a_lo) : a;

    ImVec2 center{pos.x + 0.5f * size.x + style.ItemInnerSpacing.x, pos.y + 0.45f * size.y};

    float thick = style.KnobThick;
    float rad = style.KnobRadius;

    for (int i = 0; i < mod_count; i++) {
        float tlo = mod_min[i];
        float thi = mod_max[i];

        tlo = tlo < 0.f ? 0.f : tlo > 1.f ? 1.f : tlo;
        thi = thi < 0.f ? 0.f : thi > 1.f ? 1.f : thi;

        float start = a_lo + tlo * (a_hi - a_lo);
        float end = a_lo + thi * (a_hi - a_lo);

        window->DrawList->PathClear();
        window->DrawList->PathArcTo(
            center, style.KnobRadius + style.KnobThick / 2 - style.KnobModThick * i,
            start, end, 40);
        window->DrawList->PathStroke(GetColorU32(RGB(mod_colors[i])), false, style.KnobModThick);
        if (i == 0) {
            thick -= style.KnobModThick;
        } else if (i == 1) {
            thick -= style.KnobModThick / 2;
            rad -= style.KnobModThick / 2;
        } else {
            rad -= style.KnobModThick;
        }
    }

    window->DrawList->PathClear();
    window->DrawList->PathArcTo(center, style.KnobRadius - style.KnobThick, 0, 2 * M_PI, 30);
    window->DrawList->PathFillConvex(GetColorU32(ARGB(dot_color)));

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
    window->DrawList->PathArcTo(center, style.KnobRadius, a - 0.15, a + 0.15, 10);
    window->DrawList->PathStroke(
        GetColorU32(g.ActiveId == id ? ImGuiCol_SliderGrabActive : ImGuiCol_SliderGrab), false, style.KnobHandleThick);

    ImVec2 text_pos {
        center.x - style.KnobRadius - style.KnobThick / 2,
        pos.y + 2 * style.KnobRadius + 1.5f * style.KnobThick + style.ItemInnerSpacing.y};
    ImVec2 text_clip {
        center.x + style.KnobRadius + style.KnobThick / 2,
        text_pos.y + label_size.y + style.ItemInnerSpacing.y};
    if (pressed) {
        char value_buf[64];
        int len = TinyNumberFormatString(value_buf, IM_ARRAYSIZE(value_buf), vdisplay);
        RenderTextClipped(text_pos, text_clip, value_buf, value_buf + len, NULL, ImVec2{0.5f, 0});
    } else {
        RenderTextClipped(text_pos, text_clip, label, NULL, NULL, ImVec2{0.5f, 0});
    }
    return changed;
}

bool KnobToggle(const char *label, bool *value, jint dot_color) {
    auto window = GetCurrentWindow();
    if (window->SkipItems)
        return false;

    ImGuiContext &g = *GImGui;
    const ImGuiStyle &style = g.Style;
    auto pos = window->DC.CursorPos;
    const ImGuiID id = window->GetID(label);

    ImVec2 label_size = CalcTextSize(label, NULL, true);
    ImVec2 size{
        (style.ItemInnerSpacing.x + style.KnobRadius) * 2,
        (style.ItemInnerSpacing.y + style.KnobRadius) * 2 + label_size.y + style.KnobThick};
    const ImRect bb{pos, pos + size};

    ItemSize(bb);
    if (!ItemAdd(bb, id))
        return false;

    bool hovered = false;
    bool held = false;
    bool pressed = ButtonBehavior(bb, id, &hovered, &held, 0);
    if (pressed) {
        *value = !*value;
    }

    ImVec2 center{pos.x + 0.5f * size.x + style.ItemInnerSpacing.x, pos.y + 0.45f * size.y};

    int color;
    if (*value) {
        color = GetColorU32(hovered ? KnobValueHoveredColor : KnobValueColor);
    } else {
        color = GetColorU32(hovered ? ImGuiCol_FrameBgHovered : ImGuiCol_FrameBg);
    }
    window->DrawList->PathClear();
    window->DrawList->PathArcTo(center, style.KnobRadius, 0, 2 * M_PI, 30);
    window->DrawList->PathFillConvex(color);

    window->DrawList->PathClear();
    window->DrawList->PathArcTo(center, style.KnobRadius - style.KnobThick, 0, 2 * M_PI, 30);
    window->DrawList->PathFillConvex(GetColorU32(ARGB(dot_color)));

    ImVec2 text_pos {
        center.x - style.KnobRadius - style.KnobThick / 2,
        pos.y + 2 * style.KnobRadius + 1.5f * style.KnobThick + style.ItemInnerSpacing.y};
    ImVec2 text_clip {
        center.x + style.KnobRadius + style.KnobThick / 2,
        text_pos.y + label_size.y + style.ItemInnerSpacing.y};
    RenderTextClipped(text_pos, text_clip, label, NULL, NULL, ImVec2{0.5f, 0});
    return pressed;
}

bool KnobButton(const char *label, bool displayAsPressed, jint dot_color) {
    auto window = GetCurrentWindow();
    if (window->SkipItems)
        return false;

    ImGuiContext &g = *GImGui;
    const ImGuiStyle &style = g.Style;
    auto pos = window->DC.CursorPos;
    const ImGuiID id = window->GetID(label);

    ImVec2 label_size = CalcTextSize(label, NULL, true);
    ImVec2 size{
        (style.ItemInnerSpacing.x + style.KnobRadius) * 2,
        (style.ItemInnerSpacing.y + style.KnobRadius) * 2 + label_size.y + style.KnobThick};
    const ImRect bb{pos, pos + size};

    ItemSize(bb);
    if (!ItemAdd(bb, id))
        return false;

    bool hovered = false;
    bool held = false;
    bool pressed = ButtonBehavior(bb, id, &hovered, &held, ImGuiButtonFlags_PressedOnClick);

    ImVec2 center{pos.x + 0.5f * size.x + style.ItemInnerSpacing.x, pos.y + 0.45f * size.y};

    int color =
        (held || displayAsPressed) ? GetColorU32(KnobValueColor)
        : hovered ? GetColorU32(ImGuiCol_FrameBgHovered)
        : GetColorU32(ImGuiCol_FrameBg);
    window->DrawList->PathClear();
    window->DrawList->AddRectFilled(
        ImVec2{center.x - style.KnobRadius, center.y - style.KnobRadius},
        ImVec2{center.x + style.KnobRadius, center.y + style.KnobRadius},
        color,
        /* rounding, rounding flags: */ 0, 0);

    window->DrawList->PathClear();
    window->DrawList->PathArcTo(center, style.KnobRadius - style.KnobThick, 0, 2 * M_PI, 30);
    window->DrawList->PathFillConvex(GetColorU32(ARGB(dot_color)));

    ImVec2 text_pos {
        center.x - style.KnobRadius - style.KnobThick / 2,
        pos.y + 2 * style.KnobRadius + 1.5f * style.KnobThick + style.ItemInnerSpacing.y};
    ImVec2 text_clip {
        center.x + style.KnobRadius + style.KnobThick / 2,
        text_pos.y + label_size.y + style.ItemInnerSpacing.y};
    RenderTextClipped(text_pos, text_clip, label, NULL, NULL, ImVec2{0.5f, 0});
    return pressed;
}

ImGuiID dockspace_id;
static ImGuiID dockspace_channels;
static bool dockspaces_initialized = false;

void InitChannelWindowDockSpace() {
    if (dockspaces_initialized) {
        return;
    }
    dockspaces_initialized = true;
    dockspace_id = ImGui::GetID("MainDockSpace");
}
