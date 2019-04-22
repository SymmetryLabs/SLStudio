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
    protected void windowSetup() {
        UI.setNextWindowDefaults(50, 300, 450, 550);
    }

    @Override
    protected void drawContents() {
        ParameterUI.draw(lx, lx.swatches.transitionTime, ParameterUI.WidgetType.KNOB);

        UI.spacing(5, 20);

        final LXLook look = lookEditor.getLook();
        for (SwatchLibrary.Swatch swatch : lx.swatches) {
            ParameterUI.draw(lx, swatch.color);
        }

        UI.spacing(5, 20);

        UI.beginTable(4, "swatchButtons");
        for (SwatchLibrary.Swatch swatch : lx.swatches) {
            UI.pushColor(UI.COLOR_BUTTON, swatch.color.getColor());
            UI.pushColor(UI.COLOR_BUTTON_HOVERED, swatch.color.getColor());
            if (UI.button("GO##" + swatch.index, -1, 80)) {
                final SwatchLibrary.Swatch s = swatch;
                lx.engine.addTask(() -> lx.swatches.apply(s, look));
            }
            UI.popColor(2);
            UI.spacing(1, 10);
            UI.nextCell();
        }
        UI.endTable();
    }
}
