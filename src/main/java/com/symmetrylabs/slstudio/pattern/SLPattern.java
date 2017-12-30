package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/* Non-Patterns */
public abstract class SLPattern extends LXPattern {

    protected final SLStudioLX lx;

    public SLPattern(LX lx) {
        super(lx);

        this.lx = (SLStudioLX)lx;
    }

    protected <T extends LXParameter> T addParam(T param) {
        addParameter(param);
        return param;
    }

    protected BooleanParameter booleanParam(String name) {
        return addParam(new BooleanParameter(name));
    }

    protected BooleanParameter booleanParam(String name, boolean value) {
        return addParam(new BooleanParameter(name, value));
    }

    protected CompoundParameter compoundParam(String name, double value, double min, double max) {
        return addParam(new CompoundParameter(name, value, min, max));
    }

    protected DiscreteParameter discreteParameter(String name, int value, int min, int max) {
        return addParam(new DiscreteParameter(name, value, min, max));
    }

    public void unconsumeKeyEvent() {
        this.keyEventConsumed = false;
    }

    public void consumeKeyEvent() {
        this.keyEventConsumed = true;
    }

    public boolean keyEventConsumed() {
        return this.keyEventConsumed;
    }

    private boolean keyEventConsumed = false;

    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {}
    public void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {}
    public void onMouseClicked(MouseEvent mouseEvent, float mx, float my) {}
    public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {}
    public void onMouseMoved(MouseEvent mouseEvent, float mx, float my) {}
    public void onMouseOver(MouseEvent mouseEvent) {}
    public void onMouseOut(MouseEvent mouseEvent) {}
    public void onMouseWheel(MouseEvent mouseEvent, float mx, float my, float delta) {}
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {}
    public void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {}
    public void onKeyTyped(KeyEvent keyEvent, char keyChar, int keyCode) {}
}
