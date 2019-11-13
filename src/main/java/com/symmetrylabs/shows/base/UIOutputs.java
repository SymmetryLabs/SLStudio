package com.symmetrylabs.shows.base;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.output.AbstractSLControllerBase;
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

public class UIOutputs extends UICollapsibleSection {
    private final UIItemList.ScrollList outputList;

    private Dispatcher dispatcher;

    public UIOutputs(LX lx, UI ui, SLShow show, float x, float y, float w) {
        super(ui, x, y, w, 324);

        dispatcher = Dispatcher.getInstance(lx);

        outputList = new UIItemList.ScrollList(ui, 0, 22, w-8, 278);

        updateItems(show);
        outputList.setSingleClickActivate(true);
        outputList.addToContainer(this);

        show.addControllerSetListener(new SetListener<AbstractSLControllerBase>() {
            public void onItemAdded(final AbstractSLControllerBase c) {
                dispatcher.dispatchUi(() -> {
                    if (c.networkDevice != null) {
                    c.networkDevice.version.addListener(deviceVersionListener);
                    }

                    updateItems(show);
                });
            }

            public void onItemRemoved(final AbstractSLControllerBase c) {
                dispatcher.dispatchUi(() -> {
                    if (c.networkDevice != null) {
                    c.networkDevice.version.removeListener(deviceVersionListener);
                    }

                    updateItems(show);
                });
            }
        });

        UIButton testOutput = new UIButton(0, 0, w/2 - 8, 19) {
            @Override
            public void onToggle(boolean isOn) { }
        }.setLabel("Test Broadcast").setParameter(SLStudio.applet.outputControl.testBroadcast);
        testOutput.addToContainer(this);

        UIButton resetCubes = new UIButton(w/2-6, 0, w/2 - 1, 19) {
            @Override
            public void onToggle(boolean isOn) {
            SLStudio.applet.outputControl.controllerResetModule.enabled.setValue(isOn);
            }
        }.setMomentary(true).setLabel("Reset Controllers");
        resetCubes.addToContainer(this);

        addTopLevelComponent(new UIButton(4, 4, 12, 12) {}
            .setParameter(SLStudio.applet.outputControl.enabled).setBorderRounding(4));

        SLStudio.applet.outputControl.enabled.addListener(param -> redraw());
    }

    private void updateItems(SLShow show) {
        final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
        for (AbstractSLControllerBase c : show.getSortedControllers()) { items.add(new ControllerItem(c)); }
        outputList.setItems(items);
        setTitle(items.size());
        redraw();
    }

    private final IntListener deviceVersionListener = version -> dispatcher.dispatchUi(this::redraw);

    private void setTitle(int count) {
        setTitle("OUTPUT (" + count + ")");
        setTitleX(20);
    }

    class ControllerItem extends UIItemList.AbstractItem {
        final AbstractSLControllerBase controller;

        ControllerItem(AbstractSLControllerBase _controller) {
            this.controller = _controller;
            controller.enabled.addListener(param -> redraw());
        }

        public String getLabel() {
            NetworkDevice device = controller.networkDevice;
            if (device != null && !device.versionId.isEmpty()) {
                return controller.humanID + " (" + device.versionId + ")";
            } else if (device != null && device.version.get() >= 0) {
                return controller.humanID + " (v" + device.version + ")";
            } else {
                return controller.humanID;
            }
        }

        public boolean isSelected() {
            return controller.enabled.isOn();
        }

        @Override
        public boolean isActive() {
            return controller.enabled.isOn();
        }

        @Override
        public int getActiveColor(UI ui) {
            return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
        }

        @Override
        public void onActivate() {
            if (!SLStudio.applet.outputControl.enabled.getValueb())
                return;
            controller.enabled.toggle();
        }

        // @Override
        // public void onDeactivate() {
        //     println("onDeactivate");
        //     controller.enabled.setValue(false);
        // }
    }
}
