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
import heronarts.lx.LXChannel;
import heronarts.lx.LXEngine;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.studio.clip.UIClipLauncher;
import heronarts.p3lx.ui.studio.clip.UISceneLauncher;
import processing.core.PConstants;
import processing.core.PGraphics;

public class UIMixer extends UI2dContainer {

    public final static int PADDING = 6;
    private final static int CHILD_MARGIN = 1;
    public final static int STRIP_SPACING = UIMixerStripControls.WIDTH + CHILD_MARGIN;
    public final static int HEIGHT = UIMixerStrip.HEIGHT + 2*PADDING;

    private final Map<LXChannel, UIChannelStrip> internalChannelStrips = new HashMap<LXChannel, UIChannelStrip>();
    public final Map<LXChannel, UIChannelStrip> channelStrips = Collections.unmodifiableMap(this.internalChannelStrips);

    private final UIButton addChannelButton;
    public final UIMasterStrip masterStrip;
    public UISceneLauncher sceneLauncher;

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
            this.internalChannelStrips.put(channel, strip);
            strip.addToContainer(this);
        }

        this.addChannelButton = new UIButton(0, PADDING + UIClipLauncher.HEIGHT + UIMixerStrip.SPACING, 16, UIMixerStripControls.HEIGHT) {
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

        this.masterStrip = (UIMasterStrip) new UIMasterStrip(ui, this, lx).addToContainer(this);
        this.sceneLauncher = (UISceneLauncher) new UISceneLauncher(ui, this, lx, 0, PADDING).addToContainer(this);

        lx.engine.addListener(new LXEngine.Listener() {
            public void channelAdded(LXEngine engine, LXChannel channel) {
                UIChannelStrip strip = new UIChannelStrip(ui, UIMixer.this, lx, channel);
                internalChannelStrips.put(channel, strip);
                strip.addToContainer(UIMixer.this, channel.getIndex());
            }

            public void channelRemoved(LXEngine engine, LXChannel channel) {
                internalChannelStrips.remove(channel).removeFromContainer();
            }

            public void channelMoved(LXEngine engine, LXChannel channel) {
                internalChannelStrips.get(channel).setContainerIndex(channel.getIndex());
            }
        });
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        super.onDraw(ui, pg);
        pg.noStroke();
        pg.fill(0xff393939);
        pg.rect(
            this.width - PADDING - UISceneLauncher.WIDTH,
            this.height - PADDING - UIChannelStripControls.HEIGHT,
            UISceneLauncher.WIDTH,
            UIChannelStripControls.HEIGHT
        );

    }

}
