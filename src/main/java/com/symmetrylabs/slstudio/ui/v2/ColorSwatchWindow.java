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
        UI.setNextWindowDefaults(50, 300, 400, 750);
    }

    @Override
    protected void drawContents() {
        UI.pushFont(FontLoader.DEFAULT_FONT_L);
        UI.text("Swatch colors");
        UI.popFont();

        if (UI.button("Add swatch")) {
            lx.engine.addTask(() -> lx.swatches.addSwatch());
        }
        UI.sameLine();
        if (UI.button("Reset to default")) {
            UI.openPopup("resetSwatches");
        }
        if (UI.beginPopup("resetSwatches", false)) {
            UI.text("Really reset swatches to default?");
            if (UI.button("Yes, please remove all of my carefully chosen colors")) {
                lx.swatches.resetToDefault();
                UI.closePopup();
            }
            UI.endPopup();
        }

        final LXLook look = lookEditor.getLook();
        for (SwatchLibrary.Swatch swatch : lx.swatches) {
            ParameterUI.draw(lx, swatch.color);
            if (UI.beginContextMenu("removeSwatch" + swatch.index)) {
                if (UI.contextMenuItem("Remove")) {
                    lx.engine.addTask(() -> lx.swatches.removeSwatch(swatch));
                }
                UI.endContextMenu();
            }
        }

        UI.spacing(5, 20);
        UI.pushFont(FontLoader.DEFAULT_FONT_L);
        UI.text("Activate");
        UI.popFont();

        ParameterUI.draw(lx, lx.swatches.transitionTime, ParameterUI.WidgetType.KNOB);

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

        UI.spacing(5, 20);
        UI.pushFont(FontLoader.DEFAULT_FONT_L);
        UI.text("Channel Scope");
        UI.popFont();

        if (UI.button("[all]")) {
            for (LXChannel chan : look.channels) {
                chan.acceptSwatches.setValue(true);
            }
        }
        UI.sameLine();
        if (UI.button("[none]")) {
            for (LXChannel chan : look.channels) {
                chan.acceptSwatches.setValue(false);
            }
        }
        UI.beginTable(4, "channelScope");
        for (LXChannel chan : look.channels) {
            ParameterUI.toggle(lx, chan.getLabel(), chan.acceptSwatches, false, 0);
            UI.nextCell();
        }
        UI.endTable();
    }
}
