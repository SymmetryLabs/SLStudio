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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import heronarts.lx.*;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UI2dContext;
import heronarts.p3lx.ui.studio.device.UIDeviceBin;
import heronarts.p3lx.ui.studio.mixer.UIMixer;
import heronarts.p3lx.ui.studio.mixer.UIMixerStripControls;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;

public class UIBottomTray extends UI2dContext {

    public static final int PADDING = 8;
    // Device section is its own full-width row below the mixer so it never
    // gets cut off when many channels widen the mixer.
    public static final int DEVICE_SECTION_HEIGHT = UIDeviceBin.HEIGHT + 2*UIDeviceBin.PADDING;
    public static final int HEIGHT = UIMixer.HEIGHT + DEVICE_SECTION_HEIGHT + 3*PADDING;
    public static final int CLOSED_HEIGHT = UIMixerStripControls.HEIGHT + 2*UIMixer.PADDING + DEVICE_SECTION_HEIGHT + 3*PADDING;
    // Height of the tray when the device section is beside the mixer (original layout)
    public static final int SIDE_HEIGHT = UIMixer.HEIGHT + 2*PADDING;
    private static final int SEPARATOR = 16;
    private static final int TOGGLE_W = 14;

    private final UI ui;
    private final LX lx;
    public final UIMixer mixer;
    public final UI2dContainer rightSection;

    private final Map<LXBus, UIDeviceBin> mutableDeviceBins = new HashMap<LXBus, UIDeviceBin>();
    public final Map<LXBus, UIDeviceBin> deviceBins = Collections.unmodifiableMap(this.mutableDeviceBins);

    // When true, the device section is a full-width row below the mixer;
    // when false, it sits to the right of the mixer (original layout).
    private boolean deviceBelow = true;
    private final heronarts.p3lx.ui.component.UIButton layoutToggle;

