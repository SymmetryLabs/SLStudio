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
import heronarts.lx.LXMappingEngine;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.LXPeriodicModulator;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIModulationSource;
import heronarts.p3lx.ui.UIMouseFocus;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIColorBox;
import heronarts.p3lx.ui.component.UITextBox;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public abstract class UIModulator extends UI2dContainer implements UIMouseFocus {

    protected static final int PADDING = 4;
    protected static final int TITLE_X = 18;
    protected static final int CONTENT_Y = 20;
    protected static final int MAP_WIDTH = 24;
    protected static final int LOOP_WIDTH = MAP_WIDTH;
    protected static final int COLOR_WIDTH = 10;

    private final UI ui;
    private final LX lx;
    public final LXModulator modulator;
    protected final UITextBox title;
    private final UI2dContainer content;

    private float expandedHeight;
    private boolean expanded = true;

    public UIModulator(final UI ui, final LX lx, LXModulator modulator, float x, float y, float w, float h) {
        super(x, y, w, CONTENT_Y + h + PADDING);
        this.expandedHeight = this.height;
        this.ui = ui;
        this.lx = lx;
        this.modulator = modulator;
        setBackgroundColor(ui.theme.getWindowBackgroundColor());
        setBorderRounding(4);

        new UIButton(PADDING, PADDING, 12, 12)
        .setParameter(modulator.running)
        .setLabel("")
        .setBorderRounding(4)
        .addToContainer(this);

        float colorX = this.width - 2*PADDING - MAP_WIDTH - COLOR_WIDTH;

        UIButton loopingButton = null;
        if (modulator instanceof LXPeriodicModulator) {
            loopingButton = new UIButton(this.width - 2*(PADDING + MAP_WIDTH), PADDING-1, MAP_WIDTH, 14);
            colorX -= PADDING + LOOP_WIDTH;
        }

        this.title = new UITextBox(TITLE_X, PADDING, colorX - TITLE_X - PADDING, 12);
        this.title
        .setParameter(modulator.label)
        .setBorder(false)
        .setBackground(false)
        .setFont(ui.theme.getLabelFont())
        .setFontColor(ui.theme.getControlTextColor())
        .setTextAlignment(PConstants.LEFT)
        .addToContainer(this);

        new UIColorBox(ui, modulator.clr, colorX, PADDING+1, COLOR_WIDTH, COLOR_WIDTH)
        .addToContainer(this);

        if (loopingButton != null) {
            loopingButton
            .setLabel("\u21BA")
            .setParameter(((LXPeriodicModulator) modulator).looping)
            .addToContainer(this);
        }

        final UIButton mapButton = new UIButton(this.width - PADDING - MAP_WIDTH, PADDING-1, MAP_WIDTH, 14) {
            @Override
            public void onToggle(boolean on) {
                ui.mapModulationSource(on ? getModulationSourceUI() : null);
            }
        };
        mapButton
        .setLabel("→")
        .addToContainer(this);

        lx.engine.mapping.mode.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (lx.engine.mapping.getMode() == LXMappingEngine.Mode.OFF || lx.engine.mapping.getMode() == LXMappingEngine.Mode.MIDI) {
                    mapButton.setActive(false);
                }
            }
        });

        this.content = new UI2dContainer(PADDING, CONTENT_Y, width-2*PADDING, h) {
            @Override
            public void onResize() {
                UIModulator.this.setHeight(expandedHeight = CONTENT_Y + this.height + PADDING);
            }
        };
        setContentTarget(this.content);
    }

    protected abstract UIModulationSource getModulationSourceUI();

    private void toggleExpanded() {
        this.expanded = !this.expanded;
        this.content.setVisible(this.expanded);
        setHeight(this.expanded ? this.expandedHeight : CONTENT_Y);
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
            if (keyCode == java.awt.event.KeyEvent.VK_R) {
                consumeKeyEvent();
                this.title.focus();
                this.title.edit();
            } else if (keyCode == java.awt.event.KeyEvent.VK_D || keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                this.lx.engine.modulation.removeModulator(this.modulator);
            }
        }
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        if (mouseEvent.getCount() == 2 && this.title.contains(mx, my)) {
            toggleExpanded();
        }
    }

    @Override
    public void drawFocus(UI ui, PGraphics pg) {}

    @Override
    public void onFocus() {
        setBackgroundColor(ui.theme.getWindowFocusedBackgroundColor());
    }

    @Override
    public void onBlur() {
        setBackgroundColor(ui.theme.getWindowBackgroundColor());
    }
}
