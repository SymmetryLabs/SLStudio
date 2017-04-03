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

import heronarts.lx.LXPattern;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIKnob;

public class UIPatternControl extends UI2dContainer {

    private static final int PADDING = 4;

    public final LXPattern pattern;
    private final UI2dContainer content;

    public UIPatternControl(UI ui, LXPattern pattern, float x, float y, float h) {
        super(x, y, 2*PADDING, h);
        this.pattern = pattern;
        setBackgroundColor(0xff303030);
        setBorderRounding(4);

        this.content = new UI2dContainer(PADDING, PADDING, 0, this.height-2*PADDING) {
            @Override
            public void onResize() {
                UIPatternControl.this.setWidth(getWidth() + 2*PADDING);
            }
        };
        setContentTarget(this.content);

        if (pattern instanceof UIPattern) {
            ((UIPattern)pattern).buildControlUI(ui, this);
        } else {
            buildDefaultControlUI(pattern);
        }
    }

    private final static int KNOB_X_SPACING = UIKnob.WIDTH + 4;
    private final static int KNOB_Y_SPACING = 46;

    private void buildDefaultControlUI(LXPattern pattern) {
        float xp = 0, yp = 0;
        int i = 0;
        for (LXParameter parameter : pattern.getParameters()) {
            if (parameter instanceof BoundedParameter) {
                if ((i > 0) && (i % 3 == 0)) {
                    xp += KNOB_X_SPACING;
                    yp = 0;
                }
                BoundedParameter p = (BoundedParameter) parameter;
                new UIKnob(xp, yp)
                .setParameter(p)
                .addToContainer(this);
                yp += KNOB_Y_SPACING;
                ++i;
            }
        }
        setContentWidth((i == 0) ? 0 : (xp + UIKnob.WIDTH));
    }

    @Override
    public void onResize() {
        this.content.setWidth(getWidth() - 2*PADDING);
    }
}