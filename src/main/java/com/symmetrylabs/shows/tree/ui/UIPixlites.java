package com.symmetrylabs.shows.tree.ui;

import java.util.List;
import java.util.ArrayList;
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


public class UIPixlites extends UICollapsibleSection {

    public final UIItemList.ScrollList pixlitePorts;
    private final BooleanParameter enableAll = new BooleanParameter("enableAll").setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter disableAll = new BooleanParameter("disableAll").setMode(BooleanParameter.Mode.MOMENTARY);

    public UIPixlites(LX lx, UI ui, float x, float y, float w) {
        super(ui, x, y, w, 260);
        setTitle("PIXLITE DATALINES");
        setPadding(5);
        TreeModel tree = (TreeModel)lx.model;
        TreeShow show = (TreeShow)SLStudio.applet.show;

        this.pixlitePorts = new UIItemList.ScrollList(ui, 0, 25, w, 200);
        pixlitePorts.setSingleClickActivate(true);
        pixlitePorts.setShowCheckboxes(true);
        pixlitePorts.addToContainer(this);

        new UIButton(0, 0, w / 2 - 10, 20)
            .setLabel("Enable All")
            .setParameter(enableAll)
            .addToContainer(this);
        new UIButton(w / 2, 0, w / 2 - 10, 20)
            .setLabel("Disable All")
            .setParameter(disableAll)
            .addToContainer(this);
        enableAll.addListener(p -> {
                if (enableAll.getValueb()) {
                    for (AssignablePixlite ap : show.pixlites.values()) {
                        ap.enabled.setValue(true);
                    }
                    pixlitePorts.redraw();
                }
            });
        disableAll.addListener(p -> {
                if (disableAll.getValueb()) {
                    for (AssignablePixlite ap : show.pixlites.values()) {
                        ap.enabled.setValue(false);
                    }
                    pixlitePorts.redraw();
                }
            });

        List<PortItem> portItems = new ArrayList<>();

        for (String ipAddress : show.pixlites.keySet()) {
            AssignablePixlite pixlite = show.pixlites.get(ipAddress);

            for (AssignablePixlite.Port port : pixlite.ports) {
                portItems.add(new PortItem(port));
            }
        }
        pixlitePorts.setItems(portItems);
    }

    private class PortItem extends UIItemList.AbstractItem {
        final AssignablePixlite.Port port;

        PortItem(AssignablePixlite.Port port) {
            this.port = port;
        }

        public String getLabel() {
            return "(ip: " + port.ipAddress + ")   [port: " + port.index + "]";
        }

        public boolean isSelected() {
            return port.enabled.isOn();
        }

        @Override
        public boolean isActive() {
            return isSelected();
        }

        @Override
        public void onCheck(boolean on) {
            if (on) {
                port.mode.setValue(LXOutput.Mode.WHITE);
            } else {
                port.mode.setValue(LXOutput.Mode.NORMAL);
            }
        }

        @Override
        public boolean isChecked() {
            return port.mode.getEnum() == LXOutput.Mode.WHITE;
        }

        @Override
        public int getActiveColor(UI ui) {
            return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
        }

        @Override
        public void onActivate() {
            port.enabled.toggle();
        }
    }
}
