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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.warp.LXWarp;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
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

    private final UI ui;
    private final UIChannelDevice channelDevice;
    private int effectDeviceOffset;
    private int effectContainerOffset;

    private final List<UIDevice> devices = new ArrayList<UIDevice>();

    private final Map<LXPattern, UIPatternDevice> patternDevices =
        new HashMap<LXPattern, UIPatternDevice>();

    private final LXChannel channel;

    public UIDeviceBin(final UI ui, LXBus bus, float y, float w) {
        super(ui, PADDING, y, w, HEIGHT);
        setLayout(UI2dContainer.Layout.HORIZONTAL);
        setChildMargin(PADDING);
        setHorizontalScrollingEnabled(true);
        setVerticalScrollingEnabled(false);
        setArrowKeyFocus(UI2dContainer.ArrowKeyFocus.HORIZONTAL);
        this.ui = ui;

        if (bus instanceof LXChannel) {
            this.channel = (LXChannel) bus;
            this.effectDeviceOffset = 1;
            this.effectContainerOffset = 2;

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
            addDevice(this.channelDevice, 0);

            for (LXPattern pattern : this.channel.patterns) {
                addPatternDevice(pattern);
            }

            new UI2dComponent(0, 0, 4, getContentHeight()) {
                @Override
                protected void onDraw(UI ui, PGraphics pg) {
                    pg.ellipseMode(PConstants.CENTER);
                    pg.noStroke();
                    pg.fill(ui.theme.getControlDisabledColor());
                    for (int i = 0; i < 3; ++i) {
                        pg.ellipse(this.width/2, this.height/2 + (i-1) * (this.width + 4), this.width, this.width);
                    }
                }
            }.addToContainer(this);

            setFocusedPattern(this.channel.getFocusedPattern());
            this.channel.focusedPattern.addListener(new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter parameter) {
                    setFocusedPattern(channel.getFocusedPattern());
                }
            });
        } else {
            this.channel = null;
            this.channelDevice = null;
            this.effectDeviceOffset = 0;
            this.effectContainerOffset = 0;
        }
        for (LXEffect effect : bus.getEffects()) {
            addDevice(new UIEffectDevice(ui, bus, effect), -1);
        }

        bus.addListener(new LXChannel.AbstractListener() {
            @Override
            public void warpAdded(LXBus channel, LXWarp warp) {
                addDevice(new UIWarpDevice(ui, bus, warp), -1);
            }

            @Override
            public void warpRemoved(LXBus bus, LXWarp warp) {
                UIDevice warpDevice = findWarpDevice(warp);
                if (warpDevice != null) {
                    int index = devices.indexOf(warpDevice);
                    removeDevice(warpDevice);
                    if (index >= devices.size()) {
                        index = devices.size() - 1;
                    }
                    if (index >= 0) {
                        devices.get(index).focus();
                    }
                }
            }

            @Override
            public void warpMoved(LXBus bus, LXWarp warp) {
                UIWarpDevice warpDevice = findWarpDevice(warp);
                if (warpDevice != null) {
                    devices.remove(warpDevice);
                    devices.add(warp.getIndex() + effectDeviceOffset, warpDevice);
                    warpDevice.setContainerIndex(warp.getIndex() + effectContainerOffset);
                }
            }

            private UIWarpDevice findWarpDevice(LXWarp warp) {
                for (UIDevice device : devices) {
                    if (device instanceof UIWarpDevice) {
                        if (((UIWarpDevice) device).warp == warp) {
                            return (UIWarpDevice) device;
                        }
                    }
                }
                return null;
            }

            @Override
            public void effectAdded(LXBus bus, LXEffect effect) {
                addDevice(new UIEffectDevice(ui, bus, effect), -1);
            }

            @Override
            public void effectRemoved(LXBus bus, LXEffect effect) {
                UIDevice effectDevice = findEffectDevice(effect);
                if (effectDevice != null) {
                    int index = devices.indexOf(effectDevice);
                    removeDevice(effectDevice);
                    if (index >= devices.size()) {
                        index = devices.size() - 1;
                    }
                    if (index >= 0) {
                        devices.get(index).focus();
                    }
                }
            }

            @Override
            public void effectMoved(LXBus bus, LXEffect effect) {
                UIEffectDevice effectDevice = findEffectDevice(effect);
                if (effectDevice != null) {
                    devices.remove(effectDevice);
                    devices.add(effect.getIndex() + effectDeviceOffset, effectDevice);
                    effectDevice.setContainerIndex(effect.getIndex() + effectContainerOffset);
                }
            }
        });
    }

    private UIEffectDevice findEffectDevice(LXEffect effect) {
        for (UIDevice device : devices) {
            if (device instanceof UIEffectDevice) {
                if (((UIEffectDevice) device).effect == effect) {
                    return (UIEffectDevice) device;
                }
            }
        }
        return null;
    }

    private UIDeviceBin addDevice(UIDevice device, int index) {
        if (index < 0) {
            device.addToContainer(this);
            this.devices.add(device);
        } else {
            device.addToContainer(this, index);
            this.devices.add(index, device);
        }
        return this;
    }

    private UIDeviceBin removeDevice(UIDevice device) {
        int index = this.devices.indexOf(device);
        if (index >= 0) {
            this.devices.remove(index).removeFromContainer();
        }
        return this;
    }

    private void setFocusedPattern(LXPattern pattern) {
        for (UIPatternDevice patternDevice : this.patternDevices.values()) {
            patternDevice.setVisible(patternDevice.pattern == pattern);
        }
    }

    private void addPatternDevice(LXPattern pattern) {
        UIPatternDevice patternDevice = new UIPatternDevice(this.ui, this.channel, pattern);
        patternDevice.setVisible(false);
        this.patternDevices.put(pattern, patternDevice);
        addDevice(patternDevice, 1);
        ++this.effectDeviceOffset;
        ++this.effectContainerOffset;
    }

    private void removePatternDevice(LXPattern pattern) {
        UIPatternDevice patternDevice = this.patternDevices.remove(pattern);
        if (patternDevice != null) {
            removeDevice(patternDevice);
            --this.effectDeviceOffset;
            --this.effectContainerOffset;
        }
    }
}
