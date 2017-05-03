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
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIKnob;

public class UIPatternControl extends UI2dContainer {

    private static final int PADDING = 4;

    public final LXPattern pattern;
    private final UI2dContainer content;

    public UIPatternControl(final UI ui, final LXPattern pattern, float x, float y, float h) {
        super(x, y, 2*PADDING, h);
        this.pattern = pattern;
        setBackgroundColor(0xff303030);
        setBorderRounding(4);

        this.pattern.controlSurfaceSempahore.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                if (pattern.controlSurfaceSempahore.getValue() > 0) {
                    setBorderColor(ui.theme.getSurfaceColor());
                } else {
                    setBorder(false);
                }
            }
        });

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

    private void buildDefaultControlUI(LXPattern pattern) {
        this.content.setPadding(2, 0, 0, 0);
        this.content.setLayout(UI2dContainer.Layout.VERTICAL_GRID);
        this.content.setChildMargin(2, 4);
        for (LXParameter parameter : pattern.getParameters()) {
            if (parameter instanceof BoundedParameter || parameter instanceof DiscreteParameter) {
                new UIKnob((LXListenableNormalizedParameter) parameter).addToContainer(this);
            }
        }
    }

    @Override
    public void onResize() {
        this.content.setWidth(getWidth() - 2*PADDING);
    }
}