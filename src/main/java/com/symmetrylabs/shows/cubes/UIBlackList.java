package com.symmetrylabs.shows.cubes;

import com.symmetrylabs.shows.cubes.CubesController;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.IntListener;
import com.symmetrylabs.util.listenable.SetListener;
import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.studio.UICollapsibleSection;

import java.util.ArrayList;
import java.util.List;

public class UIBlackList extends UICollapsibleSection {
    private final UIItemList.ScrollList outputList;

    private Dispatcher dispatcher;

    public UIBlackList(LX lx, UI ui, CubesShow show, float x, float y, float w) {
        super(ui, x, y, w, 124);

        dispatcher = Dispatcher.getInstance(lx);

        outputList = new UIItemList.ScrollList(ui, 0, 0, w-8, 100);

        updateItems(show);
        outputList.setSingleClickActivate(true);
        outputList.addToContainer(this);

        show.addControllerSetListener(new SetListener<CubesController>() {
            public void onItemAdded(final CubesController c) {
            dispatcher.dispatchUi(() -> {
                if (c.networkDevice != null) {
                c.networkDevice.version.addListener(deviceVersionListener);
                }
                updateItems(show);
            });
            }

            public void onItemRemoved(final CubesController c) {
            dispatcher.dispatchUi(() -> {
                if (c.networkDevice != null) {
                c.networkDevice.version.removeListener(deviceVersionListener);
                }
                updateItems(show);
            });
            }
        });

//        SLStudio.applet.outputControl.enabled.addListener(param -> redraw());
    }

      private void updateItems(CubesShow show) {
        final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();

        String blacklist[] = {
            "397",
            "?14",
            "530",
            "1071"
        };

        for (CubesController c : show.getSortedControllers()) {
            for (String id : blacklist){
                if (c.id.equals(id)){
                    items.add(new ControllerItem(c));
                }
            }
        }
        outputList.setItems(items);
        setTitle(items.size());
        redraw();
      }

    private final IntListener deviceVersionListener = version -> dispatcher.dispatchUi(this::redraw);

    private void setTitle(int count) {
        setTitle("BLACKLIST (" + count + ")");
        setTitleX(20);
    }

    class ControllerItem extends UIItemList.AbstractItem {
        final CubesController controller;

        ControllerItem(CubesController _controller) {
            this.controller = _controller;
            controller.blacklist.addListener(param -> redraw());
        }

        public String getLabel() {
            NetworkDevice device = controller.networkDevice;
            if (device != null && !device.versionId.isEmpty()) {
                return controller.id + " (" + device.versionId + ")";
            } else if (device != null && device.version.get() >= 0) {
                return controller.id + " (v" + device.version + ")";
            } else {
                return controller.id;
            }
        }

        public boolean isSelected() {
            return controller.blacklist.isOn();
        }

        @Override
        public boolean isActive() {
            return controller.blacklist.isOn();
        }

        @Override
        public int getActiveColor(UI ui) {
            return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
        }

        @Override
        public void onActivate() {
            controller.blacklist.toggle();
        }

        // @Override
        // public void onDeactivate() {
        //     println("onDeactivate");
        //     controller.enabled.setValue(false);
        // }
    }
}
