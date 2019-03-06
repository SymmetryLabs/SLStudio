package com.symmetrylabs.shows.tree.ui;

import java.util.List;
import java.util.ArrayList;

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

    public UITenereControllers(LX lx, UI ui, float x, float y, float w) {
        super(ui, x, y, w, 230);
        setTitle("Tenere Controllers");
        setPadding(5);
        TreeModel tree = (TreeModel) lx.model;
        AbsintheShow layout = (AbsintheShow) SLStudio.applet.show; // genericize this

        this.controllers = new UIItemList.ScrollList(ui, 0, 0, w, 200);
        controllers.setSingleClickActivate(true);
        controllers.setShowCheckboxes(true);
        controllers.addToContainer(this);

        List<ControllerItem> items = new ArrayList<>();
        for (TreeModel.Branch branch : tree.getBranches()) {
            items.add(new ControllerItem(layout.controllers.get(branch)));
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
