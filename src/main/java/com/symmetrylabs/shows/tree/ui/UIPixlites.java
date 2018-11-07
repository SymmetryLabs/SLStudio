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
import com.symmetrylabs.shows.kalpa.KalpaShow;
import com.symmetrylabs.shows.tree.config.*;
import static com.symmetrylabs.util.MathUtils.*;


public class UIPixlites extends UICollapsibleSection {

    public final UIItemList.ScrollList pixlitePorts;

    public UIPixlites(LX lx, UI ui, float x, float y, float w) {
        super(ui, x, y, w, 230);
        setTitle("PIXLITE DATALINES");
        setPadding(5);
        TreeModel tree = (TreeModel)lx.model;

        this.pixlitePorts = new UIItemList.ScrollList(ui, 0, 0, w, 200);
        pixlitePorts.setSingleClickActivate(true);
        pixlitePorts.setShowCheckboxes(true);
        pixlitePorts.addToContainer(this);


        // hardcoding Kalpa show for now
        KalpaShow layout = (KalpaShow)SLStudio.applet.layout;

        List<PortItem> portItems = new ArrayList<>();

        for (String ipAddress : layout.pixlites.keySet()) {
            AssignablePixlite pixlite = layout.pixlites.get(ipAddress);

            for (AssignablePixlite.Port port : pixlite.ports) {
                portItems.add(new PortItem(port));
            }

//            int port = 0;
//            for (int i = 1; i < 14; i += 2) {
//                datalineItems.add(new DatalineItem(pixlite.datalines.get(i), pixlite.datalines.get(i+1), port++));
//            }
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

//    private class ChannelItem extends UIItemList.AbstractItem {
//        final PixliteOutput.ReceiverChannel channel;
//
//        ChannelItem(PixliteOutput.ReceiverChannel channel) {
//            this.channel = channel;
//        }
//
//        public String getLabel() {
//            return "(ip: " + channel.ipAddress.getString() + ")   [port: " + channel.pixlitePort.getValuei() + "]   {ch: " + channel.type.toString() + "}";
//        }
//
//        public boolean isSelected() {
//            return channel.enabled.isOn();
//        }
//
//        @Override
//        public boolean isActive() {
//            return isSelected();
//        }
//
//        @Override
//        public int getActiveColor(UI ui) {
//            return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
//        }
//
//        @Override
//        public void onActivate() {
//            //...
//        }
//    }
}
