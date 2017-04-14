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
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.p3lx.pattern;

import java.io.IOException;

import heronarts.lx.LX;
import heronarts.lx.script.LXScriptPattern;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UITextBox;
import heronarts.p3lx.ui.studio.device.UIPattern;
import heronarts.p3lx.ui.studio.device.UIPatternControl;
import processing.core.PConstants;

public class JavascriptPattern extends LXScriptPattern implements UIPattern {

    public JavascriptPattern(LX lx) {
        super(lx);
    }

    @Override
    public void buildControlUI(UI ui, UIPatternControl container) {
        container.setWidth(120);

        new UITextBox(0, 0, container.getContentWidth() - 18, 16)
        .setParameter(this.scriptPath)
        .setTextAlignment(PConstants.LEFT)
        .addToContainer(container);

        new UIButton(container.getContentWidth() - 16, 0, 16, 16) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    initialize();
                }
            }
        }
        .setLabel("\u21BA")
        .setMomentary(true)
        .addToContainer(container);

        new UIButton(0, 20, container.getContentWidth(), 16) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    try {
                        java.awt.Desktop.getDesktop().edit(getFile());
                    } catch (IOException iox) {
                        System.err.println(iox.getLocalizedMessage());
                    }
                }
            }
        }
        .setLabel("Edit")
        .setMomentary(true)
        .addToContainer(container);

    }

}