    public UIBottomTray(UI ui, LX lx) {
        super(ui, 0, ui.getHeight() - HEIGHT - UIContextualHelpBar.VISIBLE_HEIGHT, ui.getWidth(), HEIGHT);
        this.ui = ui;
        this.lx = lx;
        setBackgroundColor(ui.theme.getPaneBackgroundColor());

        this.mixer = new UIMixer(ui, lx, PADDING, PADDING, UIMixer.HEIGHT);
        this.mixer.addToContainer(this);

        this.rightSection = (UI2dContainer)
            new UI2dContainer(PADDING + TOGGLE_W + 2, PADDING + UIMixer.HEIGHT + PADDING, getContentWidth() - 2*PADDING - TOGGLE_W - 2, DEVICE_SECTION_HEIGHT)
            .setBackgroundColor(ui.theme.getPaneInsetColor())
            .setBorderRounding(4)
            .addToContainer(this);

        // Small arrow button to the left of the device section: toggles between
        // below-the-mixer and beside-the-mixer layouts.
        this.layoutToggle = new heronarts.p3lx.ui.component.UIButton(PADDING, PADDING + UIMixer.HEIGHT + PADDING, TOGGLE_W, TOGGLE_W) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    toggleDeviceLayout();
                }
            }
        };
        this.layoutToggle
            .setLabel("^")
            .setMomentary(true)
            .setBorder(false)
            .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
            .setDescription("Toggle device section position: below or beside the mixer")
            .addToContainer(this);

        for (LXChannel channel : lx.engine.getChannels()) {
            addChannel(channel);
        }
        addChannel(lx.engine.masterChannel);

        lx.engine.getFocusedLook().addListener(new LXLook.Listener() {
            public void channelAdded(LXLook look, LXChannel channel) {
                addChannel(channel);
                onChannelFocus();
            }

            public void channelRemoved(LXLook look, LXChannel channel) {
                removeChannel(channel);
                onChannelFocus();
            }

            public void channelMoved(LXLook look, LXChannel channel) {
                onChannelFocus();
            }
        });

        lx.engine.getFocusedLook().focusedChannel.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                onChannelFocus();
            }
        });

        reflow();
        onChannelFocus();
    }

    private void addChannel(LXBus channel) {
        UIDeviceBin deviceBin = new UIDeviceBin(ui, channel, this.rightSection.getContentHeight() - UIDeviceBin.HEIGHT - UIDeviceBin.PADDING, this.rightSection.getContentWidth() - 2*UIDeviceBin.PADDING);
        this.mutableDeviceBins.put(channel, deviceBin);
        deviceBin.setVisible(false);
        deviceBin.addToContainer(this.rightSection);
    }

    private void removeChannel(LXBus channel) {
        this.mutableDeviceBins.remove(channel).removeFromContainer();
    }

    // Set from the engine thread (mouse event); processed on the render thread
    // in beginDraw via processPendingLayoutToggle(), because resizing UI2dContexts
    // off the Processing thread crashes the GL renderer.
    private volatile boolean pendingLayoutToggle = false;

    private void toggleDeviceLayout() {
        this.pendingLayoutToggle = true;
    }

    /** Called from UI.beginDraw() on the Processing thread. */
    public void processPendingLayoutToggle() {
        if (!this.pendingLayoutToggle) {
            return;
        }
        this.pendingLayoutToggle = false;
        this.deviceBelow = !this.deviceBelow;
        // Arrow points where the device section will go on next click
        this.layoutToggle.setLabel(this.deviceBelow ? "^" : "v");
        setHeight(this.deviceBelow ? HEIGHT : SIDE_HEIGHT);
        this.ui.reflow();
        redraw();
    }

    void onChannelFocus() {
        LXBus focusedChannel = lx.engine.getFocusedChannel();
        for (LXBus channel : this.mutableDeviceBins.keySet()) {
            UIDeviceBin deviceBin = this.mutableDeviceBins.get(channel);
            deviceBin.setVisible(channel == focusedChannel);
        }
        // For the green line at the bottom
        redraw();
    }

    @Override
    public void reflow() {
        if (this.rightSection == null) {
            return;
        }
        if (this.deviceBelow) {
            this.rightSection.setX(PADDING + TOGGLE_W + 2);
            this.rightSection.setY(PADDING + this.mixer.getHeight() + PADDING);
            this.rightSection.setWidth(Math.max(50, getContentWidth() - 2*PADDING - TOGGLE_W - 2));
            this.rightSection.setHeight(DEVICE_SECTION_HEIGHT);
        } else {
            float deviceX = this.mixer.getX() + this.mixer.getWidth() + SEPARATOR + TOGGLE_W + 2;
            this.rightSection.setX(deviceX);
            this.rightSection.setY(PADDING);
            this.rightSection.setWidth(Math.max(50, getContentWidth() - deviceX - PADDING));
            this.rightSection.setHeight(this.mixer.getHeight());
        }
        this.layoutToggle.setPosition(this.rightSection.getX() - TOGGLE_W - 2, this.rightSection.getY());
        for (UIDeviceBin deviceBin : this.mutableDeviceBins.values()) {
            deviceBin.setWidth(this.rightSection.getContentWidth() - 2*UIDeviceBin.PADDING);
            deviceBin.setY(this.rightSection.getContentHeight() - UIDeviceBin.HEIGHT - UIDeviceBin.PADDING);
        }
    }

    @Override
    public void onDraw(UI ui, PGraphics pg) {
        pg.stroke(ui.theme.getPrimaryColor());
        float channelX = PADDING + UIMixer.PADDING + UIMixer.STRIP_SPACING * lx.engine.getFocusedLook().focusedChannel.getValuei() + UIMixerStripControls.WIDTH/2;
        pg.strokeWeight(2);
        if (this.deviceBelow) {
            // Connector from the focused channel strip down to the device row below
            float binX = this.rightSection.getX() + 12;
            float mixerBottom = this.mixer.getY() + this.mixer.getHeight();
            float sectionTop = this.rightSection.getY();
            float yMid = (mixerBottom + sectionTop) / 2;
            pg.line(channelX, mixerBottom, channelX, yMid);
            pg.line(binX, yMid, binX, sectionTop);
            pg.line(Math.min(channelX, binX), yMid, Math.max(channelX, binX), yMid);
        } else {
            // Original connector along the bottom of the tray to the side section
            float binX = this.rightSection.getX() + 12;
            float b = 4;
            pg.line(channelX, this.height-PADDING, channelX, this.height-b-1);
            pg.line(binX, this.height-b-1, binX, this.height-PADDING);
            pg.line(Math.min(channelX, binX)+1, this.height-b, Math.max(channelX, binX)-1, this.height-b);
        }
        pg.strokeWeight(1);
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_N && (keyEvent.isControlDown() || keyEvent.isMetaDown())) {
            lx.engine.addChannel();
            lx.engine.getFocusedLook().focusedChannel.setValue(lx.engine.getChannels().size()-1);
        }
    }

}
