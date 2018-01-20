package com.symmetrylabs.slstudio.ui;

import java.util.List;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Comparator;

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
import com.symmetrylabs.slstudio.output.SLController;
import com.symmetrylabs.slstudio.util.dispatch.Dispatcher;
import com.symmetrylabs.slstudio.util.listenable.IntListener;
import com.symmetrylabs.slstudio.util.listenable.ListListener;

public class UIOutputs extends UICollapsibleSection {
        UIOutputs(LX lx, UI ui, float x, float y, float w) {
                super(ui, x, y, w, 124);

                final SortedSet<SLController> sortedControllers = new TreeSet<SLController>(new Comparator<SLController>() {
                        public int compare(SLController o1, SLController o2) {
                                try {
                                        return Integer.parseInt(o1.id) - Integer.parseInt(o2.id);
                                } catch (NumberFormatException e) {
                                        return o1.id.compareTo(o2.id);
                                }
                        }
                });

                final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
                for (SLController c : SLStudio.applet.slControllers) { sortedControllers.add(c); }
                for (SLController c : sortedControllers) { items.add(new ControllerItem(c)); }
                final UIItemList.ScrollList outputList = new UIItemList.ScrollList(ui, 0, 22, w-8, 78);

                outputList.setItems(items).setSingleClickActivate(true);
                outputList.addToContainer(this);

                setTitle(items.size());

                SLStudio.applet.slControllers.addListener(new ListListener<SLController>() {
                    public void itemAdded(final int index, final SLController c) {
                        SLStudio.applet.dispatcher.dispatchUi(new Runnable() {
                                public void run() {
                                        if (c.networkDevice != null) c.networkDevice.version.addListener(deviceVersionListener);
                                        sortedControllers.add(c);
                                        items.clear();
                                                for (SLController c : sortedControllers) { items.add(new ControllerItem(c)); }
                                        outputList.setItems(items);
                                        setTitle(items.size());
                                        redraw();
                                }
                        });
                    }
                    public void itemRemoved(final int index, final SLController c) {
                        SLStudio.applet.dispatcher.dispatchUi(new Runnable() {
                                public void run() {
                                        if (c.networkDevice != null) c.networkDevice.version.removeListener(deviceVersionListener);
                                        sortedControllers.remove(c);
                                        items.clear();
                                                for (SLController c : sortedControllers) { items.add(new ControllerItem(c)); }
                                        outputList.setItems(items);
                                        setTitle(items.size());
                                        redraw();
                                }
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

                SLStudio.applet.outputControl.enabled.addListener(new LXParameterListener() {
                    public void onParameterChanged(LXParameter parameter) {
                        redraw();
                    };
                });
        }

        private final IntListener deviceVersionListener = new IntListener() {
                public void onChange(int version) {
                        SLStudio.applet.dispatcher.dispatchUi(new Runnable() {
                        public void run() { redraw(); }
                        });
                }
        };

        private void setTitle(int count) {
                setTitle("OUTPUT (" + count + ")");
                setTitleX(20);
        }

        class ControllerItem extends UIItemList.AbstractItem {
                final SLController controller;

                ControllerItem(SLController _controller) {
                    this.controller = _controller;
                    controller.enabled.addListener(new LXParameterListener() {
                        public void onParameterChanged(LXParameter parameter) { redraw(); }
                    });
                }

                public String getLabel() {
                        if (controller.networkDevice != null && controller.networkDevice.version.get() != -1) {
                                return controller.id + " (v" + controller.networkDevice.version + ")";
                        } else {
                                return controller.id;
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
