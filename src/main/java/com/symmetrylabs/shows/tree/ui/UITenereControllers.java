package com.symmetrylabs.shows.tree.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.p3lx.ui.component.UIButton;

import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.output.LXOutput;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import heronarts.p3lx.ui.component.UIItemList;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.shows.tree.*;
import com.symmetrylabs.shows.tree.config.*;
import static com.symmetrylabs.util.MathUtils.*;
import com.symmetrylabs.shows.absinthe.AbsintheShow;


public class UITenereControllers extends UICollapsibleSection {

    public final UIItemList.ScrollList controllers;
    private final BooleanParameter enableAll = new BooleanParameter("enableAll").setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter disableAll = new BooleanParameter("disableAll").setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter refresh = new BooleanParameter("refresh").setMode(BooleanParameter.Mode.MOMENTARY);
    private final TreeModel tree;
    private final TreeShow show;

    public UITenereControllers(LX lx, UI ui, float x, float y, float w) {
        super(ui, x, y, w, 260);
        setTitle("Tenere Controllers");
        setPadding(5);
        tree = (TreeModel) lx.model;
        show = (TreeShow) SLStudio.applet.show;

        controllers = new UIItemList.ScrollList(ui, 0, 25, w, 200);
        controllers.setSingleClickActivate(true);
        controllers.setShowCheckboxes(true);
        controllers.addToContainer(this);

        new UIButton(0, 0, w / 3 - 10, 20)
            .setLabel("Enable All")
            .setParameter(enableAll)
            .addToContainer(this);
        new UIButton(w / 3, 0, w / 3 - 10, 20)
            .setLabel("Disable All")
            .setParameter(disableAll)
            .addToContainer(this);
        new UIButton(2 * w / 3, 0, w / 3 - 10, 20)
            .setLabel("Refresh")
            .setParameter(refresh)
            .addToContainer(this);
        enableAll.addListener(p -> {
                if (enableAll.getValueb()) {
                    for (AssignableTenereController atc : show.getTenereControllers().values()) {
                        atc.enabled.setValue(true);
                    }
                }
                controllers.redraw();
            });
        disableAll.addListener(p -> {
                if (disableAll.getValueb()) {
                    for (AssignableTenereController atc : show.getTenereControllers().values()) {
                        atc.enabled.setValue(false);
                    }
                }
                controllers.redraw();
            });
        refresh.addListener(p -> refreshControllers());

        refreshControllers();
    }

    private void refreshControllers() {
        List<ControllerItem> items = new ArrayList<>();
        Map<TreeModel.Branch, AssignableTenereController> controllerMap = show.getTenereControllers();
        for (TreeModel.Branch branch : tree.getBranches()) {
            AssignableTenereController atc = controllerMap.get(branch);
            items.add(new ControllerItem(atc));
        }
        controllers.setItems(items);
    }

    private class ControllerItem extends UIItemList.AbstractItem {
        final AssignableTenereController controller;

        ControllerItem(AssignableTenereController controller) {
            this.controller = controller;
        }

        public String getLabel() {
            return controller.getIpAddress();
        }

        public boolean isSelected() {
            return controller.enabled.isOn();
        }

        @Override
        public boolean isActive() {
            return isSelected();
        }

        @Override
        public void onCheck(boolean on) {
            if (on) {
                controller.mode.setValue(LXOutput.Mode.WHITE);
            } else {
                controller.mode.setValue(LXOutput.Mode.NORMAL);
            }
        }

        @Override
        public boolean isChecked() {
            return controller.mode.getEnum() == LXOutput.Mode.WHITE;
        }

        @Override
        public int getActiveColor(UI ui) {
            return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
        }

        @Override
        public void onActivate() {
            controller.enabled.toggle();
        }
    }
}
