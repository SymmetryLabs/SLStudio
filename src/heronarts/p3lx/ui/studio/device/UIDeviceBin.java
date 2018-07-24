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

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.warp.LXWarp;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UI2dScrollContext;
import heronarts.p3lx.ui.studio.mixer.UIMixer;
import processing.core.PConstants;
import processing.core.PGraphics;

public class UIDeviceBin extends UI2dScrollContext {
    public static final int PADDING = UIMixer.PADDING;
    public static final int HEIGHT = UIDevice.HEIGHT;

    // The children of this UI container are kept in the following order:
    //   - 0 or more UIWarpDevices (kept in warpDevices)
    //   - optional UIChannelDevice and UIDeviceBin.Separator (present if channel != null)
    //   - 0 or more UIPatternDevices (kept in patternDevices)
    //   - 0 or more UIEffectDevices (kept in effectDevices)

    private final UI ui;
    private final List<UIWarpDevice> warpDevices = new ArrayList<>();
    private final UIChannelDevice channelDevice;
    private final List<UIPatternDevice> patternDevices = new ArrayList<>();
    private final List<UIEffectDevice> effectDevices = new ArrayList<>();

    private final LXBus bus;
    private final LXChannel channel;

    public UIDeviceBin(final UI ui, LXBus bus, float y, float w) {
        super(ui, PADDING, y, w, HEIGHT);
        setLayout(UI2dContainer.Layout.HORIZONTAL);
        setChildMargin(PADDING);
        setHorizontalScrollingEnabled(true);
        setVerticalScrollingEnabled(false);
        setArrowKeyFocus(UI2dContainer.ArrowKeyFocus.HORIZONTAL);

        this.ui = ui;
        this.bus = bus;

        if (bus instanceof LXChannel) {
            this.channel = (LXChannel) bus;

            this.channel.addListener(new LXChannel.AbstractListener() {
                @Override
                public void patternAdded(LXChannel channel, LXPattern pattern) {
                    addPatternDevice(pattern);
                }

                @Override
                public void patternRemoved(LXChannel channel, LXPattern pattern) {
                    removePatternDevice(pattern);
                }
            });

            this.channelDevice = new UIChannelDevice(ui, this, this.channel);
            this.channelDevice.addToContainer(this);
            new Separator().addToContainer(this);

            for (LXPattern pattern : this.channel.patterns) {
                addPatternDevice(pattern);
            }
            setFocusedPattern(this.channel.getFocusedPattern());

            this.channel.focusedPattern.addListener(new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter parameter) {
                    setFocusedPattern(channel.getFocusedPattern());
                }
            });
        } else {
            // This is the master channel; it doesn't have channel or pattern controls, but
            // it can have warps and effects.
            this.channel = null;
            this.channelDevice = null;
        }

        for (LXEffect effect : bus.getEffects()) {
            addEffectDevice(effect);
        }

        bus.addListener(new LXChannel.AbstractListener() {
            @Override
            public void warpAdded(LXBus bus, LXWarp warp) {
                addWarpDevice(warp);
            }

            @Override
            public void warpRemoved(LXBus bus, LXWarp warp) {
                removeWarpDevice(warp);
            }

            @Override
            public void warpMoved(LXBus bus, LXWarp warp) {
                moveWarpDevice(warp, warp.getIndex());
            }

            @Override
            public void effectAdded(LXBus bus, LXEffect effect) {
                addEffectDevice(effect);
            }

            @Override
            public void effectRemoved(LXBus bus, LXEffect effect) {
                removeEffectDevice(effect);
            }

            @Override
            public void effectMoved(LXBus bus, LXEffect effect) {
                moveEffectDevice(effect, effect.getIndex());
            }
        });
    }

    private void setFocusedPattern(LXPattern pattern) {
        for (UIPatternDevice patternDevice : patternDevices) {
            patternDevice.setVisible(patternDevice.pattern == pattern);
        }
    }

    private void addWarpDevice(LXWarp warp) {
        UIWarpDevice device = new UIWarpDevice(ui, bus, warp);
        device.addToContainer(this, warpDevices.size());
        warpDevices.add(device);
    }

    private int indexOfWarpDevice(LXWarp warp) {
        for (int i = 0; i < warpDevices.size(); i++) {
            if (warpDevices.get(i).warp == warp) {
                return i;
            }
        }
        return -1;
    }

    private void removeWarpDevice(LXWarp warp) {
        int index = indexOfWarpDevice(warp);
        warpDevices.remove(index).removeFromContainer();
        if (index < warpDevices.size()) {
            warpDevices.get(index).focus();
        }
    }

    private void moveWarpDevice(LXWarp warp, int newIndex) {
        int oldIndex = indexOfWarpDevice(warp);
        UIWarpDevice device = warpDevices.get(oldIndex);
        warpDevices.remove(oldIndex);
        warpDevices.add(newIndex, device);
        device.setContainerIndex(newIndex);
    }

    private void addPatternDevice(LXPattern pattern) {
        UIPatternDevice device = new UIPatternDevice(this.ui, this.channel, pattern);
        device.setVisible(false);
        device.addToContainer(this, warpDevices.size() + (channel != null ? 2 : 0) + patternDevices.size());
        this.patternDevices.add(device);
    }

    private int indexOfPatternDevice(LXPattern pattern) {
        for (int i = 0; i < patternDevices.size(); i++) {
            if (patternDevices.get(i).pattern == pattern) {
                return i;
            }
        }
        return -1;
    }

    private void removePatternDevice(LXPattern pattern) {
        patternDevices.remove(indexOfPatternDevice(pattern)).removeFromContainer();
    }

    private void addEffectDevice(LXEffect effect) {
        UIEffectDevice device = new UIEffectDevice(ui, bus, effect);
        device.addToContainer(this);  // always goes at the very end
        effectDevices.add(device);
    }

    private int indexOfEffectDevice(LXEffect effect) {
        for (int i = 0; i < effectDevices.size(); i++) {
            if (effectDevices.get(i).effect == effect) {
                return i;
            }
        }
        return -1;
    }

    private void removeEffectDevice(LXEffect effect) {
        int index = indexOfEffectDevice(effect);
        effectDevices.remove(index).removeFromContainer();
        if (index < effectDevices.size()) {
            effectDevices.get(index).focus();
        }
    }

    private void moveEffectDevice(LXEffect effect, int newIndex) {
        int oldIndex = indexOfEffectDevice(effect);
        UIEffectDevice device = effectDevices.get(oldIndex);
        effectDevices.remove(oldIndex);
        effectDevices.add(newIndex, device);
        device.setContainerIndex(warpDevices.size() + (channel != null ? 2 : 0) + patternDevices.size() + newIndex);
    }

    class Separator extends UI2dComponent {  // three grey dots
        public Separator() {
            super(0, 0, 4, getContentHeight());
        }

        @Override
        protected void onDraw(UI ui, PGraphics pg) {
            pg.ellipseMode(PConstants.CENTER);
            pg.noStroke();
            pg.fill(ui.theme.getControlDisabledColor());
            for (int i = 0; i < 3; ++i) {
                pg.ellipse(this.width/2, this.height/2 + (i - 1)*(this.width + 4), this.width, this.width);
            }
        }
    }
}
