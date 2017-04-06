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

import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContext;
import heronarts.p3lx.ui.component.UILabel;
import processing.core.PConstants;

public class UIContextualHelpBar extends UI2dContext {
    public static final int PADDING = 8;
    public static final int HEIGHT = 19;
    public final UILabel label;

    public UIContextualHelpBar(final UI ui) {
        super(ui, 0, ui.getHeight() - HEIGHT, ui.getWidth(), HEIGHT + 4);
        setBackgroundColor(ui.theme.getPaneBackgroundColor());

        this.label = (UILabel) new UILabel(PADDING, 0, getContentWidth() - 2*PADDING, HEIGHT + 4)
        .setPadding(5, 0, 0, PADDING)
        .setTextAlignment(PConstants.LEFT, PConstants.TOP)
        .setFontColor(0xffa0a0a0)
        .setBackgroundColor(ui.theme.getPaneInsetColor())
        .setBorderRounding(4)
        .addToContainer(this);

        ui.contextualHelpText.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                label.setLabel(ui.contextualHelpText.getString());
            }
        });
    }

    @Override
    protected void onUIResize(UI ui) {
        setWidth(ui.getWidth());
        this.label.setWidth(getContentWidth() - 2*PADDING);
        setY(ui.getHeight() - HEIGHT);
    }

}
