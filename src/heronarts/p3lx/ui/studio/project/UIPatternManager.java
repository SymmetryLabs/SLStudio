/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
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

package heronarts.p3lx.ui.studio.project;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIItemList;
import processing.core.PApplet;

public class UIPatternManager extends UIComponentManager {

    public UIPatternManager(UI ui, LX lx, float x, float y, float w, float h) {
        super(ui, lx, x, y, w, h);
        setTitle("PATTERNS");

        this.itemList.setDescription("Available patterns, double-click to add to the active channel");

        for (Class<? extends LXPattern> pattern : lx.getRegisteredPatterns()) {
            this.itemList.addItem(new PatternItem(pattern));
        }
    }

    private class PatternItem extends UIItemList.AbstractItem {

        final Class<? extends LXPattern> pattern;
        final String label;

        PatternItem(Class<? extends LXPattern> pattern) {
            this.pattern = pattern;
            String simple = pattern.getSimpleName();
            if (simple.endsWith("Pattern")) {
                simple = simple.substring(0, simple.length() - "Pattern".length());
            }
            this.label = simple;
        }

        public String getLabel() {
            return this.label;
        }

        @Override
        public void onActivate() {
            LXPattern instance = null;
            try {
                try {
                    instance = pattern.getConstructor(LX.class).newInstance(lx);
                } catch (NoSuchMethodException nsmx) {
                    try {
                        PApplet applet = ((P3LX)lx).applet;
                        instance = pattern.getConstructor(applet.getClass(), LX.class).newInstance(applet, lx);
                    } catch (NoSuchMethodException nsmx2) {
                        nsmx2.printStackTrace();
                    }
                }
            } catch (java.lang.reflect.InvocationTargetException itx) {
                itx.printStackTrace();
            } catch (IllegalAccessException ix) {
                ix.printStackTrace();
            } catch (InstantiationException ix) {
                ix.printStackTrace();
            }

            if (instance != null) {
                LXBus channel = lx.engine.getFocusedChannel();
                if (channel instanceof LXChannel) {
                    ((LXChannel)channel).addPattern(instance);
                } else {
                    lx.engine.addChannel(new LXPattern[] { instance });
                }
            }
        }
    }
}