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
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXMasterChannel;
import heronarts.lx.clip.LXClip;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.UITriggerTarget;
import heronarts.p3lx.ui.component.UITextBox;
import heronarts.p3lx.ui.studio.mixer.UIMixer;
import heronarts.p3lx.ui.studio.mixer.UIMixerStrip;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIClipButton extends UI2dContainer implements UIFocus, UITriggerTarget {

    public static final int HEIGHT = 20;
    public static final int PADDING = 2;
    protected static final int LABEL_X = 16;

    protected final UIMixer mixer;
    protected final LX lx;
    protected final int index;

    private final LXBus bus;
    private LXClip clip = null;

    private final UITextBox label;

    private final LXParameterListener redraw = new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
            if (clip != null) {
                redraw();
            }
        }
    };

    protected UIClipButton(UI ui, UIMixer mixer, final LX lx, LXBus bus, int index, float x, float y) {
        super(x, y, UIClipLauncher.WIDTH, HEIGHT);
        this.mixer = mixer;
        this.lx = lx;
        this.bus = bus;
        this.index = index;
        setBorderColor(ui.theme.getControlBorderColor());
        setBackgroundColor(ui.theme.getControlBackgroundColor());

        this.label = (UITextBox) new UITextBox(LABEL_X, PADDING, getContentWidth() - LABEL_X - PADDING, HEIGHT - 2*PADDING) {
            @Override
            public void drawFocus(UI ui, PGraphics pg) {}
        }
        .setTextAlignment(PConstants.LEFT)
        .setBorder(false)
        .addToContainer(this)
        .setVisible(false);

        bus.arm.addListener(this.redraw);

        lx.engine.focusedClip.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (clip != null && lx.engine.focusedClip.getClip() == clip) {
                    if (!hasFocus()) {
                        focus();
                    }
                }
            }
        });
    }

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
        if (this.bus instanceof LXChannel) {
            this.lx.engine.focusedChannel.setValue(((LXChannel)this.bus).getIndex());
        } else if (this.bus instanceof LXMasterChannel) {
            this.lx.engine.focusedChannel.setValue(this.lx.engine.channels.size());
        }
    }

    public static void drawPlayTriangle(UI ui, PGraphics pg) {
        pg.stroke(ui.theme.getControlBorderColor());
        pg.beginShape();
        pg.vertex(4, 4);
        pg.vertex(14, 10);
        pg.vertex(4, 16);
        pg.endShape(PConstants.CLOSE);
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        if (this.clip != null) {
            pg.noStroke();
            if (this.bus.arm.isOn() && this.clip.isRunning()) {
                pg.fill(ui.theme.getRecordingColor());
                pg.ellipseMode(PConstants.CORNER);
                pg.ellipse(4, 6, 8, 8);
            } else {
                pg.fill(this.bus.arm.isOn() ? ui.theme.getRecordingColor() :
                    (this.clip.isRunning() ? ui.theme.getPrimaryColor() : ui.theme.getControlDisabledColor()));
                drawPlayTriangle(ui, pg);
            }
        }
    }

    @Override
    protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        super.onKeyPressed(keyEvent, keyChar, keyCode);
        if (this.clip == null) {
            if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                consumeKeyEvent();
                this.bus.addClip(this.index);
            }
        } else {
            if ((keyEvent.isMetaDown() || keyEvent.isControlDown()) && keyCode == java.awt.event.KeyEvent.VK_R) {
                consumeKeyEvent();
                this.label.focus();
                this.label.edit();
            } else if ((keyEvent.isMetaDown() || keyEvent.isControlDown()) && keyCode == java.awt.event.KeyEvent.VK_L) {
                consumeKeyEvent();
                this.clip.loop.toggle();
            } else if ((keyEvent.isMetaDown() || keyEvent.isControlDown()) && keyCode == java.awt.event.KeyEvent.VK_A) {
                consumeKeyEvent();
                this.bus.arm.toggle();
            } else if (keyCode == java.awt.event.KeyEvent.VK_SPACE) {
                consumeKeyEvent();
                if (this.clip.isRunning()) {
                    this.clip.stop();
                } else {
                    this.clip.trigger();
                }
            } else if (((keyEvent.isMetaDown() || keyEvent.isControlDown()) && keyCode == java.awt.event.KeyEvent.VK_D) ||
                                    keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                consumeKeyEvent();
                this.bus.removeClip(this.clip);
                this.lx.engine.focusedClip.setClip(null);
            }
        }
        if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
            UIMixerStrip target = null;
            if (this.bus instanceof LXMasterChannel) {
                target = this.mixer.channelStrips.get(this.lx.engine.getChannel(this.lx.engine.channels.size() - 1));
            } else if (this.bus instanceof LXChannel) {
                LXChannel channel = (LXChannel) this.bus;
                if (channel.getIndex() > 0) {
                    target = this.mixer.channelStrips.get(this.lx.engine.getChannel(channel.getIndex() - 1));
                }
            }
            if (target != null) {
                consumeKeyEvent();
                target.clipLauncher.clips.get(this.index).focus();
            }
        } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
            if (this.bus instanceof LXChannel) {
                consumeKeyEvent();
                UIMixerStrip target = null;
                LXChannel channel = (LXChannel) this.bus;
                if (channel.getIndex() < this.lx.engine.channels.size() - 1) {
                    target = this.mixer.channelStrips.get(this.lx.engine.getChannel(channel.getIndex() + 1));
                } else {
                    target = this.mixer.masterStrip;
                }
                target.clipLauncher.clips.get(this.index).focus();
            } else if (this.bus instanceof LXMasterChannel) {
                this.mixer.sceneStrip.sceneLauncher.scenes.get(this.index).focus();
            }
        }
    }

    @Override
    protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        if (this.clip == null) {
            if (mouseEvent.getCount() == 2) {
                this.bus.addClip(this.index);
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

    @Override
    public BooleanParameter getTriggerTarget() {
        return (this.clip != null) ? this.clip.trigger : null;
    }

}
