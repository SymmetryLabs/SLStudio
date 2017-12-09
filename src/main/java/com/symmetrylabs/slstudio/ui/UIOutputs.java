package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.pixlites.Pixlite;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.studio.UICollapsibleSection;

import java.util.ArrayList;
import java.util.List;


class UIOutputs extends UICollapsibleSection {
    UIOutputs(LX lx, UI ui, float x, float y, float w) {
        super(ui, x, y, w, 500);
        setTitle();

        addTopLevelComponent(new UIButton(4, 4, 12, 12) {
        }
            .setParameter(SLStudio.applet.outputControl.enabled).setBorderRounding(4));

        final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
        final UIItemList.ScrollList outputList = new UIItemList.ScrollList(ui, 0, 0, w - 8, 476);

        for (Pixlite pixlite : SLStudio.applet.pixlites) {
            items.add(new PixliteItem(pixlite));
        }

        outputList.setItems(items).setSingleClickActivate(true);
        outputList.addToContainer(this);
    }


    private void setTitle() {
        setTitle("OUTPUT");
        setTitleX(20);
    }

    class PixliteItem extends UIItemList.AbstractItem {
        final Pixlite pixlite;

        PixliteItem(Pixlite pixlite) {
            this.pixlite = pixlite;
            pixlite.enabled.addListener(new LXParameterListener() {
                public void onParameterChanged(LXParameter parameter) {
                    redraw();
                }
            });
        }

        public String getLabel() {
            return "(" + pixlite.ipAddress + ") " + pixlite.slice.id;
        }

        public boolean isSelected() {
            return pixlite.enabled.isOn();
        }

        @Override
        public boolean isActive() {
            return pixlite.enabled.isOn();
        }

        @Override
        public int getActiveColor(UI ui) {
            return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
        }

        @Override
        public void onActivate() {
            if (!SLStudio.applet.outputControl.enabled.getValueb())
                return;
            pixlite.enabled.toggle();
        }
    }
}
