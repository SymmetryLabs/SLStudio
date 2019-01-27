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

package heronarts.p3lx.ui.studio.lfo;

import heronarts.lx.LXLoopTask;
import heronarts.lx.modulator.VariableLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIModulationSource;
import heronarts.p3lx.ui.UITriggerTarget;
import processing.core.PGraphics;
import processing.event.MouseEvent;

public class UIWave extends UI2dComponent implements UIModulationSource, UITriggerTarget {

    private final VariableLFO lfo;
    private final PGraphics g;
    private int basisX = 0;

    public UIWave(final UI ui, final VariableLFO lfo, float x, float y, float w, float h) {
        super(x, y, w, h);
        setBackgroundColor(ui.theme.getDarkBackgroundColor());
        setBorderColor(ui.theme.getControlBorderColor());

        this.lfo = lfo;
        this.g = ui.applet.createGraphics((int) (w-2), (int) (h-2));
        drawWave(ui);

        LXParameterListener redrawWave = new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                drawWave(ui);
                redraw();
            }
        };
        lfo.waveshape.addListener(redrawWave);
        lfo.shape.addListener(redrawWave);
        lfo.skew.addListener(redrawWave);
        lfo.exp.addListener(redrawWave);
        lfo.phase.addListener(redrawWave);

        addLoopTask(new LXLoopTask() {
            @Override
            public void loop(double deltaMs) {
                int bX = (int) Math.round(1 + lfo.getBasisf() * (width-3.));
                if (bX != basisX) {
                    basisX = bX;
                    redraw();
                }
            }
        });
    }

    void drawWave(UI ui) {
        this.g.beginDraw();
        this.g.background(getBackgroundColor());
        this.g.stroke(ui.theme.getPrimaryColor());
        int py = 0;
        for (int x = 0; x < this.g.width; ++x) {
            int y = (int) Math.round((this.g.height-1.) * this.lfo.computeBase(x / (this.g.width-1.)));
            if (x > 0) {
                this.g.line(x-1, this.g.height-1-py, x, this.g.height-1-y);
            }
            py = y;
        }
        this.g.endDraw();
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        pg.image(this.g, 1, 1);
        pg.stroke(ui.theme.getCursorColor());
        pg.line(this.basisX, 1, this.basisX, this.height-2);
    }

    private boolean editing = false;

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        this.editing = Math.abs(mx - this.basisX) < 5;
    }

    @Override
    public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
        if (this.editing) {
            this.lfo.setBasis((mx-1) / (this.width-2));
        }
    }

    public LXNormalizedParameter getModulationSource() {
        return this.lfo;
    }

    @Override
    public BooleanParameter getTriggerTarget() {
        return this.lfo.trigger;
    }
}
