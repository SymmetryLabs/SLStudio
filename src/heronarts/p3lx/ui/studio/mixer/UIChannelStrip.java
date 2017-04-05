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

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIButtonGroup;
import heronarts.p3lx.ui.component.UIDropMenu;
import heronarts.p3lx.ui.component.UISlider;
import heronarts.p3lx.ui.component.UITextBox;
import processing.core.PConstants;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIChannelStrip extends UIMixerStrip {

    private static final int TITLE_MARGIN = 4;

    private final LXChannel channel;
    private final UITextBox channelName;
    private final UIButton activeButton;
    private final UISlider fader;

    UIChannelStrip(final UI ui, final LX lx, final LXChannel channel, float x) {
        super(ui, lx, channel, x);
        this.channel = channel;

        this.channelName = new UITextBox(TITLE_MARGIN, TITLE_MARGIN-1, this.width-2*TITLE_MARGIN, 14);
        this.channelName
        .setParameter(channel.label)
        .setBorder(false)
        .setBackground(false)
        .setFont(ui.theme.getLabelFont())
        .setFontColor(ui.theme.getControlTextColor())
        .addToContainer(this);

        float yp = 40;

        // Active + Midi buttons
        float bxp = 6*PADDING;
        this.activeButton = new UIButton(bxp, yp, 28, 28);
        activeButton
            .setLabel(Integer.toString(channel.getIndex() + 1))
            .setParameter(channel.enabled)
            .setFont(ui.loadFont("Arial-Black-11.vlw"))
            .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
            .setTextOffset(0, 2);
        yp += 28 + MARGIN;
        final UIButton midiButton =
            new UIButton(bxp, yp, 28, 14)
            .setLabel("♪")
            .setParameter(channel.midiMonitor);
        yp += 16;
        final UIButton cueButton =
            new UIButton(bxp, yp, 28, 16) {
                @Override
                public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
                    super.onMousePressed(mouseEvent, mx, my);
                    if (channel.cueActive.isOn()) {
                        if (!mouseEvent.isShiftDown() && !mouseEvent.isMetaDown()) {
                            for (LXChannel c : lx.engine.getChannels()) {
                                if (channel != c) {
                                    c.cueActive.setValue(false);
                                }
                            }
                        }
                    }
                }
            }.setLabel("CUE")
            .setActiveColor(ui.theme.getAttentionColor())
            .setParameter(channel.cueActive);

        // Crossfade select
        int xfh = 16;
        int xfo = 2*PADDING;

        UIButtonGroup crossfadeGroup = new UIButtonGroup(channel.crossfadeGroup, xfo, height-MARGIN-xfh, width-6, xfh, true);
        crossfadeGroup.buttons[LXChannel.CrossfadeGroup.B.ordinal()].setActiveColor(ui.theme.getSecondaryColor());
        crossfadeGroup.setChildMargin(2);

        // Slider
        float syp = 22;
        this.fader = new UISlider(UISlider.Direction.VERTICAL, this.width-PADDING-FADER_WIDTH, syp, FADER_WIDTH, 102);
        this.fader.setParameter(channel.fader).setShowLabel(false);
        if (!channel.enabled.isOn()) {
            fader.setFillColor(ui.theme.getControlDisabledColor());
        }

        // Blend mode
        final UIDropMenu blendMode = new UIDropMenu(4, height-40, width-8, 16, channel.blendMode);
        blendMode.setDirection(UIDropMenu.Direction.UP);

        // Add them all
        activeButton.addToContainer(this);
        midiButton.addToContainer(this);
        cueButton.addToContainer(this);
        fader.addToContainer(this);
        blendMode.addToContainer(this);
        crossfadeGroup.addToContainer(this);

        channel.enabled.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (channel.enabled.isOn()) {
                    fader.resetFillColor();
                } else {
                    fader.setFillColor(ui.theme.getControlDisabledColor());
                }
            }
        });

        channel.addListener(new LXChannel.Listener() {
            public void indexChanged(LXChannel channel) {
                activeButton.setLabel(Integer.toString(channel.getIndex() + 1));
            }
            public void effectAdded(LXBus bus, LXEffect effect) {}
            public void effectRemoved(LXBus bus, LXEffect effect) {}
            public void effectMoved(LXBus bus, LXEffect effect) {}
            public void patternAdded(LXChannel channel, LXPattern pattern) {}
            public void patternRemoved(LXChannel channel, LXPattern pattern) {}
            public void patternWillChange(LXChannel channel, LXPattern pattern, LXPattern nextPattern) {}
            public void patternDidChange(LXChannel channel, LXPattern pattern) {}
        });
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
            if (keyCode == java.awt.event.KeyEvent.VK_R) {
                consumeKeyEvent();
                channelName.focus();
                channelName.edit();
            } else if (keyCode == java.awt.event.KeyEvent.VK_D || keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                consumeKeyEvent();
                if (lx.engine.getChannels().size() > 1) {
                    lx.engine.removeChannel(this.channel);
                }
            } else if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
                consumeKeyEvent();
                if (channel.getIndex() > 0) {
                    lx.engine.moveChannel(channel, channel.getIndex() - 1);
                }
            } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
                consumeKeyEvent();
                if (channel.getIndex() < lx.engine.getChannels().size() - 1) {
                    lx.engine.moveChannel(channel, channel.getIndex() + 1);
                }
            }
        } else {
            super.onKeyPressed(keyEvent, keyChar, keyCode);
        }
    }
}
