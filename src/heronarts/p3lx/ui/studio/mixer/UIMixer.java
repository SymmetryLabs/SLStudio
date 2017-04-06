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

import java.util.HashMap;
import java.util.Map;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEngine;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import processing.core.PConstants;

public class UIMixer extends UI2dContainer {

    public final static int PADDING = 6;
    public final static int STRIP_SPACING = UIMixerStrip.WIDTH + UIMixerStrip.SPACING;
    private final static int ADD_CHANNEL_BUTTON_MARGIN = 1;

    private final Map<LXChannel, UIChannelStrip> channelStrips = new HashMap<LXChannel, UIChannelStrip>();
    private final UIMasterStrip masterStrip;
    private final UIButton addChannelButton;

    final LX lx;

    public UIMixer(final UI ui, final LX lx, float x, float y, float h) {
        super(x, y, 0, h);
        this.lx = lx;

        setBackgroundColor(ui.theme.getPaneInsetColor());
        setBorderRounding(4);

        int xp = UIMixerStrip.MARGIN;
        for (LXChannel channel : lx.engine.getChannels()) {
            UIChannelStrip strip = new UIChannelStrip(ui, lx, channel, xp);
            this.channelStrips.put(channel, strip);
            strip.addToContainer(this);
            xp += STRIP_SPACING;
        }

        this.addChannelButton = new UIButton(xp, UIMixerStrip.MARGIN, 16, this.height - 2*UIMixerStrip.MARGIN) {
            @Override
            public void onToggle(boolean on) {
                if (!on) {
                    lx.engine.addChannel();
                    lx.engine.focusedChannel.setValue(lx.engine.getChannels().size()-1);
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
        xp += this.addChannelButton.getWidth() + ADD_CHANNEL_BUTTON_MARGIN;

        this.masterStrip = new UIMasterStrip(ui, lx, xp);
        this.masterStrip.addToContainer(this);
        setWidth(xp + UIMixerStrip.WIDTH + UIMixerStrip.MARGIN);

        lx.engine.addListener(new LXEngine.Listener() {
            public void channelAdded(LXEngine engine, LXChannel channel) {
                UIChannelStrip strip = new UIChannelStrip(ui, lx, channel, width);
                channelStrips.put(channel, strip);
                strip.addToContainer(UIMixer.this, channel.getIndex());
                updateStripPositions();
                setWidth(width + STRIP_SPACING);
            }

            public void channelRemoved(LXEngine engine, LXChannel channel) {
                for (LXChannel c : channelStrips.keySet()) {
                    if (c.getIndex() >= channel.getIndex()) {
                        UIChannelStrip strip = channelStrips.get(c);
                        strip.setPosition(strip.getX() - STRIP_SPACING, strip.getY());
                    }
                }
                channelStrips.remove(channel).removeFromContainer();
                masterStrip.setPosition(masterStrip.getX() - STRIP_SPACING, masterStrip.getY());
                updateStripPositions();
                setWidth(width - STRIP_SPACING);
            }

            public void channelMoved(LXEngine engine, LXChannel channel) {
                for (LXChannel c : channelStrips.keySet()) {
                    UIChannelStrip strip = channelStrips.get(c);
                    strip.setPosition(UIMixerStrip.MARGIN + STRIP_SPACING * c.getIndex(), strip.getY());
                }
                channelStrips.get(channel).setContainerIndex(channel.getIndex());
            }
        });
    }

    void updateStripPositions() {
        int xp = UIMixerStrip.MARGIN;
        for (LXChannel channel : lx.engine.getChannels()) {
            this.channelStrips.get(channel).setX(xp);
            xp += STRIP_SPACING;
        }
        this.addChannelButton.setX(xp);
        xp += this.addChannelButton.getWidth() + ADD_CHANNEL_BUTTON_MARGIN;
        this.masterStrip.setX(xp);
    }

}