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

package heronarts.p3lx.ui.studio.mixer;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.clip.LXClip;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.component.UITextBox;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIClipLauncher extends UI2dContainer {

    public static final int NUM_CLIPS = 5;
    public static final int WIDTH = UIMixerStripControls.WIDTH;
    public static final int SPACING = 1;
    public static final int HEIGHT = NUM_CLIPS * UIClipButton.HEIGHT + (NUM_CLIPS-1) * SPACING;

    private final UIClipButton[] clips = new UIClipButton[NUM_CLIPS];

    private final LX lx;
    private final LXChannel channel;

    public UIClipLauncher(UI ui, LX lx, LXBus bus) {
        super(0, 0, WIDTH, HEIGHT);
        setLayout(UI2dContainer.Layout.VERTICAL);
        setChildMargin(SPACING);
        this.lx = lx;
        this.channel = (bus instanceof LXChannel) ? (LXChannel) bus : null;
        setArrowKeyFocus(UI2dContainer.ArrowKeyFocus.VERTICAL);
        for (int i = 0; i < NUM_CLIPS; ++i) {
            this.clips[i] = (UIClipButton) new UIClipButton(ui, 0, i * UIClipButton.HEIGHT).addToContainer(this);
        }
        if (this.channel != null) {
            resetClips();
            channel.addClipListener(new LXChannel.ClipListener() {
                @Override
                public void clipRemoved(LXChannel channel, LXClip clip) {
                    resetClips();
                }

                @Override
                public void clipAdded(LXChannel channel, LXClip clip) {
                    resetClips();
                }
            });
        }
    }

    void resetClips() {
        if (this.channel != null) {
            int i = 0;
            for (LXClip clip : channel.clips) {
                if (i >= NUM_CLIPS) {
                    break;
                }
                this.clips[i++].setClip(clip);
            }
            while (i < NUM_CLIPS) {
                this.clips[i++].setClip(null);
            }
        }
    }

    public class UIClipButton extends UI2dContainer implements UIFocus {

        public static final int HEIGHT = 20;
        public static final int PADDING = 2;
        private static final int LABEL_X = 14;

        private LXClip clip = null;
        private final UITextBox label;

        private final LXParameterListener redraw = new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (clip != null) {
                    redraw();
                }
            }
        };

        UIClipButton(UI ui, float x, float y) {
            super(x, y, WIDTH, HEIGHT);
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

            if (channel != null) {
                channel.arm.addListener(redraw);
            }
        }

        void setClip(LXClip clip) {
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
            lx.engine.focusedClip.setClip(this.clip);
            if (channel != null) {
                lx.engine.focusedChannel.setValue(channel.getIndex());
            }
        }

        @Override
        protected void onDraw(UI ui, PGraphics pg) {
            if (this.clip != null) {
                pg.noStroke();
                if (this.clip.isRunning()) {
                    pg.fill(channel.arm.isOn() ? ui.theme.getRecordingColor() : ui.theme.getPrimaryColor());
                    pg.rect(4, 6, 8, 8);
                } else {
                    if (channel.arm.isOn()) {
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
                if (channel != null) {
                    if (keyCode == java.awt.event.KeyEvent.VK_SPACE || keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                        consumeKeyEvent();
                        channel.addClip();
                    }
                }
            } else if (channel != null) {
                if ((keyEvent.isMetaDown() || keyEvent.isControlDown()) && keyCode == java.awt.event.KeyEvent.VK_R) {
                    this.label.focus();
                    this.label.edit();
                } else if (keyCode == java.awt.event.KeyEvent.VK_SPACE) {
                    this.clip.trigger();
                } else if (((keyEvent.isMetaDown() || keyEvent.isControlDown()) && keyCode == java.awt.event.KeyEvent.VK_D) ||
                                        keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                    consumeKeyEvent();
                    channel.removeClip(this.clip);
                    lx.engine.focusedClip.setClip(null);
                }
            }
        }

        @Override
        protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
            if (this.clip == null) {
                if (channel != null && mouseEvent.getCount() == 2) {
                    channel.addClip();
                }
            } else {
                if (mx < 20) {
                    if (this.clip.isRunning()) {
                        this.clip.stop();
                    } else {
                        this.clip.trigger();
                    }
                }
            }
        }
    }

}
