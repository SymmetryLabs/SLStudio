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
import heronarts.lx.LXPattern;
import heronarts.p3lx.ui.UI;

public class UIPatternDevice extends UIDevice {

    private final static int MIN_WIDTH = 40;

    public final LXPattern pattern;

    UIPatternDevice(UI ui, LXChannel channel, LXPattern pattern) {
        super(ui, pattern, MIN_WIDTH);
        setTitle(pattern.label);
        setMinWidth(MIN_WIDTH);
        this.pattern = pattern;
        if (pattern instanceof UIPattern) {
            ((UIPattern) pattern).buildDeviceUI(ui, this);
        } else {
            buildDefaultControlUI(pattern);
        }
    }

}
