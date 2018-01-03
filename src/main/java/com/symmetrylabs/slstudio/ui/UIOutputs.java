package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.pixlites.Pixlite;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.studio.UICollapsibleSection;

import java.util.ArrayList;
import java.util.List;


public class UIOutputs extends UICollapsibleSection {

    public static final float DEFAULT_HEIGHT = 500;
    public static final float TOP_MARGIN = 24;

    final BooleanParameter clearParam = new BooleanParameter("clear", false);

    public final UIItemList.ScrollList outputList;

    UIOutputs(LX lx, UI ui, float x, float y, float w) {
        super(ui, x, y, w, DEFAULT_HEIGHT);
        setTitle();

        addTopLevelComponent(
            new UIButton(4, 4, 12, 12)
                .setParameter(SLStudio.applet.outputControl.enabled).setBorderRounding(4)
        );

        addTopLevelComponent(new UIButton(80, 4, 30, 12).setParameter(clearParam));
        clearParam.addListener(it -> {
            for (final Pixlite pixlite : SLStudio.applet.pixlites) {
                pixlite.enabled.setValue(false);
            }
            clearParam.setValue(false);
        });


        final List<UIItemList.Item> items = new ArrayList<>();
        outputList = new UIItemList.ScrollList(ui, 0, 0, w - 8, DEFAULT_HEIGHT - TOP_MARGIN);

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
