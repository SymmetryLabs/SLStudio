/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui.studio.clip;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.clip.LXClip;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.component.UITextBox;
import heronarts.p3lx.ui.studio.mixer.UIMixer;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIChannelClipButton extends UIClipButton implements UIFocus {

    private final LXChannel channel;
    private LXClip clip = null;
    private final UITextBox label;

    private final LXParameterListener redraw = new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
            if (clip != null) {
                redraw();
            }
        }
    };

    public UIChannelClipButton(UI ui, UIMixer mixer, LX lx, LXChannel channel, int index, float x, float y) {
        super(ui, mixer, lx, index, x, y);
        this.channel = channel;

        this.label = (UITextBox) new UITextBox(LABEL_X, PADDING, getContentWidth() - LABEL_X - PADDING, HEIGHT - 2*PADDING) {
            @Override
            public void drawFocus(UI ui, PGraphics pg) {}
        }
        .setTextAlignment(PConstants.LEFT)
        .setBorder(false)
        .addToContainer(this)
        .setVisible(false);

        if (channel != null) {
            channel.arm.addListener(redraw);
        }
    }

    @Override
    protected void setClip(LXClip clip) {
        if (this.clip != clip) {
            if (this.clip != null) {
                this.clip.running.removeListener(redraw);
            }
            this.clip = clip;
            if (this.clip != null) {
                this.label.setParameter(this.clip.label);
                this.label.setVisible(true);
                this.clip.running.addListener(redraw);
                if (hasFocus()) {
                    lx.engine.focusedClip.setClip(this.clip);
                }
            } else {
                this.label.setVisible(false);
                this.label.setParameter(null);
            }
            redraw();
        }
    }

    @Override
    protected void onFocus() {
        this.lx.engine.focusedClip.setClip(this.clip);
        this.lx.engine.focusedChannel.setValue(channel.getIndex());
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        if (this.clip != null) {
            pg.noStroke();
            if (this.clip.isRunning()) {
                pg.fill(channel.arm.isOn() ? ui.theme.getRecordingColor() : ui.theme.getPrimaryColor());
                pg.rect(4, 6, 8, 8);
            } else {
                if (this.channel.arm.isOn()) {
                    pg.fill(ui.theme.getRecordingColor());
                    pg.ellipseMode(PConstants.CORNER);
                    pg.ellipse(4, 6, 8, 8);
                } else {
                    pg.fill(ui.theme.getControlDisabledColor());
                    pg.beginShape();
                    pg.vertex(4, 4);
                    pg.vertex(12, 10);
                    pg.vertex(4, 16);
                    pg.endShape(PConstants.CLOSE);
                }
            }
        }
    }

    @Override
    protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        super.onKeyPressed(keyEvent, keyChar, keyCode);
        if (this.clip == null) {
            if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                consumeKeyEvent();
                this.channel.addClip(this.index);
            }
        } else {
            if ((keyEvent.isMetaDown() || keyEvent.isControlDown()) && keyCode == java.awt.event.KeyEvent.VK_R) {
                this.label.focus();
                this.label.edit();
            } else if (keyCode == java.awt.event.KeyEvent.VK_SPACE) {
                this.clip.trigger();
            } else if (((keyEvent.isMetaDown() || keyEvent.isControlDown()) && keyCode == java.awt.event.KeyEvent.VK_D) ||
                                    keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                consumeKeyEvent();
                this.channel.removeClip(this.clip);
                this.lx.engine.focusedClip.setClip(null);
            }
        }
        if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
            if (this.channel.getIndex() > 0) {
                consumeKeyEvent();
                this.mixer.channelStrips.get(this.lx.engine.getChannel(this.channel.getIndex() - 1)).clipLauncher.clips.get(this.index).focus();
            }
        } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
            if (this.channel.getIndex() < this.lx.engine.channels.size() - 1) {
                consumeKeyEvent();
                this.mixer.channelStrips.get(this.lx.engine.getChannel(this.channel.getIndex() + 1)).clipLauncher.clips.get(this.index).focus();
            } else {
                consumeKeyEvent();
                this.mixer.masterStrip.clipLauncher.clips.get(this.index).focus();
            }
        }
    }

    @Override
    protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        if (this.clip == null) {
            if (mouseEvent.getCount() == 2) {
                this.channel.addClip(this.index);
            }
        } else {
            if (mx < LABEL_X) {
                if (this.clip.isRunning()) {
                    this.clip.stop();
                } else {
                    this.clip.trigger();
                }
            }
        }
    }
}
