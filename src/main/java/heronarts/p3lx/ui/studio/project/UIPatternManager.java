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

import java.util.*;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import processing.core.PApplet;

public class UIPatternManager extends UICollapsibleSection {
    protected final LX lx;

    private class UIPatternGroup extends UIComponentManager {
        final String name;

        UIPatternGroup(UI ui, LX lx, float x, float y, float w, String name) {
            super(ui, lx, x, y, w);
            this.name = name;
            setTitle(name);
        }
    }

    public UIPatternManager(UI ui, LX lx, float x, float y, float w) {
        super(ui, x, y, w, 0);
        this.lx = lx;

        setTitle("PATTERNS");

        setLayout(UI2dContainer.Layout.VERTICAL);

        List<Class<? extends LXPattern>> patterns = lx.getRegisteredPatterns();
        HashMap<String, List<PatternItem>> groups = new HashMap<>();
        for (Class<? extends LXPattern> p : patterns) {
            String group = LXPattern.getGroupName(p);
            groups.putIfAbsent(group, new ArrayList<>());
            groups.get(group).add(new PatternItem(p));
        }

        for (List<PatternItem> ps : groups.values()) {
            Collections.sort(ps);
        }
        List<String> groupNames = new ArrayList<>(groups.keySet());

        String activeGroup = ui.getActivePatternGroup();
        Collections.sort(groupNames, (a, b) -> {
            int aSortKey, bSortKey;
            if (activeGroup != null) {
                aSortKey = a == null ? 0 : a.equals(activeGroup) ? -1 : 1;
                bSortKey = b == null ? 0 : b.equals(activeGroup) ? -1 : 1;
            } else {
                aSortKey = a == null ? 0 : 1;
                bSortKey = b == null ? 0 : 1;
            }
            int sortKeyCompare = Integer.compare(aSortKey, bSortKey);
            if (sortKeyCompare != 0)
                return -sortKeyCompare;
            return a.compareToIgnoreCase(b);
        });

        for (String groupName : groupNames) {
            String displayName = groupName == null ? "Uncategorized" : groupName;
            if (groupName != null && !groupName.equals("tree"))
                continue;
            UIPatternGroup uipg = new UIPatternGroup(ui, lx, x, y, w - 2 * UICollapsibleSection.PADDING, displayName);
            uipg.addToContainer(this);

            for (PatternItem pi : groups.get(groupName)) {
                uipg.itemList.addItem(pi);
            }
        }
    }

    private class PatternItem extends UIItemList.AbstractItem implements Comparable<PatternItem> {

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

        @Override
        public String getLabel() {
            return label;
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
            return label.compareToIgnoreCase(o.label);
        }
    }
}
