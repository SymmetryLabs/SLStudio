package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXComponent;
import heronarts.lx.LXEffect;
import heronarts.lx.LXLook;
import heronarts.lx.LXPattern;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.LXParameter;

public class ColorSwatchWindow extends CloseableWindow {
    protected final LX lx;
    protected final LookEditor lookEditor;

    public ColorSwatchWindow(LX lx, LookEditor lookEditor) {
        super("Color swatches");
        this.lx = lx;
        this.lookEditor = lookEditor;
    }

    @Override
    protected void drawContents() {
        if (UI.button("RED")) {
            lx.engine.addTask(() -> setColors(0.f, 100.f, 100.f));
        }
    }

    private void setColors(float h, float s, float b) {
        LXLook look = lookEditor.getLook();
        for (LXChannel chan : look.channels) {
            for (LXPattern pat : chan.getPatterns()) {
                setColorParams(pat, h, s, b);
            }
            for (LXEffect eff : chan.getEffects()) {
                setColorParams(eff, h, s, b);
            }
        }
    }

    private void setColorParams(LXComponent c, float h, float s, float b) {
        for (LXParameter param : c.getParameters()) {
            if (param instanceof ColorParameter) {
                ColorParameter cp = (ColorParameter) param;
                cp.hue.setValue(h);
                cp.saturation.setValue(s);
                cp.brightness.setValue(b);
            } else if (param.getLabel().toLowerCase().equals("hue")) {
                param.setValue(h);
            } else if (param.getLabel().toLowerCase().equals("saturation")) {
                param.setValue(s);
            } else if (param.getLabel().toLowerCase().equals("color")) {
                param.setValue(h);
            }
        }
    }
}
