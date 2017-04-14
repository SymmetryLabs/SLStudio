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
import heronarts.lx.LXEffect;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIItemList;
import processing.core.PApplet;

public class UIEffectManager extends UIComponentManager {

    public  UIEffectManager(UI ui, LX lx, float x, float y, float w, float h) {
        super(ui, lx, x, y, w, h);
        setTitle("EFFECTS");
        this.itemList.setDescription("Available effects, double-click to add to the active channel");

        List<Class<? extends LXEffect>> effects = lx.getRegisteredEffects();
        EffectItem[] items = new EffectItem[effects.size()];
        for (int i = 0; i < items.length; ++i) {
            items[i] = new EffectItem(effects.get(i));
        }
        Arrays.sort(items, new Comparator<EffectItem>() {
            @Override
            public int compare(EffectItem o1, EffectItem o2) {
                return o1.label.compareToIgnoreCase(o2.label);
            }
        });
        for (EffectItem item : items) {
            this.itemList.addItem(item);
        }
    }

    private class EffectItem extends UIItemList.AbstractItem {

        final Class<? extends LXEffect> effect;
        final String label;

        EffectItem(Class<? extends LXEffect> effect) {
            this.effect = effect;
            String simple = effect.getSimpleName();
            if (simple.endsWith("Effect")) {
                simple = simple.substring(0, simple.length() - "Effect".length());
            }
            this.label = simple;
        }

        public String getLabel() {
            return this.label;
        }

        @Override
        public void onActivate() {
            LXEffect instance = null;
            try {
                try {
                    instance = effect.getConstructor(LX.class).newInstance(lx);
                } catch (NoSuchMethodException nsmx) {
                    try {
                        PApplet applet = ((P3LX)lx).applet;
                        instance = effect.getConstructor(applet.getClass(), LX.class).newInstance(applet, lx);
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
                lx.engine.getFocusedChannel().addEffect(instance);
            }
        }
    }
}
