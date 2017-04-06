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

package heronarts.p3lx.ui.studio;

import java.util.HashMap;
import java.util.Map;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEngine;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.LXStudio;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dScrollContext;
import heronarts.p3lx.ui.studio.device.UIDeviceBin;
import heronarts.p3lx.ui.studio.mixer.UIMixer;
import heronarts.p3lx.ui.studio.mixer.UIMixerStrip;
import processing.core.PGraphics;
import processing.event.KeyEvent;

public class UIBottomTray extends UI2dScrollContext {

    public static final int PADDING = 8;
    public static final int HEIGHT = 196;
    private static final int SEPARATOR = 16;

    private final UI ui;
    private final LX lx;
    private final UIMixer mixer;
    private final Map<LXBus, UIDeviceBin> deviceBins = new HashMap<LXBus, UIDeviceBin>();

    public UIBottomTray(UI ui, LX lx) {
        super(ui, 0, ui.getHeight() - HEIGHT - UIContextualHelpBar.HEIGHT, ui.getWidth(), HEIGHT);
        setHorizontalScrollingEnabled(true);
        setVerticalScrollingEnabled(false);
        this.ui = ui;
        this.lx = lx;
        setBackgroundColor(ui.theme.getPaneBackgroundColor());
        this.mixer = new UIMixer(ui, lx, PADDING, PADDING, HEIGHT-2*PADDING);
        this.mixer.addToContainer(this);

        for (LXChannel channel : lx.engine.getChannels()) {
            addChannel(channel);
        }
        addChannel(lx.engine.masterChannel);

        lx.engine.addListener(new LXEngine.Listener() {
            public void channelAdded(LXEngine engine, LXChannel channel) {
                addChannel(channel);
                onChannelFocus();
            }

            public void channelRemoved(LXEngine engine, LXChannel channel) {
                removeChannel(channel);
                onChannelFocus();
            }

            public void channelMoved(LXEngine engine, LXChannel channel) {
                onChannelFocus();
            }
        });

     lx.engine.focusedChannel.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                onChannelFocus();
            }
        });

        reflow();
        onChannelFocus();
    }

    @Override
    protected void onUIResize(UI ui) {
        onHelpBarToggle((LXStudio.UI) ui);
        setWidth(ui.getWidth());
        for (UIDeviceBin deviceBin : this.deviceBins.values()) {
            deviceBin.updateWidth();
        }
        redraw();
    }

    public void onHelpBarToggle(LXStudio.UI ui) {
        float yPos = ui.getHeight() - HEIGHT;
        if (ui.helpBar.isVisible()) {
            yPos -= UIContextualHelpBar.HEIGHT;
        }
        setY(yPos);
    }

    private void addChannel(LXBus channel) {
        UIDeviceBin deviceBin = new UIDeviceBin(ui, this, channel, this.mixer.getWidth() + SEPARATOR);
        this.deviceBins.put(channel, deviceBin);
        deviceBin.setVisible(false);
        deviceBin.addToContainer(this);
        onChannelsChanged();
    }

    private void removeChannel(LXBus channel) {
        this.deviceBins.remove(channel).removeFromContainer();
        onChannelsChanged();
    }

    private void onChannelsChanged() {
        // The channel mixer width is different now, collapse bins with extra width
        for (UIDeviceBin deviceBin : this.deviceBins.values()) {
            deviceBin.updateWidth();
        }
    }

    void onChannelFocus() {
        LXBus focusedChannel = lx.engine.getFocusedChannel();
        for (LXBus channel : this.deviceBins.keySet()) {
            UIDeviceBin deviceBin = this.deviceBins.get(channel);
            if (channel == focusedChannel) {
                deviceBin.setVisible(true);
                setScrollWidth(deviceBin.getX() + deviceBin.getWidth() + PADDING);
            } else {
                deviceBin.setVisible(false);
            }
        }
    }

    @Override
    public void reflow() {
        for (UIDeviceBin deviceBin : this.deviceBins.values()) {
            deviceBin.setX(this.mixer.getWidth() + SEPARATOR);
            if (deviceBin.isVisible()) {
                setScrollWidth(deviceBin.getX() + deviceBin.getWidth() + PADDING);
            }
        }
    }

    @Override
    public void onDraw(UI ui, PGraphics pg) {
        pg.stroke(ui.theme.getPrimaryColor());
        float channelX = PADDING + UIMixer.PADDING + UIMixer.STRIP_SPACING * lx.engine.focusedChannel.getValuei() + UIMixerStrip.WIDTH/2;
        float binX = this.mixer.getX() + this.mixer.getWidth() + SEPARATOR + 12;
        float b = 4;
        pg.strokeWeight(2);
        pg.line(channelX, this.height-PADDING, channelX, this.height-b-1);
        pg.line(binX, this.height-b-1, binX, this.height-PADDING);
        pg.line(channelX+1, this.height-b, binX-1, this.height-b);
        pg.strokeWeight(1);
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_N && (keyEvent.isControlDown() || keyEvent.isMetaDown())) {
            lx.engine.addChannel();
            lx.engine.focusedChannel.setValue(lx.engine.getChannels().size()-1);
        }
    }

}
