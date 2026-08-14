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
 */

package heronarts.p3lx.ui.studio.device;

import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;
import heronarts.lx.LXPatternBank;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UITimerTask;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIDoubleBox;
import heronarts.p3lx.ui.component.UIDropMenu;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.component.UILabel;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * UI component for displaying a single pattern bank within a channel.
 * Shows the bank's patterns in a vertical list with controls for transitions and auto-cycle.
 */
public class UIPatternBank extends UI2dContainer {

    private static final int BANK_WIDTH = 140;
    private static final int HEADER_HEIGHT = 16;
    private static final int FOOTER_HEIGHT = 36;
    
    private final UI ui;
    private final LXChannel channel;
    private final LXPatternBank bank;
    private final UILabel headerLabel;
    private final UIBankPatternList patternList;
    private boolean selected = false;
    private Runnable selectListener = null;

    public UIPatternBank(UI ui, LXChannel channel, LXPatternBank bank, float x, float y, float h) {
        super(x, y, BANK_WIDTH, h);
        this.ui = ui;
        this.channel = channel;
        this.bank = bank;
        setBorderColor(ui.theme.getDeviceBorderColor());

        // Bank header/title
        this.headerLabel = new UILabel(0, 0, BANK_WIDTH, HEADER_HEIGHT);
        this.headerLabel.setLabel(bank.getLabel());
        this.headerLabel.setTextAlignment(PConstants.CENTER, PConstants.CENTER);
        this.headerLabel.addToContainer(this);

        updateSelectedStyle();

        // Pattern list
        this.patternList = new UIBankPatternList(ui, 0, HEADER_HEIGHT, BANK_WIDTH, h - HEADER_HEIGHT - FOOTER_HEIGHT, bank);
        this.patternList.addToContainer(this);

        // Transition controls
        new UIButton(0, h - 36, 16, 16)
            .setLabel("\u21C4")
            .setParameter(bank.transitionEnabled)
            .setTextOffset(0, -1)
            .addToContainer(this);
        new UIDropMenu(18, h - 36, 80, 16, bank.transitionBlendMode)
            .setDirection(UIDropMenu.Direction.UP)
            .addToContainer(this);
        new UITransitionBox(bank, 100, h - 36, 40, 16)
            .setParameter(bank.transitionTimeSecs)
            .setShiftMultiplier(.1f)
            .addToContainer(this);

        // Auto cycle controls
        new UIButton(0, h - 16, 16, 16)
            .setLabel("\u21BA")
            .setParameter(bank.autoCycleEnabled)
            .addToContainer(this);
        new UIAutoCycleBox(bank, 18, h - 16, 122, 16)
            .setParameter(bank.autoCycleTimeSecs)
            .setShiftMultiplier(60)
            .addToContainer(this);

        // Listen for label changes
        bank.label.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                headerLabel.setLabel(bank.getLabel());
            }
        });
    }

    public LXPatternBank getBank() {
        return this.bank;
    }

    public UIPatternBank setSelectListener(Runnable selectListener) {
        this.selectListener = selectListener;
        return this;
    }

    @Override
    public void onMousePressed(processing.event.MouseEvent mouseEvent, float mx, float my) {
        super.onMousePressed(mouseEvent, mx, my);
        if (my < HEADER_HEIGHT && this.selectListener != null) {
            this.selectListener.run();
        }
    }

    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            updateSelectedStyle();
            redraw();
        }
    }

    private void updateSelectedStyle() {
        if (this.selected) {
            setBackgroundColor(this.ui.theme.getDeviceFocusedBackgroundColor());
            this.headerLabel
                .setBackgroundColor(this.ui.theme.getPrimaryColor())
                .setFontColor(UI.BLACK);
        } else {
            setBackgroundColor(this.ui.theme.getDeviceBackgroundColor());
            this.headerLabel
                .setBackgroundColor(this.ui.theme.getDarkBackgroundColor())
                .setFontColor(this.ui.theme.getLabelColor());
        }
    }

    public boolean isSelected() {
        return this.selected;
    }

    @Override
    protected void endDraw(UI ui, PGraphics pg) {
        super.endDraw(ui, pg);
        // Draw selection border if selected (after children are drawn)
        if (this.selected) {
            pg.noFill();
            pg.stroke(ui.theme.getPrimaryColor());
            pg.strokeWeight(3);
            pg.rect(1, 1, this.width - 3, this.height - 3);
            pg.strokeWeight(1);
        }
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        super.onKeyPressed(keyEvent, keyChar, keyCode);
        if (!keyEventConsumed()) {
            if (keyCode == java.awt.event.KeyEvent.VK_UP || keyCode == java.awt.event.KeyEvent.VK_DOWN) {
                this.patternList.onKeyPressed(keyEvent, keyChar, keyCode);
            }
        }
    }

    abstract class UIProgressBox extends UIDoubleBox {
        protected final LXPatternBank bank;
        protected int progress = 0;

        abstract protected boolean hasProgress();
        abstract protected double getProgress();

        UIProgressBox(final LXPatternBank bank, float x, float y, float w, float h) {
            super(x, y, w, h);
            this.bank = bank;
            addLoopTask(new UITimerTask(30, UITimerTask.Mode.FPS) {
                @Override
                public void run() {
                    if (hasProgress()) {
                        int newProgress = (int) (getProgress() * (width-5));
                        if (newProgress != progress) {
                            progress = newProgress;
                            redraw();
                        }
                    } else {
                        if (progress != 0) {
                            progress = 0;
                            redraw();
                        }
                    }
                }
            });
        }

        @Override
        public void onDraw(UI ui, PGraphics pg) {
            if (progress > 0) {
                pg.noFill();
                pg.stroke(ui.theme.getPrimaryColor());
                pg.line(2, height-2, 2 + progress, height-2);
            }
            super.onDraw(ui, pg);
        }
    }

    class UITransitionBox extends UIProgressBox {
        UITransitionBox(LXPatternBank bank, float x, float y, float w, float h) {
            super(bank, x, y, w, h);
        }

        @Override
        protected boolean hasProgress() {
            return this.bank.transitionEnabled.isOn();
        }

        @Override
        protected double getProgress() {
            return this.bank.getTransitionProgress();
        }
    }

    class UIAutoCycleBox extends UIProgressBox {
        UIAutoCycleBox(LXPatternBank bank, float x, float y, float w, float h) {
            super(bank, x, y, w, h);
        }

        @Override
        protected boolean hasProgress() {
            return this.bank.autoCycleEnabled.isOn();
        }

        @Override
        protected double getProgress() {
            return this.bank.getAutoCycleProgress();
        }
    }

    /**
     * Pattern list specific to a bank
     */
    class UIBankPatternList extends UIItemList.ScrollList {

        private final LXPatternBank bank;
        final Map<LXPattern, BankPatternItem> patternToItem = new HashMap<>();

        public UIBankPatternList(UI ui, float x, float y, float w, float h, final LXPatternBank bank) {
            super(ui, x, y, w, h);
            setRenamable(true);
            setReorderable(true);
            setShowCheckboxes(true);

            this.bank = bank;
            for (LXPattern pattern : bank.patterns) {
                addPattern(pattern);
            }

            LXPatternBank.Listener bankListener = new LXPatternBank.AbstractListener() {

                @Override
                public void patternAdded(LXPatternBank bank, LXPattern pattern) {
                    addPattern(pattern);
                }

                @Override
                public void patternRemoved(LXPatternBank bank, LXPattern pattern) {
                    removePattern(pattern);
                }

                @Override
                public void patternMoved(LXPatternBank bank, LXPattern pattern) {
                    // TODO(mcslee): should we handle? right now only happens from within the UI
                }

                @Override
                public void patternWillChange(LXPatternBank bank, LXPattern pattern, LXPattern nextPattern) {
                    redraw();
                }

                @Override
                public void patternDidChange(LXPatternBank bank, LXPattern pattern) {
                    redraw();
                }
            };

            bank.addListener(bankListener);

            bank.focusedPattern.addListener(new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter parameter) {
                    setFocusIndex(bank.focusedPattern.getValuei());
                }
            });

            bankListener.patternDidChange(bank, bank.getActivePattern());
        }

        private void addPattern(LXPattern pattern) {
            BankPatternItem item = new BankPatternItem(pattern);
            this.patternToItem.put(pattern, item);
            addItem(item);
        }

        private void removePattern(LXPattern pattern) {
            BankPatternItem patternItem = this.patternToItem.remove(pattern);
            if (patternItem == null) {
                throw new IllegalStateException("Pattern removed from bank not found in map: " + pattern);
            }
            removeItem(patternItem);
        }

        class BankPatternItem implements UIItemList.Item {
            private final LXPattern pattern;

            BankPatternItem(LXPattern pattern) {
                this.pattern = pattern;
                pattern.label.addListener(new LXParameterListener() {
                    public void onParameterChanged(LXParameter p) {
                        redraw();
                    }
                });
                pattern.autoCycleEligible.addListener(new LXParameterListener() {
                    public void onParameterChanged(LXParameter p) {
                        redraw();
                    }
                });
            }

            @Override
            public boolean isActive() {
                return
                    (bank.getActivePattern() == this.pattern) ||
                    (bank.getNextPattern() == this.pattern);
            }

            @Override
            public boolean isChecked() {
                return this.pattern.autoCycleEligible.isOn();
            }

            @Override
            public int getActiveColor(UI ui) {
                return (bank.getActivePattern() == this.pattern) ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
            }

            @Override
            public String getLabel() {
                String groupName = LXPattern.getGroupName(pattern.getClass());
                if (groupName != null)
                    return String.format("%s / %s", groupName, pattern.getLabel());
                return pattern.getLabel();
            }

            @Override
            public void onActivate() {
                bank.goPattern(this.pattern);
            }

            @Override
            public void onRename(String label) {
                this.pattern.label.setValue(label);
            }

            @Override
            public void onReorder(int index) {
                bank.movePattern(this.pattern, index);
            }

            @Override
            public void onCheck(boolean on) {
                this.pattern.autoCycleEligible.setValue(on);
            }

            @Override
            public void onDelete() {
                if (bank.patterns.size() > 1) {
                    bank.removePattern(this.pattern);
                }
            }

            @Override
            public void onDeactivate() {
            }

            @Override
            public void onFocus() {
                bank.focusedPattern.setValue(this.pattern.getIndex());
            }
        }
    }
}
