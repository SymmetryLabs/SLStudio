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
import heronarts.lx.LXMappingEngine;
import java.util.Stack;
import java.util.LinkedList;

public class ParameterUI {
    public enum WidgetType {
        KNOB,
        SLIDER,
    };

    public static ParameterUI getDefault(LX lx) {
        return new ParameterUI(lx, new State());
    }

    protected static class State implements Cloneable {
        WidgetType defaultBoundedWidget;
        boolean showLabel;
        boolean allowMapping;

        public State() {
            this.defaultBoundedWidget = WidgetType.SLIDER;
            this.showLabel = true;
            this.allowMapping = false;
        }

        @Override
        public State clone() {
            try {
                return (State) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected final LX lx;
    protected final LXMappingEngine mapping;
    protected Stack<State> stateStack;

    protected ParameterUI(LX lx, State initial) {
        this.lx = lx;
        this.mapping = lx.engine.mapping;
        this.stateStack = new Stack<>();
        stateStack.push(initial);
    }

    public ParameterUI push() {
        stateStack.push(stateStack.peek().clone());
        return this;
    }

    public ParameterUI pop() {
        if (stateStack.size() > 1) {
            stateStack.pop();
        }
        return this;
    }

    public ParameterUI setDefaultBoundedWidget(WidgetType t) {
        stateStack.peek().defaultBoundedWidget = t;
        return this;
    }

    WidgetType getDefaultBoundedWidget() {
        return stateStack.peek().defaultBoundedWidget;
    }

    public ParameterUI showLabel(boolean showLabel) {
        stateStack.peek().showLabel = showLabel;
        return this;
    }

    public ParameterUI allowMapping(boolean allowMapping) {
        stateStack.peek().allowMapping = allowMapping;
        return this;
    }

    public String getID(LXParameter p) {
        return getID(p, true);
    }

    protected String getID(LXParameter p, boolean showMappingIndicator) {
        String visibleLabel;
        String stableId;
        String parentId = null;

        visibleLabel = p.getLabel();
        stableId = visibleLabel;

        if (showMappingIndicator && isMapping()) {
            if (mapping.getControlTarget() == p) {
                visibleLabel = visibleLabel + "**";
            } else {
                visibleLabel = visibleLabel + " \u25CF";
            }
        }

        LXComponent parent = p.getComponent();
        if (parent != null) {
            parentId = Integer.toString(parent.getId());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(visibleLabel);
        sb.append("###");
        sb.append(stableId);
        if (parentId != null) {
            sb.append("/");
            sb.append(parentId);
        }
        return sb.toString();
    }

    protected boolean isMapping() {
        return stateStack.peek().allowMapping && mapping.mode.getEnum() == LXMappingEngine.Mode.MIDI;
    }

    public void draw(BoundedParameter p) {
        draw(p, stateStack.peek().defaultBoundedWidget);
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

        boolean isMapping = isMapping();

        int dotColor = 0;
        if (isMapping) {
            dotColor = mapping.getControlTarget() == p ? 0xFFFF0000 : 0xFFFFFFFF;
        }

        final float res =
            p instanceof CompoundParameter
            ? compoundKnob((CompoundParameter) p, dotColor)
            : UI.knobFloat(getID(p, false), p.getValuef(), start, dotColor);

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

        if (isMapping && UI.isItemClicked()) {
            mapping.setControlTarget(p);
        } else if (!isMapping && start != res) {
            final float fres = res;
            lx.engine.addTask(() -> p.setNormalized(fres));
        }
    }

    private float compoundKnob(CompoundParameter cp, int dotColor) {
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
            getID(cp, false), (float) cp.getBaseValue(), (float) cp.getBaseNormalized(),
            cp.getNormalizedf(), N, mins, maxs, colors, dotColor);
    }

    public void draw(DiscreteParameter p) {
        String[] options = p.getOptions();
        boolean isMapping = isMapping();
        if (options == null) {
            int start = p.getValuei();
            final int res = UI.sliderInt(
                getID(p), start, p.getMinValue(), p.getMaxValue() - 1);
            if (isMapping && UI.isItemClicked()) {
                mapping.setControlTarget(p);
            } else if (!isMapping && start != res) {
                lx.engine.addTask(() -> p.setValue(res));
            }
        } else {
            int start = p.getValuei();
            int res = UI.combo(getID(p), start, options);
            if (isMapping && UI.isItemClicked()) {
                mapping.setControlTarget(p);
            } else if (!isMapping && start != res) {
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
        final boolean isMapping = isMapping();
        if (isMapping && UI.isItemClicked()) {
            mapping.setControlTarget(p);
        } else if (!isMapping && start != res) {
            lx.engine.addTask(() -> p.setValue(res));
        }
        return res;
    }

    public boolean toggle(String label, BooleanParameter p, boolean important, float w) {
        final boolean start = p.getValueb();
        final boolean res = toggle(label, start, important, w);
        final boolean isMapping = isMapping();
        if (isMapping && UI.isItemClicked()) {
            mapping.setControlTarget(p);
        } else if (!isMapping && start != res) {
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
        final boolean isMapping = isMapping();
        if (isMapping && UI.isItemClicked()) {
            mapping.setControlTarget(p);
        } else if (!isMapping() && start != res) {
            lx.engine.addTask(() -> p.setValue(res));
        }
    }

    public void draw(ColorParameter p) {
        int start = p.getColor();
        float h = p.hue.getValuef();
        float s = p.saturation.getValuef();
        float b = p.brightness.getValuef();
        float[] res = UI.colorPickerHSV(getID(p), h, s, b);
        if (!isMapping() && (h != res[0] || s != res[1] || b != res[2])) {
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
