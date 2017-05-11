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

package heronarts.p3lx.ui.studio.device;

import heronarts.lx.LXChannel;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UITimerTask;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIDoubleBox;
import heronarts.p3lx.ui.component.UIDropMenu;
import processing.core.PGraphics;
import processing.event.KeyEvent;

class UIChannelDevice extends UIDevice {

    private static final int PADDING = 4;
    private static final int PATTERN_LIST_WIDTH = 140;
    private static final int WIDTH = PATTERN_LIST_WIDTH;

    private final UIPatternList patternList;

    UIChannelDevice(UI ui, UIDeviceBin deviceBin, final LXChannel channel) {
        super(ui, channel, WIDTH);
        setTitle(channel.label);

        this.patternList = (UIPatternList)
        new UIPatternList(ui, 0, 0, PATTERN_LIST_WIDTH, getContentHeight() - 40, channel)
        .setDescription("Patterns available on this channel, click to select, double-click to activate")
        .addToContainer(this);

        // Transition Controls
        new UIButton(0, getContentHeight() - 36, 16, 16)
        .setLabel("\u21C4")
        .setParameter(channel.transitionEnabled)
        .setTextOffset(0, -1)
        .addToContainer(this);
        new UIDropMenu(18, getContentHeight() - 36, 80, 16, channel.transitionBlendMode)
        .setDirection(UIDropMenu.Direction.UP)
        .addToContainer(this);
        new UITransitionBox(channel, 100, getContentHeight() - 36, 40, 16)
        .setParameter(channel.transitionTimeSecs)
        .setShiftMultiplier(.1f)
        .addToContainer(this);

        // Auto cycle controls
        new UIButton(0, getContentHeight() - 16, 16, 16)
        .setLabel("\u21BA")
        .setParameter(channel.autoCycleEnabled)
        .addToContainer(this);
        new UIAutoCycleBox(channel, 18, getContentHeight() - 16, 122, 16)
        .setParameter(channel.autoCycleTimeSecs)
        .setShiftMultiplier(60)
        .addToContainer(this);
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        super.onKeyPressed(keyEvent, keyChar, keyCode);
        if (!keyEventConsumed()) {
            if (keyCode == java.awt.event.KeyEvent.VK_UP || keyCode == java.awt.event.KeyEvent.VK_DOWN) {
                this.patternList.onKeyPressed(keyEvent, keyChar, keyCode);
            }
        }
    }

    abstract class UIProgressBox extends UIDoubleBox {
        protected final LXChannel channel;
        protected int progress = 0;

        abstract protected boolean hasProgress();
        abstract protected double getProgress();

        UIProgressBox(final LXChannel channel, float x, float y, float w, float h) {
            super(x, y, w, h);
            this.channel = channel;
            addLoopTask(new UITimerTask(30, UITimerTask.Mode.FPS) {
                @Override
                public void run() {
                    if (hasProgress()) {
                        int newProgress = (int) (getProgress() * (width-5));
                        if (newProgress != progress) {
                            progress = newProgress;
                            redraw();
                        }
                    } else {
                        if (progress != 0) {
                            progress = 0;
                            redraw();
                        }
                    }
                }
            });
        }

        @Override
        public void onDraw(UI ui, PGraphics pg) {
            if (progress > 0) {
                pg.noFill();
                pg.stroke(ui.theme.getPrimaryColor());
                pg.line(2, height-2, 2 + progress, height-2);
            }
            super.onDraw(ui, pg);
        }
    }

    class UITransitionBox extends UIProgressBox {
        UITransitionBox(LXChannel channel, float x, float y, float w, float h) {
            super(channel, x, y, w, h);
        }

        @Override
        protected boolean hasProgress() {
            return this.channel.transitionEnabled.isOn();
        }

        @Override
        protected double getProgress() {
            return this.channel.getTransitionProgress();
        }

    }

    class UIAutoCycleBox extends UIProgressBox {
        UIAutoCycleBox(LXChannel channel, float x, float y, float w, float h) {
            super(channel, x, y, w, h);
        }

        @Override
        protected boolean hasProgress() {
            return this.channel.autoCycleEnabled.isOn();
        }

        @Override
        protected double getProgress() {
            return this.channel.getAutoCycleProgress();
        }
    }
}
