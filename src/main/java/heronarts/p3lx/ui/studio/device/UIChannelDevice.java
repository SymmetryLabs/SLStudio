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

import heronarts.lx.LXChannel;
import heronarts.lx.LXPatternBank;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UITimerTask;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIDoubleBox;
import heronarts.p3lx.ui.component.UIDropMenu;
import heronarts.p3lx.ui.component.UISlider;
import heronarts.p3lx.ui.studio.PatternScope;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;

class UIChannelDevice extends UIDevice {

    private static final int BANK_WIDTH = 140;
    private static final int ADD_BUTTON_WIDTH = 16;
    
    private final UI ui;
    private final LXChannel channel;
    private final UI2dContainer banksContainer;
    private final List<UIPatternBank> bankComponents = new ArrayList<>();
    private UIPatternBank selectedBank = null;

    UIChannelDevice(UI ui, UIDeviceBin deviceBin, final LXChannel channel) {
        super(ui, channel, BANK_WIDTH);
        this.ui = ui;
        this.channel = channel;
        setTitle(channel.label);

        // Scope toggle button
        final UIButton scopeButton = new UIButton(0, 0, BANK_WIDTH - ADD_BUTTON_WIDTH - 2, 16) {
            @Override
            public void onMousePressed(processing.event.MouseEvent mouseEvent, float mx, float my) {
                int numBanks = channel.getBanks().size();
                PatternScope.cycleScope(numBanks);
                setLabel(PatternScope.getScopeLabel());
            }
        };
        scopeButton
            .setMomentary(true)
            .setLabel(PatternScope.getScopeLabel())
            .setDescription("Cycle effect/warp scope: Channel → Pattern → Bank1 → Bank2 → ...")
            .addToContainer(this);

        // Add bank button (+)
        new UIButton(BANK_WIDTH - ADD_BUTTON_WIDTH, 0, ADD_BUTTON_WIDTH, 16) {
            @Override
            public void onMousePressed(processing.event.MouseEvent mouseEvent, float mx, float my) {
                channel.addBank();
            }
        }
        .setLabel("+")
        .setMomentary(true)
        .setDescription("Add a new pattern bank")
        .addToContainer(this);

        new UISlider(0, 18, BANK_WIDTH, 16)
        .setParameter(channel.speed)
        .setShowLabel(false)
        .addToContainer(this);

        // Container for banks (scrollable horizontally)
        this.banksContainer = new UI2dContainer(0, 34, BANK_WIDTH, getContentHeight() - 34);
        this.banksContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
        this.banksContainer.setPadding(0);
        this.banksContainer.addToContainer(this);

        // Initialize banks
        for (LXPatternBank bank : channel.getBanks()) {
            addBankUI(bank);
        }

        // Listen for bank changes
        channel.addListener(new LXChannel.AbstractListener() {
            @Override
            public void bankAdded(LXChannel channel, LXPatternBank bank) {
                addBankUI(bank);
                updateWidth();
            }

            @Override
            public void bankRemoved(LXChannel channel, LXPatternBank bank) {
                removeBankUI(bank);
                updateWidth();
            }
        });

        updateWidth();
    }

    private void addBankUI(LXPatternBank bank) {
        float x = this.bankComponents.size() * BANK_WIDTH;
        final UIPatternBank bankUI = new UIPatternBank(this.ui, channel, bank, x, 0, this.banksContainer.getHeight());
        bankUI.setSelectListener(() -> selectBank(bankUI));
        this.bankComponents.add(bankUI);
        bankUI.addToContainer(this.banksContainer);
        if (this.selectedBank == null) {
            selectBank(bankUI);
        }
    }

    private void removeBankUI(LXPatternBank bank) {
        UIPatternBank toRemove = null;
        for (UIPatternBank bankUI : this.bankComponents) {
            if (bankUI.getBank() == bank) {
                toRemove = bankUI;
                break;
            }
        }
        if (toRemove != null) {
            this.bankComponents.remove(toRemove);
            toRemove.removeFromContainer();
            if (selectedBank == toRemove) {
                selectedBank = null;
            }
            // Reposition remaining banks
            for (int i = 0; i < this.bankComponents.size(); i++) {
                this.bankComponents.get(i).setX(i * BANK_WIDTH);
            }
            if (this.selectedBank == null && !this.bankComponents.isEmpty()) {
                selectBank(this.bankComponents.get(0));
            }
        }
    }

    private void updateWidth() {
        float contentWidth = Math.max(1, this.bankComponents.size()) * BANK_WIDTH;
        setContentWidth(contentWidth);
        setWidth(contentWidth + 2*PADDING + DEVICE_BAR_WIDTH);
        this.banksContainer.setWidth(contentWidth);
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        super.onKeyPressed(keyEvent, keyChar, keyCode);
        if (!keyEventConsumed()) {
            // Delete/Backspace removes selected bank
            if ((keyCode == java.awt.event.KeyEvent.VK_DELETE || keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) && selectedBank != null) {
                channel.removeBank(selectedBank.getBank());
                consumeKeyEvent();
            }
        }
    }

    private void selectBank(UIPatternBank bank) {
        if (selectedBank != null) {
            selectedBank.setSelected(false);
        }
        selectedBank = bank;
        if (selectedBank != null) {
            selectedBank.setSelected(true);
            // Update focused bank parameter
            int index = this.bankComponents.indexOf(selectedBank);
            if (index >= 0) {
                channel.focusedBank.setValue(index);
            }
        }
    }

}
