package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.slstudio.SLStudio;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContext;
import heronarts.p3lx.ui.UI3dContext;
import heronarts.p3lx.ui.UIObject;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/** Draws some text overlaid on top of another UI panel. */
public class UITextOverlay extends UI2dContext {
    protected UIObject bg;
    protected int anchorX = 0;
    protected int anchorY = 0;
    protected int alignX = PConstants.LEFT;
    protected int alignY = PConstants.TOP;
    protected String text = "";
    protected int color = 0xa0ffffff;

    public UITextOverlay(UI ui, UIObject bg, int anchorX, int anchorY, int alignX, int alignY) {
        super(ui, bg.getX(), bg.getY(), bg.getWidth(), bg.getHeight());
        this.bg = bg;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.alignX = alignX;
        this.alignY = alignY;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int c) {
        color = c;
    }

    protected void onDraw(UI ui, PGraphics pg) {
        pg.textFont(SLStudio.MONO_FONT.getFont());
        pg.clear();
        pg.textAlign(alignX, alignY);
        pg.fill(color);
        float x = anchorX < 0 ? bg.getWidth() + anchorX : anchorX;
        float y = anchorY < 0 ? bg.getHeight() + anchorY : anchorY;
        pg.text(getText(), x, y);
    }

    public void onMousePressed(MouseEvent event, float mx, float my) {
        bg.onMousePressed(event, mx, my);
    }

    public void onMouseReleased(MouseEvent event, float mx, float my) {
        bg.onMouseReleased(event, mx, my);
    }

    public void onMouseClicked(MouseEvent event, float mx, float my) {
        bg.onMouseClicked(event, mx, my);
    }

    public void onMouseDragged(MouseEvent event, float mx, float my, float dx, float dy) {
        bg.onMouseDragged(event, mx, my, dx, dy);
    }

    public void onMouseMoved(MouseEvent event, float mx, float my) {
        bg.onMouseMoved(event, mx, my);
    }

    public void onMouseOver(MouseEvent event) {
        bg.onMouseOver(event);
    }

    public void onMouseOut(MouseEvent event) {
        bg.onMouseOut(event);
    }

    public void onMouseWheel(MouseEvent event, float mx, float my, float delta) {
        bg.onMouseWheel(event, mx, my, delta);
    }

    public void onKeyPressed(KeyEvent event, char keyChar, int keyCode) {
        bg.onKeyPressed(event, keyChar, keyCode);
    }

    public void onKeyReleased(KeyEvent event, char keyChar, int keyCode) {
        bg.onKeyReleased(event, keyChar, keyCode);
    }

    public void onKeyTyped(KeyEvent event, char keyChar, int keyCode) {
        bg.onKeyTyped(event, keyChar, keyCode);
    }
}
