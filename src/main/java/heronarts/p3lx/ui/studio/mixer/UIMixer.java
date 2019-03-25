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

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEngine;
import heronarts.lx.LXMasterChannel;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import processing.core.PConstants;

public class UIMixer extends UI2dContainer {

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

        lx.engine.addListener(new LXEngine.Listener() {
            public void channelAdded(LXEngine engine, LXChannel channel) {
                UIChannelStrip strip = new UIChannelStrip(ui, UIMixer.this, lx, channel);
                mutableChannelStrips.put(channel, strip);
                strip.addToContainer(UIMixer.this, channel.getIndex());
            }

            public void channelRemoved(LXEngine engine, LXChannel channel) {
                mutableChannelStrips.remove(channel).removeFromContainer();
            }

            public void channelMoved(LXEngine engine, LXChannel channel) {
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
