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

import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UI2dContext;
import heronarts.p3lx.ui.UI2dScrollContext;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.MouseEvent;

public abstract class UIPane extends UI2dContext {

    protected static final int MARGIN = 8;
    protected static final int PADDING = 6;
    private static final int PILL_HEIGHT = 16;
    private static final int INSET_Y = 24;
    private static final int CHILD_MARGIN = 6;

    protected final LX lx;
    private final UI2dContainer inset;
    private final String[] sectionNames;
    protected final UI2dScrollContext[] sections;
    private final int topOffset;
    private final int pillWidth;
    private int activeSection = 0;

    protected UIPane(UI ui, LX lx, String[] sectionNames, int x, int w) {
        this(ui, lx, sectionNames, x, w, MARGIN);
    }

    protected UIPane(UI ui, LX lx, String[] sectionNames, int x, int w, int topOffset) {
        super(ui, x, 0, w, ui.getHeight() - UIBottomTray.HEIGHT);
        this.lx = lx;
        this.sectionNames = sectionNames;
        this.topOffset = topOffset;
        setBackgroundColor(ui.theme.getPaneBackgroundColor());
        this.pillWidth = (w - (1+sectionNames.length)*UIPane.MARGIN) / sectionNames.length;

        this.inset = new UI2dContainer(MARGIN, this.topOffset + INSET_Y, width-2*MARGIN, height - (this.topOffset + INSET_Y));
        this.inset.setBorderRounding(4).setBackgroundColor(ui.theme.getPaneInsetColor()).addToContainer(this);

        this.sections = new UI2dScrollContext[sectionNames.length];
        for (int i = 0; i < sectionNames.length; ++i) {
            this.sections[i] = new UI2dScrollContext(ui, PADDING, PADDING, this.inset.getWidth() - 2*PADDING, this.inset.getHeight() - 2*PADDING) {
                @Override
                protected void reflow() {
                    super.reflow();
                }
            };
            this.sections[i].setVisible(i == 0);

            this.sections[i]
            .setLayout(UI2dContainer.Layout.VERTICAL)
            .setChildMargin(CHILD_MARGIN)
            .setBackgroundColor(ui.theme.getPaneInsetColor())
            .addToContainer(this.inset);
        }
    }

    @Override
    protected void onUIResize(UI ui) {
        setHeight(ui.getHeight() - UIBottomTray.HEIGHT);
        this.inset.setHeight(this.height - INSET_Y);
        for (UI2dScrollContext section : this.sections) {
            section.setHeight(this.inset.getHeight() - 2*PADDING);
        }
        redraw();
    }

    @Override
    public void onDraw(UI ui, PGraphics pg) {
        pg.noStroke();
        pg.textFont(ui.theme.getLabelFont());
        pg.textAlign(PConstants.CENTER, PConstants.TOP);
        for (int i = 0; i < this.sectionNames.length; ++i) {
            pg.fill(i == activeSection ? ui.theme.getWindowBackgroundColor() : 0xff333333);
            pg.rect(MARGIN + i*(MARGIN + this.pillWidth), this.topOffset, this.pillWidth, PILL_HEIGHT, 4);
            pg.fill(i == activeSection ? ui.theme.getControlTextColor() : ui.theme.getControlDisabledColor());
            pg.text(this.sectionNames[i], MARGIN + i*(MARGIN + this.pillWidth) + this.pillWidth/2, this.topOffset + 4);
        }

        // Active line
        pg.stroke(ui.theme.getWindowBackgroundColor());
        pg.strokeWeight(2);
        int lineX = MARGIN + this.activeSection * (MARGIN + this.pillWidth) + pillWidth/2;
        pg.line(lineX, this.topOffset + PILL_HEIGHT, lineX, this.inset.getY());
        pg.strokeWeight(1);
    }

    protected UIPane setActiveSection(int index) {
        if (this.activeSection != index) {
            this.activeSection = index;
            for (int i = 0; i < this.sections.length; ++i) {
                this.sections[i].setVisible(this.activeSection == i);
            }
            redraw();
        }
        return this;
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        super.onMousePressed(mouseEvent, mx, my);
        if (my >= this.topOffset && my < (this.topOffset + PILL_HEIGHT)) {
            float xp = mx - MARGIN;
            for (int i = 0; i < this.sectionNames.length; ++i) {
                if (xp >= 0 && xp < this.pillWidth) {
                    setActiveSection(i);
                    return;
                }
                xp -= this.pillWidth + MARGIN;
            }
        }
    }
}