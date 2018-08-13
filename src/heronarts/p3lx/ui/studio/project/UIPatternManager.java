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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIItemList;
import processing.core.PApplet;

public class UIPatternManager extends UIComponentManager {

    public UIPatternManager(UI ui, LX lx, float x, float y, float w) {
        super(ui, lx, x, y, w);
        setTitle("PATTERNS");
        this.itemList.setDescription("Available patterns, double-click to add to the active channel");

        List<LX.PatternInfo> patterns = lx.getRegisteredPatternInfo();
        PatternItem[] items = new PatternItem[patterns.size()];
        for (int i = 0; i < items.length; ++i) {
            LX.PatternInfo pi = patterns.get(i);
            items[i] = new PatternItem(pi.getPattern(), pi.getGroup());
        }
        Arrays.sort(items);
        for (PatternItem item : items) {
            this.itemList.addItem(item);
        }
    }

    private class PatternItem extends UIItemList.AbstractItem implements Comparable<PatternItem> {

        final Class<? extends LXPattern> pattern;
        final String label;
        final String group;

        PatternItem(Class<? extends LXPattern> pattern, String group) {
            this.pattern = pattern;
            String simple = pattern.getSimpleName();
            if (simple.endsWith("Pattern")) {
                simple = simple.substring(0, simple.length() - "Pattern".length());
            }
            this.group = group;
            this.label = simple;
        }

        @Override
        public String getLabel() {
            if (group == null)
                return label;
            return String.format("%s / %s", group, label);
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

        @Override
        public int compareTo(PatternItem o) {
            if (group == null && o.group == null) {
                return label.compareToIgnoreCase(o.label);
            }

            /* Things without groups are always sorted after things with groups */
            if (group == null) {
                return 1;
            }
            if (o.group == null) {
                return -1;
            }

            int groupCompare = group.compareToIgnoreCase(o.group);
            if (groupCompare != 0) {
                return groupCompare;
            }
            return label.compareToIgnoreCase(o.label);
        }
    }
}