package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXCompoundModulation;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.CompoundParameter;

public class ParameterUI {
    public enum WidgetType {
        KNOB,
        SLIDER,
    };

    public static ParameterUI getDefault(LX lx) {
        return new ParameterUI(
            lx,
            WidgetType.SLIDER,
            true);
    }

    private final LX lx;
    WidgetType defaultBoundedWidget;
    protected boolean showLabel;

    protected ParameterUI(LX lx, WidgetType defaultBoundedWidget, boolean showLabel) {
        this.lx = lx;
        this.defaultBoundedWidget = defaultBoundedWidget;
        this.showLabel = showLabel;
    }

    public ParameterUI setDefaultBoundedWidget(WidgetType t) {
        defaultBoundedWidget = t;
        return this;
    }

    public ParameterUI showLabel(boolean showLabel) {
        this.showLabel = showLabel;
        return this;
    }

    public String getID(LXParameter p) {
        LXComponent parent = p.getComponent();
        if (parent == null) {
            return (showLabel ? "" : "##") + p.getLabel();
        }
        return String.format(
            showLabel ? "%s##%d/%s" : "##%s/%d/%s",
            p.getLabel(), parent.getId(), p.getLabel());
    }

    public void draw(BoundedParameter p) {
        draw(p, defaultBoundedWidget);
    }

    public void draw(BoundedParameter p, WidgetType wt) {
        if (wt == WidgetType.SLIDER) {
            float start = p.getValuef();
            final float res = UI.sliderFloat(getID(p), start, (float) p.range.v0, (float) p.range.v1);
            if (start != res) {
                lx.engine.addTask(() -> p.setValue(res));
            }
            return;
        }
        final float start = p.getNormalizedf();
        final float res =
            p instanceof CompoundParameter
            ? compoundKnob((CompoundParameter) p)
            : UI.knobFloat(getID(p), p.getValuef(), start, 0);

        if (UI.beginDragDropSource()) {
            UI.setDragDropPayload("SL.BoundedParameter", p);
            UI.endDragDropSource();
        }
        if (UI.beginDragDropTarget()) {
            BoundedParameter dragged = UI.acceptDragDropPayload("SL.BoundedParameter", BoundedParameter.class);
            if (dragged != null) {
                // TODO: use this to create links/modulations
                System.out.println(String.format("drag %s onto %s", dragged.getLabel(), p.getLabel()));
            }
            UI.endDragDropTarget();
        }

        if (start != res) {
            final float fres = res;
            lx.engine.addTask(() -> p.setNormalized(fres));
        }
    }

    private float compoundKnob(CompoundParameter cp) {
        int N = cp.modulations.size();
        float[] mins = new float[N];
        float[] maxs = new float[N];
        int[] colors = new int[N];
        for (int i = 0; i < N; i++) {
            LXCompoundModulation modulation = cp.modulations.get(i);

            float modStart, modEnd;
            switch (modulation.getPolarity()) {
            case BIPOLAR:
                modStart = -modulation.range.getValuef();
                modEnd = modulation.range.getValuef();
                break;
            default:
            case UNIPOLAR:
                modStart = 0;
                modEnd = modStart + modulation.range.getValuef();
                break;
            }
            mins[i] = Float.min(modStart, modEnd);
            maxs[i] = Float.max(modStart, modEnd);
            colors[i] = modulation.color.getColor();
        }
        return UI.knobModulatedFloat(
            getID(cp), (float) cp.getBaseValue(), (float) cp.getBaseNormalized(), cp.getNormalizedf(), N, mins, maxs, colors, 0);
    }

    public void draw(DiscreteParameter p) {
        String[] options = p.getOptions();
        if (options == null) {
            int start = p.getValuei();
            final int res = UI.sliderInt(
                getID(p), start, p.getMinValue(), p.getMaxValue() - 1);
            if (start != res) {
                lx.engine.addTask(() -> p.setValue(res));
            }
        } else {
            int start = p.getValuei();
            int res = UI.combo(getID(p), start, options);
            if (start != res) {
                lx.engine.addTask(() -> p.setValue(res));
            }
        }
    }

    public boolean toggle(String label, boolean active, boolean important, float w) {
        if (active) {
            UI.pushColor(UI.COLOR_BUTTON, important ? UIConstants.RED : UIConstants.BLUE);
            UI.pushColor(UI.COLOR_BUTTON_HOVERED, important ? UIConstants.RED_HOVER : UIConstants.BLUE_HOVER);
        }
        boolean flip = UI.button(label, w, 0);
        if (active) {
            UI.popColor(2);
        }
        return flip ? !active : active;
    }

    public boolean toggle(BooleanParameter p, boolean important, float w) {
        final boolean start = p.getValueb();
        final boolean res = toggle(getID(p), start, important, w);
        if (res != start) {
            lx.engine.addTask(() -> p.setValue(res));
        }
        return res;
    }

    public boolean toggle(String label, BooleanParameter p, boolean important, float w) {
        final boolean start = p.getValueb();
        final boolean res = toggle(label, start, important, w);
        if (res != start) {
            lx.engine.addTask(() -> p.setValue(res));
        }
        return res;
    }

    public void draw(BooleanParameter p) {
        draw(p, false);
    }

    public void draw(BooleanParameter p, boolean important) {
        final boolean start = p.getValueb();
        boolean res;
        if (p.getMode() == BooleanParameter.Mode.TOGGLE) {
            if (important && start) {
                UI.pushColor(UI.COLOR_WIDGET, UIConstants.RED);
                UI.pushColor(UI.COLOR_WIDGET_HOVERED, UIConstants.RED_HOVER);
            }
            res = UI.checkbox(getID(p), start);
            if (important && start) {
                UI.popColor(2);
            }
        } else {
            UI.button(getID(p));
            res = UI.isItemActive();
        }
        if (res != start) {
            lx.engine.addTask(() -> p.setValue(res));
        }
    }

    public void draw(ColorParameter p) {
        int start = p.getColor();
        float h = p.hue.getValuef();
        float s = p.saturation.getValuef();
        float b = p.brightness.getValuef();
        float[] res = UI.colorPickerHSV(getID(p), h, s, b);
        if (h != res[0] || s != res[1] || b != res[2]) {
            lx.engine.addTask(() -> {
                    p.hue.setValue(res[0]);
                    p.saturation.setValue(res[1]);
                    p.brightness.setValue(res[2]);
                });
        }
    }

    public void draw(LXParameter param) {
        if (param instanceof BoundedParameter) {
            draw((BoundedParameter) param);
        } else if (param instanceof DiscreteParameter) {
            draw((DiscreteParameter) param);
        } else if (param instanceof BooleanParameter) {
            draw((BooleanParameter) param);
        } else if (param instanceof ColorParameter) {
            draw((ColorParameter) param);
        }
    }

    public void menuItem(BooleanParameter p) {
        menuItem(p, getID(p));
    }

    public void menuItem(BooleanParameter p, String label) {
        boolean res = UI.menuItemToggle(label, null, p.getValueb(), true);
        if (res != p.getValueb()) {
            lx.engine.addTask(() -> p.setValue(res));
        }
    }
}
