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
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIObject;
import heronarts.p3lx.ui.studio.UIBottomTray;
import processing.event.KeyEvent;

public class UIDeviceBin extends UI2dContainer {

    private static final int MARGIN = UIBottomTray.PADDING;
    static final int HEIGHT = UIBottomTray.HEIGHT - 2*MARGIN;
    static final int PADDING = 6;

    private final UIBottomTray tray;
    private final UIChannelDevice channelDevice;
    private final int effectDeviceOffset;

    private final List<UIDevice> devices = new ArrayList<UIDevice>();

    public UIDeviceBin(final UI ui, final UIBottomTray tray, LXBus channel, float x) {
        super(x, MARGIN, 0, HEIGHT);
        this.tray = tray;

        setBackgroundColor(ui.theme.getPaneInsetColor());
        setBorderRounding(4);

        if (channel instanceof LXChannel) {
            this.channelDevice = new UIChannelDevice(ui, (LXChannel) channel);
            this.effectDeviceOffset = 1;
            addDevice(this.channelDevice);
        } else {
            this.effectDeviceOffset = 0;
            this.channelDevice = null;
        }
        for (LXEffect effect : channel.getEffects()) {
            addDevice(new UIEffectDevice(ui, channel, effect));
        }

        channel.addListener(new LXChannel.AbstractListener() {
            @Override
            public void effectAdded(LXBus bus, LXEffect effect) {
                addDevice(new UIEffectDevice(ui, bus, effect));
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
                    effectDevice.setContainerIndex(effect.getIndex() + effectDeviceOffset);
                    onDeviceChange();
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

    private float getNextDeviceX() {
        if (this.devices.size() > 0) {
            UIDevice lastDevice = this.devices.get(this.devices.size()-1);
            return  lastDevice.getX() + lastDevice.getWidth() + PADDING;
        }
        return PADDING;
    }

    public void updateWidth() {
        updateWidth(getNextDeviceX());
    }

    private void updateWidth(float w) {
        setWidth(Math.max(w, this.tray.getWidth() - this.x - UIBottomTray.PADDING));
    }

    public UIDeviceBin onDeviceChange() {
        float x = PADDING;
        for (UIDevice device : this.devices) {
            device.setX(x);
            x += device.getWidth() + PADDING;
        }
        updateWidth(x);
        return this;
    }

    public UIDeviceBin addDevice(UIDevice device) {
        device.setX(getNextDeviceX());
        device.addToContainer(this);
        this.devices.add(device);
        updateWidth(device.getX() + device.getWidth() + PADDING);
        return this;
    }

    public UIDeviceBin removeDevice(UIDevice device) {
        int index = this.devices.indexOf(device);
        if (index >= 0) {
            this.devices.remove(index).removeFromContainer();
            float x = device.getX();
            while (index < this.devices.size()) {
                UIDevice next = this.devices.get(index);
                next.setX(x);
                x += next.getWidth() + PADDING;
                ++index;
            }
            updateWidth(x);
        }
        return this;
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_LEFT ||
                keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
            UIObject focusedChild = getFocusedChild();
            if (focusedChild != null) {
                int index = this.devices.indexOf(focusedChild);
                if (index >= 0) {
                    if (keyCode == java.awt.event.KeyEvent.VK_LEFT && index > 0) {
                        this.devices.get(index-1).focus();
                        consumeKeyEvent();
                    } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT && (index < this.devices.size() - 1)) {
                        this.devices.get(index+1).focus();
                        consumeKeyEvent();
                    }
                }
            } else if (hasDirectFocus()) {
                if (this.devices.size() > 0) {
                    this.devices.get(0).focus();
                    consumeKeyEvent();
                }
            }
        }
    }
}