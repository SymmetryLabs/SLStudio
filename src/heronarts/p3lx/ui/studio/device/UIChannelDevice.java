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

import java.util.HashMap;
import java.util.Map;

import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UITimerTask;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIDoubleBox;
import heronarts.p3lx.ui.component.UIDropMenu;
import processing.core.PGraphics;

class UIChannelDevice extends UIDevice {

    private static final int PADDING = 4;
    private static final int PATTERN_LIST_WIDTH = 140;
    private static final int WIDTH = 310;

    private final UI ui;
    private final LXChannel channel;
    private final Map<LXPattern, UIPatternControl> patternControls =
        new HashMap<LXPattern, UIPatternControl>();

    UIChannelDevice(UI ui, final LXChannel channel) {
        super(ui, WIDTH);
        this.ui = ui;
        this.channel = channel;
        setTitle(channel.label);

        channel.addListener(new LXChannel.AbstractListener() {
            @Override
            public void patternAdded(LXChannel channel, LXPattern pattern) {
                addPattern(pattern);
            }

            @Override
            public void patternRemoved(LXChannel channel, LXPattern pattern) {
                removePattern(pattern);
            }
        });

        final UIPatternList patternList = new UIPatternList(ui, this, 0, 0, PATTERN_LIST_WIDTH, getContentHeight() - 40, channel);
        patternList.addToContainer(this);

        // Transition Controls
        new UIButton(0, getContentHeight() - 36, 16, 16)
        .setLabel("⇄")
        .setParameter(channel.transitionsEnabled)
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

        for (LXPattern pattern : channel.getPatterns()) {
            addPattern(pattern);
        }
        setFocusedPattern(channel.getActivePattern());
    }

    void setFocusedPattern(LXPattern focusedPattern) {
        for (UIPatternControl patternControl : this.patternControls.values()) {
            boolean visible = patternControl.pattern == focusedPattern;
            patternControl.setVisible(visible);
            if (visible) {
                setExpandedWidth(patternControl.getWidth() + PATTERN_LIST_WIDTH + 3*PADDING);
            }
        }
    }

    private void addPattern(LXPattern pattern) {
        UIPatternControl patternControl = new UIPatternControl(this.ui, pattern, 144, 0, 140);
        patternControl.setVisible(this.channel.getActivePattern() == pattern);
        this.patternControls.put(pattern, patternControl);
        patternControl.addToContainer(this);
    }

    private void removePattern(LXPattern pattern) {
        UIPatternControl patternControl = this.patternControls.remove(pattern);
        if (patternControl != null) {
            patternControl.removeFromContainer();
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
            addLoopTask(new UITimerTask(30, UITimerTask.FPS) {
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
            return this.channel.transitionsEnabled.isOn();
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
