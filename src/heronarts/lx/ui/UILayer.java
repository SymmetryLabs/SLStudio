/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.ui;

import heronarts.lx.LXKeyEvent;

public interface UILayer {
    public void draw();
    public boolean mousePressed(float mx, float my);
    public boolean mouseReleased(float mx, float my);
    public boolean mouseClicked(float mx, float my);
    public boolean mouseDragged(float mx, float my);
    public boolean mouseWheel(float mx, float my, float delta);
    public boolean keyPressed(LXKeyEvent keyEvent, char keyChar, int keyCode);
    public boolean keyReleased(LXKeyEvent keyEvent, char keyChar, int keyCode);
    public boolean keyTyped(LXKeyEvent keyEvent, char keyChar, int keyCode);
}
