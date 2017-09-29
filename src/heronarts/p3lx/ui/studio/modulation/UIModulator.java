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

package heronarts.p3lx.ui.studio.modulation;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.LXMappingEngine;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.LXPeriodicModulator;
import heronarts.lx.modulator.LXTriggerSource;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.LXParameterModulation;
import heronarts.lx.parameter.LXTriggerModulation;
import heronarts.lx.parameter.LXCompoundModulation;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.UIKeyFocus;
import heronarts.p3lx.ui.UIModulationSource;
import heronarts.p3lx.ui.UIMouseFocus;
import heronarts.p3lx.ui.UIObject;
import heronarts.p3lx.ui.UITriggerTarget;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIColorBox;
import heronarts.p3lx.ui.component.UIComponentLabel;
import heronarts.p3lx.ui.component.UIImage;
import heronarts.p3lx.ui.component.UIParameterLabel;
import heronarts.p3lx.ui.component.UISlider;
import heronarts.p3lx.ui.component.UITextBox;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public abstract class UIModulator extends UI2dContainer implements UIMouseFocus, UIKeyFocus, UITriggerTarget {

    public interface Factory<T extends LXModulator> {
        UIModulator buildUI(UI ui, LX lx, T modulator, float x, float y, float width);
    }

    public static class DefaultFactory<T extends LXModulator> implements Factory<T> {

        private final Class<T> modulatorClass;
        private final Class<? extends UIModulator> uiClass;

        public DefaultFactory(Class<T> modulatorClass, Class<? extends UIModulator> uiClass) {
            this.modulatorClass = modulatorClass;
            this.uiClass = uiClass;
        }

        public UIModulator buildUI(UI ui, LX lx, T modulator, float x, float y, float width) {
            try {
                return
                    this.uiClass
                    .getConstructor(UI.class, LX.class, this.modulatorClass, Float.TYPE, Float.TYPE, Float.TYPE)
                    .newInstance(ui, lx, this.modulatorClass.cast(modulator), 0, 0, width);
            } catch (Exception ex) {
                System.err.println("Could not instantiate modulator UI for " + this.modulatorClass.getName() + ": " + ex.getLocalizedMessage());
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
    }

    protected static final int PADDING = 4;
    private static final int MODULATION_SPACING = 2;
    protected static final int TITLE_X = 18;
    protected static final int CONTENT_Y = 20;
    public static final int MAP_WIDTH = 24;
    public static final int TRIGGER_WIDTH = 16;
    protected static final int LOOP_WIDTH = 18;
    protected static final int COLOR_WIDTH = 10;

    private final UI ui;
    private final LX lx;
    public final LXComponent component;
    public final LXModulator modulator;
    protected final UI2dComponent toggleTarget;
    protected final UITextBox title;
    private final UI2dContainer content;
    protected final UI2dContainer modulations;

    private float expandedHeight;
    private boolean expanded = true;

    public UIModulator(final UI ui, final LX lx, final LXComponent component, boolean isModulator, float x, float y, float w, float h) {
        super(x, y, w, CONTENT_Y + h + PADDING);
        this.expandedHeight = this.height;
        this.ui = ui;
        this.lx = lx;
        this.component = component;
        this.modulator = isModulator ? (LXModulator) this.component : null;
        setBackgroundColor(ui.theme.getDeviceBackgroundColor());
        setBorderRounding(4);

        float titleX = TITLE_X;
        float titleRightX = this.width - 3*PADDING - COLOR_WIDTH - MAP_WIDTH;
        UIButton loopingButton = null;
        UIButton gateButton = null;

        if (this.modulator != null) {
            new UIButton(PADDING, PADDING, 12, 12)
            .setParameter(modulator.running)
            .setLabel("")
            .setBorderRounding(4)
            .addToContainer(this);

            if (this.modulator instanceof LXPeriodicModulator) {
                new UIButton(this.width - 4*PADDING - COLOR_WIDTH - MAP_WIDTH - LOOP_WIDTH - TRIGGER_WIDTH, PADDING, TRIGGER_WIDTH, 12)
                    .setIcon(ui.theme.iconTrigger)
                    .setParameter(this.modulator.trigger)
                    .setBorderRounding(4)
                    .addToContainer(this);

                loopingButton = new UIButton(this.width - 3*PADDING - COLOR_WIDTH - MAP_WIDTH - LOOP_WIDTH, PADDING, LOOP_WIDTH, 12)
                    .setIcon(ui.theme.iconLoop)
                    .setParameter(((LXPeriodicModulator) this.modulator).looping);

                titleRightX -= 2*PADDING + LOOP_WIDTH + TRIGGER_WIDTH;

            }

            if (this.modulator instanceof LXTriggerSource) {
                gateButton = new UITriggerModulationButton(ui, lx, ((LXTriggerSource) this.modulator).getTriggerSource(), titleRightX - TRIGGER_WIDTH, PADDING, TRIGGER_WIDTH, 12);
                titleRightX -= PADDING + TRIGGER_WIDTH;
            }

            this.title = new UITextBox(titleX, PADDING, titleRightX - titleX, 12);
            this.title
            .setParameter(modulator.label)
            .setBorder(false)
            .setBackground(false)
            .setFont(ui.theme.getLabelFont())
            .setFontColor(ui.theme.getControlTextColor())
            .setTextAlignment(PConstants.LEFT)
            .addToContainer(this);
            this.toggleTarget = this.title;
        } else {
            this.title = null;
            this.toggleTarget = new UIComponentLabel(PADDING, PADDING, titleRightX - PADDING, 12) {
                @Override
                public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
                    super.onMousePressed(mouseEvent, mx, my);
                    focus();
                }
            }
            .setComponent(this.component)
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .addToContainer(this);
        }

        if (loopingButton != null) {
            loopingButton.addToContainer(this);
        }
        if (gateButton != null) {
            gateButton.addToContainer(this);
        }

        if (isModulator) {
            final UIButton mapButton = (UIButton) new UIButton(this.width - 2*PADDING - MAP_WIDTH - COLOR_WIDTH, PADDING, MAP_WIDTH, 12) {
                @Override
                public void onToggle(boolean on) {
                    if (on) {
                        UIModulationSource modulationSource = getModulationSourceUI();
                        if (modulationSource != null) {
                            ui.mapModulationSource(modulationSource);
                        } else {
                            lx.engine.mapping.setMode(LXMappingEngine.Mode.MODULATION_SOURCE);
                        }
                    } else {
                        ui.mapModulationSource(null);
                    }
                }
            }
            .setIcon(ui.theme.iconMap)
            .setDescription("Map: select a new target for this modulation source")
            .addToContainer(this);

            lx.engine.mapping.mode.addListener(new LXParameterListener() {
                public void onParameterChanged(LXParameter p) {
                    if (lx.engine.mapping.getMode() == LXMappingEngine.Mode.OFF || lx.engine.mapping.getMode() == LXMappingEngine.Mode.MIDI) {
                        mapButton.setActive(false);
                    }
                }
            });
        }

        new UIColorBox(ui, this.component.modulationColor, this.width - PADDING - COLOR_WIDTH, PADDING + 1, COLOR_WIDTH, COLOR_WIDTH)
        .addToContainer(this);

        this.content = new UI2dContainer(PADDING, CONTENT_Y, width-2*PADDING, h) {
            @Override
            public void onResize() {
                recomputeHeight();
            }
        };
        setContentTarget(this.content);

        float modulationsY = CONTENT_Y + this.content.getHeight();
        if (this.content.getHeight() > 0) {
            modulationsY += PADDING;
        }
        this.modulations = new UI2dContainer(PADDING, modulationsY, width-2*PADDING, 0) {
            @Override
            public void onResize() {
                recomputeHeight();
            }
        };
        this.modulations
        .setArrowKeyFocus(UI2dContainer.ArrowKeyFocus.VERTICAL)
        .setLayout(UI2dContainer.Layout.VERTICAL)
        .setPadding(MODULATION_SPACING, 0, 0, 0)
        .setChildMargin(MODULATION_SPACING);
        addTopLevelComponent(this.modulations);
    }

    public String getIdentifier() {
        return String.format("%d", this.component.getId());
    }

    public UIModulator setExpanded(boolean expanded) {
        if (this.expanded != expanded) {
            this.expanded = expanded;
            this.content.setVisible(this.expanded);
            this.modulations.setVisible(this.expanded);
            setHeight(this.expanded ? this.expandedHeight : CONTENT_Y);
        }
        return this;
    }

    public UIModulator toggleExpanded() {
        return setExpanded(!this.expanded);
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    private void recomputeHeight() {
        float contentHeight = this.content.getHeight();
        float mappingHeight = this.modulations.getHeight();
        float padding = PADDING;
        if (mappingHeight > 0 && contentHeight > 0) {
            padding += PADDING;
        }
        UIModulator.this.setHeight(expandedHeight = CONTENT_Y + contentHeight + mappingHeight + padding);
    }

    public UIModulator addModulation(LXCompoundModulation modulation) {
        new UICompoundModulation(this.ui, modulation, 0, 0, this.modulations.getContentWidth()).addToContainer(this.modulations);
        return this;
    }

    public UIModulator _removeModulation(LXParameterModulation modulation) {
        int total = 0;
        for (UIObject child : this.modulations) {
            ++total;
            UIModulation uiModulation = (UIModulation) child;
            if (uiModulation.modulation == modulation) {
                uiModulation.removeFromContainer();
                --total;
            }
        }
        if ((this.modulator == null) && (total == 0)) {
            // We're a dead parameter!
            removeFromContainer();
        }
        return this;
    }

    public UIModulator removeModulation(LXCompoundModulation modulation) {
        return _removeModulation(modulation);
    }

    public UIModulator addTrigger(LXTriggerModulation trigger) {
        new UITriggerModulation(this.ui, trigger, 0, 0, this.modulations.getContentWidth()).addToContainer(this.modulations);
        return this;
    }

    public UIModulator removeTrigger(LXTriggerModulation trigger) {
        return _removeModulation(trigger);
    }

    public BooleanParameter getTriggerTarget() {
        if (this.modulator instanceof LXPeriodicModulator) {
            return this.modulator.trigger;
        }
        return null;
    }

    protected abstract UIModulationSource getModulationSourceUI();

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            consumeKeyEvent();
            onDelete();
        } else if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
            if (keyCode == java.awt.event.KeyEvent.VK_DOWN || keyCode == java.awt.event.KeyEvent.VK_UP) {
                consumeKeyEvent();
                setExpanded(!this.expanded);
            } else if (this.modulator != null && keyCode == java.awt.event.KeyEvent.VK_R) {
                consumeKeyEvent();
                this.title.focus();
                this.title.edit();
            } else if (keyCode == java.awt.event.KeyEvent.VK_D || keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                consumeKeyEvent();
                onDelete();
            }
        }
    }

    private void onDelete() {
        if (this.modulator != null) {
            this.lx.engine.modulation.removeModulator(this.modulator);
        } else {
            for (UIObject uiModulation : this.modulations) {
                if (uiModulation instanceof UICompoundModulation) {
                    this.lx.engine.modulation.removeModulation(((UICompoundModulation) uiModulation).modulation);
                } else if (uiModulation instanceof UITriggerModulation) {
                    this.lx.engine.modulation.removeTrigger(((UITriggerModulation) uiModulation).trigger);
                } else {
                    throw new IllegalStateException("Unknown child modulation type: " + uiModulation);
                }
            }
        }
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        if (mouseEvent.getCount() == 2 && this.toggleTarget.contains(mx, my)) {
            toggleExpanded();
        }
    }

    @Override
    public void drawFocus(UI ui, PGraphics pg) {}

    @Override
    public void onFocus() {
        setBackgroundColor(ui.theme.getDeviceFocusedBackgroundColor());
    }

    @Override
    public void onBlur() {
        setBackgroundColor(ui.theme.getDeviceBackgroundColor());
    }

    protected abstract class UIModulation extends UI2dContainer implements UIFocus {

        protected static final int PADDING = 4;

        protected final LXParameterModulation modulation;

        UIModulation(UI ui, final LXParameterModulation modulation, float x, float y, float w, float h) {
            super(x, y, w, h);
            this.modulation = modulation;
            setBackgroundColor(ui.theme.getDarkBackgroundColor());
        }

        protected abstract void remove();

        @Override
        public void drawFocus(UI ui, PGraphics pg) {
            pg.stroke(component.modulationColor.getColor());
            pg.line(0, 0, 0, this.height-1);
        }

        @Override
        public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
            if ((keyCode == java.awt.event.KeyEvent.VK_D && (keyEvent.isControlDown() || keyEvent.isMetaDown())) ||
                keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                consumeKeyEvent();
                remove();
            }
        }
    }

    private class UITriggerModulation extends UIModulation {

        private static final int HEIGHT = 12 + 2*PADDING;

        private final LXTriggerModulation trigger;

        UITriggerModulation(UI ui, final LXTriggerModulation trigger, float x, float y, float w) {
            super(ui, trigger, x, y, w, HEIGHT);
            this.trigger = trigger;
            new UIParameterLabel(PADDING + 10, PADDING, width - 2*PADDING - 10, 12)
            .setParameter(trigger.target)
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .addToContainer(this);
        }

        @Override
        protected void onDraw(UI ui, PGraphics pg) {
            pg.image(ui.theme.iconTriggerSource, 0, 3);
        }

        @Override
        protected void remove() {
            lx.engine.modulation.removeTrigger(this.trigger);
        }

    }

    protected class UICompoundModulation extends UIModulation {

        private static final int HEIGHT = 34 + 2*PADDING;
        private final String MAP_BLANK = "       ";
        protected final LXCompoundModulation modulation;

        UICompoundModulation(final UI ui, final LXCompoundModulation modulation, float x, float y, float w) {
            super(ui, modulation, x, y, w, HEIGHT);
            this.modulation = modulation;

            final UIParameterLabel label = (UIParameterLabel) new UIParameterLabel(PADDING, PADDING, width - 2*PADDING, 12)
            .setPrefix(MAP_BLANK)
            .setParameter(modulation.target)
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .addToContainer(this);

            final UIImage map = (UIImage) new UIImage(ui.theme.iconMap).setPosition(4, 3).addToContainer(this);

            if (modulation.source != component) {
                String sourceLabel = modulation.source.getLabel();
                ui.applet.g.textFont(ui.theme.getLabelFont());
                map.setX(ui.applet.g.textWidth(sourceLabel) + PADDING + 2);
                label.setPrefix(sourceLabel + MAP_BLANK);
                if (modulation.source instanceof LXComponent) {
                    ((LXComponent) modulation.source).label.addListener(new LXParameterListener() {
                        public void onParameterChanged(LXParameter p) {
                            String sourceLabel = modulation.source.getLabel();
                            ui.applet.g.textFont(ui.theme.getLabelFont());
                            map.setX(ui.applet.g.textWidth(sourceLabel) + PADDING + 2);
                            label.setPrefix(sourceLabel + MAP_BLANK);
                        }
                    });
                }
            }

            new UIButton(PADDING + 2, PADDING + 18, 24, 12).setParameter(modulation.polarity).addToContainer(this);
            final UISlider slider = (UISlider) new UISlider(2*PADDING + 26, PADDING + 16, width-3*PADDING - 26, 16)
            .setFillColor(modulation.clr.getColor())
            .setShowLabel(false)
            .setParameter(modulation.range)
            .addToContainer(this);

            modulation.clr.addListener(new LXParameterListener() {
                public void onParameterChanged(LXParameter p) {
                    slider.setFillColor(modulation.clr.getColor());
                    redraw();
                }
            });
        }

        @Override
        protected void remove() {
            lx.engine.modulation.removeModulation(this.modulation);
        }
    }
}