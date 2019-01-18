package heronarts.p3lx.ui.studio.project;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.warp.LXWarp;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIItemList;
import processing.core.PApplet;

public class UIWarpManager extends UIComponentManager {

    public  UIWarpManager(UI ui, LX lx, float x, float y, float w) {
        super(ui, lx, x, y, w);
        setTitle("WARPS");
        this.itemList.setDescription("Available warps, double-click to add to the active channel");

        List<Class<? extends LXWarp>> warps = lx.getRegisteredWarps();
        WarpItem[] items = new WarpItem[warps.size()];
        for (int i = 0; i < items.length; ++i) {
            items[i] = new WarpItem(warps.get(i));
        }
        Arrays.sort(items, new Comparator<WarpItem>() {
            @Override
            public int compare(WarpItem o1, WarpItem o2) {
                return o1.label.compareToIgnoreCase(o2.label);
            }
        });
        for (WarpItem item : items) {
            this.itemList.addItem(item);
        }
    }

    private class WarpItem extends UIItemList.AbstractItem {
        final Class<? extends LXWarp> warp;
        final String label;

        WarpItem(Class<? extends LXWarp> warp) {
            this.warp = warp;
            this.label = warp.getSimpleName().replaceAll("Warp$", "");
        }

        public String getLabel() {
            return this.label;
        }

        @Override
        public void onActivate() {
            LXWarp instance = null;
            try {
                try {
                    instance = warp.getConstructor(LX.class).newInstance(lx);
                } catch (NoSuchMethodException nsmx) {
                    try {
                        PApplet applet = ((P3LX)lx).applet;
                        instance = warp.getConstructor(applet.getClass(), LX.class).newInstance(applet, lx);
                    } catch (NoSuchMethodException nsmx2) {
                        nsmx2.printStackTrace();
                    }
                }
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }

            if (instance != null) {
                lx.engine.getFocusedChannel().addWarp(instance);
                instance.enabled.setValue(true);
            }
        }
    }
}
