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
import java.util.WeakHashMap;
import heronarts.lx.midi.LXMidiEngine;
import heronarts.lx.midi.LXMidiMapping;
import heronarts.lx.parameter.StringParameter;

public class ParameterUI implements LXMidiEngine.MappingListener {
    public static ParameterUI getDefault(LX lx) {
        return new ParameterUI(lx, false, new State());
    }

    public static ParameterUI getMappable(LX lx) {
        return new ParameterUI(lx, true, new State());
    }

    public enum IntWidget {
        SLIDER,
        BOX,
    };

    public static class State implements Cloneable {
        public boolean preferKnobsForFloats;
        public boolean preferKnobsForToggles;
        public boolean preferKnobsForButtons;
        public IntWidget preferIntWidget;
        public boolean showLabel;
        public boolean allowMapping;

        public State() {
            this.preferKnobsForFloats = false;
            this.preferKnobsForToggles = false;
            this.preferKnobsForButtons = false;
            this.preferIntWidget = IntWidget.SLIDER;
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
    protected final WeakHashMap<LXParameter, Object> mappedParameters = new WeakHashMap<>();

    /* HACK: it is possible for two different sources to be holding down a
       momentary button, like the GUI and a MIDI mapping. LX has no built-in
       mechanism to remember this; momentary boolean parameters don't have a
       semaphore-ish way of representing being held down by more than one thing.
       No one place in the system has the required context to know whether a
       given momentary parameter should be turned off once it's on; ParameterUI
       knows when it should be on because of the GUI, but it doesn't know if the
       parameter should be off or whether it should be on because of some other
       system when the button is being held down. With MIDI, the situation is
       even worse; it knows to turn it on when it receives note-on, and off when
       it receives note-off, but in the middle it has no opinion at all.

       This is "solved" here by remembering the last momentary parameter that was
       clicked on in the GUI, and if we get to that parameter on the next frame
       and it's not being clicked, we conclude that /probably/ it should be off.
       If a MIDI note-on event arrived at the exact same moment that the mouse-up
       arrives, then that assumption will be incorrect and the parameter will be
       incorrectly set to off. */
    private LXParameter clickedMomentaryParameter = null;

    protected ParameterUI(LX lx, boolean registerMappingListener, State initial) {
        this.lx = lx;
        this.mapping = lx.engine.mapping;
        this.stateStack = new Stack<>();
        if (registerMappingListener) {
            lx.engine.midi.addMappingListener(this);
        }
        stateStack.push(initial);
    }

    public void dispose() {
        lx.engine.midi.removeMappingListener(this);
    }

    @Override
    public void mappingAdded(LXMidiEngine engine, LXMidiMapping mapping) {
        mappedParameters.put(mapping.parameter, new Object());
        this.mapping.setControlTarget(null);
    }

    @Override
    public void mappingRemoved(LXMidiEngine engine, LXMidiMapping mapping) {
        mappedParameters.put(mapping.parameter, new Object());
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

    State peek() {
        return stateStack.peek();
    }

    public ParameterUI preferKnobsForButtons(boolean p) {
        stateStack.peek().preferKnobsForButtons = p;
        return this;
    }

    public ParameterUI preferKnobsForFloats(boolean p) {
        stateStack.peek().preferKnobsForFloats = p;
        return this;
    }

    public ParameterUI preferKnobsForToggles(boolean p) {
        stateStack.peek().preferKnobsForToggles = p;
        return this;
    }

    public ParameterUI preferKnobs(boolean p) {
        State s = stateStack.peek();
        s.preferKnobsForButtons = p;
        s.preferKnobsForFloats = p;
        s.preferKnobsForToggles = p;
        return this;
    }

    public ParameterUI preferIntWidget(IntWidget w) {
        stateStack.peek().preferIntWidget = w;
        return this;
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

    protected String getID(LXParameter p, boolean showMappingIndicators) {
        String visibleLabel;
        String stableId;
        String parentId = null;

        visibleLabel = p.getLabel();
        stableId = visibleLabel;

        if (showMappingIndicators && isMapping()) {
            if (mapping.getControlTarget() == p) {
                visibleLabel = visibleLabel + " \u25CF"; // filled circle
            } else {
                visibleLabel = visibleLabel + " \u25CB"; // outline circle
            }
        } else if (showMappingIndicators && mappedParameters.containsKey(p)) {
            visibleLabel = visibleLabel + " \u25C6"; // outline diamond
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

    public ParameterUI draw(BoundedParameter p) {
        if (!stateStack.peek().preferKnobsForFloats) {
            float start = p.getValuef();
            final float res = UI.sliderFloat(getID(p), start, (float) p.range.v0, (float) p.range.v1);
            if (start != res) {
                lx.engine.addTask(() -> p.setValue(res));
            }
            return this;
        }
        final float start = p.getNormalizedf();

        boolean isMapping = isMapping();

        int dotColor = 0;
        if (isMapping) {
            dotColor = mapping.getControlTarget() == p ? 0xFFFFFFFF : 0x88FFFFFF;
        } else if (mappedParameters.containsKey(p)) {
            dotColor = 0xFFFFFFFF;
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
        } else if (UI.isItemDoubleClicked()) {
            p.reset();
        } else if (!isMapping && start != res) {
            final float fres = res;
            lx.engine.addTask(() -> p.setNormalized(fres));
        }
        return this;
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

    public ParameterUI draw(DiscreteParameter p) {
        String[] options = p.getOptions();
        boolean isMapping = isMapping();
        if (options == null) {
            int start = p.getValuei();
            int r;
            switch (stateStack.peek().preferIntWidget) {
            case SLIDER:
                r = UI.sliderInt(
                    getID(p), start, p.getMinValue(), p.getMaxValue() - 1);
                break;
            case BOX:
                r = UI.intBox(getID(p, false), start, 1, p.getMinValue(), p.getMaxValue(), null);
                break;
            default:
                r = start;
            }
            final int res = r;
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
        return this;
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

    public ParameterUI draw(BooleanParameter p) {
        return draw(p, false);
    }

    public ParameterUI draw(BooleanParameter p, boolean important) {
        final boolean start = p.getValueb();
        final boolean isMapping = isMapping();

        int dotColor = 0;
        if (isMapping) {
            dotColor = mapping.getControlTarget() == p ? 0xFFFFFFFF : 0x88FFFFFF;
        } else if (mappedParameters.containsKey(p)) {
            dotColor = 0xFFFFFFFF;
        }

        if (p.getMode() == BooleanParameter.Mode.TOGGLE) {
            if (important && start) {
                UI.pushColor(UI.COLOR_WIDGET, UIConstants.RED);
                UI.pushColor(UI.COLOR_WIDGET_HOVERED, UIConstants.RED_HOVER);
            }
            boolean res;
            if (stateStack.peek().preferKnobsForToggles) {
                res = UI.knobToggle(getID(p, false), start, dotColor);
            } else {
                res = UI.checkbox(getID(p), start);
            }
            if (important && start) {
                UI.popColor(2);
            }
            if (isMapping && UI.isItemClicked()) {
                mapping.setControlTarget(p);
            } else if (!isMapping && start != res) {
                lx.engine.addTask(() -> p.setValue(res));
            }
        } else {
            /* we use isItemActive so we can figure out if the button is held;
               button() only returns true on press, but not on subsequent frames. */
            if (stateStack.peek().preferKnobsForButtons) {
                UI.knobButton(getID(p, false), start, dotColor);
            } else {
                UI.button(getID(p));
            }
            boolean res = UI.isItemActive();

            /* see comment on clickedMomentaryParameter for an explanation of this monstrosity */
            boolean guiHasControl;
            if (res) {
                clickedMomentaryParameter = p;
                guiHasControl = true;
            } else {
                if (clickedMomentaryParameter == p) {
                    guiHasControl = true;
                    clickedMomentaryParameter = null;
                } else {
                    guiHasControl = false;
                }
            }

            if (isMapping && UI.isItemClicked()) {
                mapping.setControlTarget(p);
            } else if (!isMapping && res != start && guiHasControl) {
                /* only set the value if the button is held; if it's not held, let the parameter
                   have whatever value it has, in case something else, like a MIDI mapping,
                   is "holding down the button". */
                lx.engine.addTask(() -> p.setValue(res));
            }
        }
        return this;
    }

    public ParameterUI draw(ColorParameter p) {
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
        return this;
    }

    public ParameterUI draw(StringParameter p) {
        String start = p.getString();
        final String res = UI.inputText(getID(p, false), start);
        if (!res.equals(start)) {
            lx.engine.addTask(() -> p.setValue(res));
        }
        return this;
    }

    public ParameterUI draw(LXParameter param) {
        if (param instanceof BoundedParameter) {
            draw((BoundedParameter) param);
        } else if (param instanceof DiscreteParameter) {
            draw((DiscreteParameter) param);
        } else if (param instanceof BooleanParameter) {
            draw((BooleanParameter) param);
        } else if (param instanceof StringParameter) {
            draw((StringParameter) param);
        } else if (param instanceof ColorParameter) {
            draw((ColorParameter) param);
        }
        return this;
    }

    public ParameterUI menuItem(BooleanParameter p) {
        return menuItem(p, getID(p));
    }

    public ParameterUI menuItem(BooleanParameter p, String label) {
        boolean res = UI.menuItemToggle(label, null, p.getValueb(), true);
        if (res != p.getValueb()) {
            lx.engine.addTask(() -> p.setValue(res));
        }
        return this;
    }
}
