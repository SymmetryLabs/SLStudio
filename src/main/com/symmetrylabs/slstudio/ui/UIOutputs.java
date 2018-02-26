package com.symmetrylabs.slstudio.ui;

import java.util.List;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.studio.UICollapsibleSection;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.CubesModel;
import com.symmetrylabs.slstudio.pixlites.NissanPixlite;
import com.symmetrylabs.slstudio.output.SLController;
import com.symmetrylabs.slstudio.util.dispatch.Dispatcher;
import com.symmetrylabs.slstudio.util.listenable.IntListener;
import com.symmetrylabs.slstudio.util.listenable.ListListener;

public class UIOutputs extends UICollapsibleSection {

    public static final float DEFAULT_HEIGHT = 500;
    public static final float TOP_MARGIN = 24;

    public final BooleanParameter clearParam = new BooleanParameter("clear", false);
    public final UIItemList.ScrollList outputList;

    public UIOutputs(LX lx, UI ui, float x, float y, float w) {
        super(ui, x, y, w, DEFAULT_HEIGHT);

        setTitle();

        if (lx.model instanceof CubesModel) {
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
        }

        addTopLevelComponent(
            new UIButton(4, 4, 12, 12)
                .setParameter(SLStudio.applet.outputControl.enabled).setBorderRounding(4)
        );

        addTopLevelComponent(new UIButton(80, 4, 30, 12).setParameter(clearParam));

        clearParam.addListener(it -> {
            for (final NissanPixlite pixlite : SLStudio.applet.pixlites) {
                pixlite.enabled.setValue(false);
            }
            clearParam.setValue(false);
        });

        final List<UIItemList.Item> items = new ArrayList<>();
        outputList = new UIItemList.ScrollList(ui, 0, 0, w - 8, DEFAULT_HEIGHT - TOP_MARGIN);

        for (NissanPixlite pixlite : SLStudio.applet.pixlites) {
            items.add(new PixliteItem(pixlite));
        }

        SortedSet<SLController> sortedControllers = new TreeSet<>((o1, o2) -> {
            try {
                    return Integer.parseInt(o1.cubeId) - Integer.parseInt(o2.cubeId);
            } catch (NumberFormatException e) {
                    return o1.cubeId.compareTo(o2.cubeId);
            }
        });

        for (SLController c : SLStudio.applet.controllers) {
            sortedControllers.add(c);
        }
        for (SLController c : sortedControllers) {
            items.add(new ControllerItem(c));
        }

        outputList.setItems(items).setSingleClickActivate(true);
        outputList.addToContainer(this);

        if (lx.model instanceof CubesModel) {
            final Dispatcher dispatcher = Dispatcher.getInstance(lx);

            final IntListener deviceVersionListener = new IntListener() {
                public void onChange(int version) {
                    dispatcher.dispatchUi(() -> redraw());
                }
            };

            SLStudio.applet.controllers.addListener(new ListListener<SLController>() {
                public void itemAdded(final int index, final SLController c) {
                    dispatcher.dispatchUi(() -> {
                        if (c.networkDevice != null) {
                            c.networkDevice.version.addListener(deviceVersionListener);
                        }

                        sortedControllers.add(c);
                        items.clear();

                        for (SLController controller : sortedControllers) {
                            items.add(new ControllerItem(controller));
                        }

                        outputList.setItems(items);
                        setTitle(items.size()+"");
                        redraw();
                    });
                }
                public void itemRemoved(final int index, final SLController c) {
                    dispatcher.dispatchUi(() -> {
                        if (c.networkDevice != null) {
                            c.networkDevice.version.removeListener(deviceVersionListener);
                        }

                        sortedControllers.remove(c);
                        items.clear();

                        for (SLController controller : sortedControllers) {
                            items.add(new ControllerItem(controller));
                        }

                        outputList.setItems(items);
                        setTitle(items.size()+"");
                        redraw();
                    });
                }
            });
        }
    }

    private void setTitle() {
        setTitle("OUTPUT");
        setTitleX(20);
    }

    class PixliteItem extends UIItemList.AbstractItem {
        final NissanPixlite pixlite;

        PixliteItem(NissanPixlite pixlite) {
            this.pixlite = pixlite;
            pixlite.enabled.addListener(param -> redraw());
        }

        public String getLabel() {
            return "(" + pixlite.ipAddress + ") unknown output channel";
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

    class ControllerItem extends UIItemList.AbstractItem {
        final SLController controller;

        ControllerItem(SLController controller) {
            this.controller = controller;
            controller.enabled.addListener(param -> redraw());
        }

        public String getLabel() {
            if (controller.networkDevice != null && controller.networkDevice.version.get() != -1)
                return controller.cubeId + " (v" + controller.networkDevice.version + ")";

            return controller.cubeId;
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
    }
}
