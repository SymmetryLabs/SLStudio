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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import heronarts.lx.*;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import processing.core.PConstants;

public class UIMixer extends UI2dContainer {

    private final UI ui;

    private final java.util.LinkedHashSet<LXChannel> selectedChannels = new java.util.LinkedHashSet<>();

    public boolean isChannelSelected(LXChannel channel) {
        return selectedChannels.contains(channel);
    }

    public void toggleChannelSelection(LXChannel channel, boolean shift) {
        if (!shift) {
            selectedChannels.clear();
        }
        if (selectedChannels.contains(channel)) {
            selectedChannels.remove(channel);
        } else {
            selectedChannels.add(channel);
        }
        System.out.print("UIMixer.toggleChannelSelection: Selected channels: [");
        for (LXChannel ch : selectedChannels) {
            System.out.print(ch.getLabel() + ", ");
        }
        System.out.println("]");
    }

    public void clearChannelSelection() {
        selectedChannels.clear();
    }

    @Override
    public void onKeyPressed(processing.event.KeyEvent keyEvent, char keyChar, int keyCode) {
        // CMD+G (Meta+G or Ctrl+G) for grouping
        if ((keyEvent.isMetaDown() || keyEvent.isControlDown()) && (keyCode == java.awt.event.KeyEvent.VK_G)) {
            if (selectedChannels.size() > 1) {
                // Collect selected channels
                java.util.List<LXChannel> groupChannels = new java.util.ArrayList<>(selectedChannels);

                // Create new look
                LXLook newLook = lx.engine.addLook();
                // Remove channels from current look and add to new look
                LXLook currentLook = lx.engine.getFocusedLook();
                for (LXChannel channel : groupChannels) {
                    currentLook.removeChannel(channel);
                    newLook.addChannel(); // TODO: Copy properties from old channel if needed
                }
                // Update UI: remove channel strips and add UILookStrip
                for (LXChannel channel : groupChannels) {
                    UIChannelStrip strip = mutableChannelStrips.remove(channel);
                    if (strip != null) {
                        strip.removeFromContainer();
                    }
                }
                UILookStrip lookStrip = new UILookStrip(this.ui, this, this.lx, newLook, groupChannels);
                this.addTopLevelComponent(lookStrip);
                // Clear selection
                selectedChannels.clear();
                redraw();
            }
        } else {
            super.onKeyPressed(keyEvent, keyChar, keyCode);
        }
    }

    // TODO: Add key event handler for CMD+G using the correct event system for your UI framework.
    // See your UI framework's documentation for keyboard shortcut handling.


    public final static int PADDING = 6;
    private final static int CHILD_MARGIN = 1;
    public final static int STRIP_SPACING = UIMixerStripControls.WIDTH + CHILD_MARGIN;
    public final static int HEIGHT = UIMixerStrip.HEIGHT + 2*PADDING;

    private final Map<LXChannel, UIChannelStrip> mutableChannelStrips = new HashMap<LXChannel, UIChannelStrip>();
    public final Map<LXChannel, UIChannelStrip> channelStrips = Collections.unmodifiableMap(this.mutableChannelStrips);

    public final UIButton addChannelButton;
    public final UIMasterStrip masterStrip;

    final LX lx;

    public UIMixer(final UI ui, final LX lx, float x, float y, float h) {
        super(x, y, 0, h);
        this.ui = ui;
        this.lx = lx;

        setBackgroundColor(ui.theme.getPaneInsetColor());
        setBorderRounding(4);
        setLayout(UI2dContainer.Layout.HORIZONTAL);
        setChildMargin(CHILD_MARGIN);
        setPadding(0, PADDING, 0, PADDING);

        for (LXChannel channel : lx.engine.getChannels()) {
            UIChannelStrip strip = new UIChannelStrip(ui, this, lx, channel);
            this.mutableChannelStrips.put(channel, strip);
            strip.addToContainer(this);
        }

        this.addChannelButton = new UIButton(0, PADDING + UIMixerStrip.SPACING, 20, UIMixerStripControls.HEIGHT) {
            @Override
            public void onToggle(boolean on) {
                if (!on) {
                    lx.engine.addChannel();
                    lx.engine.getFocusedLook().focusedChannel.setValue(lx.engine.getChannels().size()-1);
                }
            }
        };
        this.addChannelButton
        .setLabel("+")
        .setMomentary(true)
        .setInactiveColor(0xff393939) // TODO(mcslee): control disabled color?
        .setBorder(false)
        .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
        .setDescription("New Channel: add another channel")
        .addToContainer(this);

        this.masterStrip = (UIMasterStrip) new UIMasterStrip(ui, this, lx).addToContainer(this);

        lx.engine.getFocusedLook().addListener(new LXLook.Listener() {
            public void channelAdded(LXLook look, LXChannel channel) {
                UIChannelStrip strip = new UIChannelStrip(ui, UIMixer.this, lx, channel);
                mutableChannelStrips.put(channel, strip);
                strip.addToContainer(UIMixer.this, channel.getIndex());
            }

            public void channelRemoved(LXLook look, LXChannel channel) {
                mutableChannelStrips.remove(channel).removeFromContainer();
            }

            public void channelMoved(LXLook look, LXChannel channel) {
                mutableChannelStrips.get(channel).setContainerIndex(channel.getIndex());
            }
        });
    }

    void focusStrip(LXBus bus) {
        if (bus instanceof LXMasterChannel) {
            this.masterStrip.controls.focus();
        } else {
            ((UIChannelStripControls) this.mutableChannelStrips.get(bus).controls).channelName.focus();
        }
    }

}
