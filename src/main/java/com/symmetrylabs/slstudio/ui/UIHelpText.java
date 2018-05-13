package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.slstudio.SLStudio;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContext;
import processing.core.PFont;
import processing.core.PGraphics;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.TOP;

public class UIHelpText extends UI2dContext {
    private final PFont font;
    private final String text;

    public UIHelpText(UI ui, float x, float y, String text) {
        super(ui, x, y, 900, text.split("\n").length * 17);
        this.font = SLStudio.applet.loadFont("Inconsolata-Bold-14.vlw");
        this.text = text;
        setVisible(false);
    }

    protected void onDraw(UI ui, PGraphics pg) {
        pg.textFont(font);
        pg.clear();
        pg.textAlign(LEFT, TOP);
        pg.fill(0xa0ffffff);
        pg.text(text, 0, 0);
    }
}
