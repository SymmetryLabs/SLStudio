package com.symmetrylabs.slstudio.ui.v2;

import com.symmetrylabs.slstudio.palettes.SwatchLibrary;
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
        final LXLook look = lookEditor.getLook();
        for (SwatchLibrary.Swatch swatch : lx.swatches) {
            ParameterUI.draw(lx, swatch.color);
            if (UI.button("Apply##" + swatch.index)) {
                final SwatchLibrary.Swatch s = swatch;
                lx.engine.addTask(() -> s.apply(look));
            }
        }
    }
}
