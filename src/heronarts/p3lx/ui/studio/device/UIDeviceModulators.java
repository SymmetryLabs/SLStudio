/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui.studio.device;

import java.util.HashMap;
import java.util.Map;

import heronarts.lx.LXDeviceComponent;
import heronarts.lx.LXMappingEngine;
import heronarts.lx.LXModulationEngine;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.VariableLFO;
import heronarts.lx.parameter.LXCompoundModulation;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.LXTriggerModulation;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.studio.device.modulator.UIModulator;
import heronarts.p3lx.ui.studio.device.modulator.UIVariableLFO;

public class UIDeviceModulators extends UI2dContainer {

    private final static int ADD_LFO_HEIGHT = 14;
    private final static int PADDING = 4;

    private final UI ui;
    private final LXDeviceComponent device;
    private final UIItemList.ScrollList modulatorList;
    private final UIButton mapButton;

    private final Map<LXModulator, DeviceModulatorItem> modulatorItems = new HashMap<LXModulator, DeviceModulatorItem>();
    private final Map<LXModulator, UIModulator> uiModulators = new HashMap<LXModulator, UIModulator>();
    private UIModulator focusedModulator;

    UIDeviceModulators(final UI ui, final LXDeviceComponent device, float x, float y, float w, float h) {
        super(x, y, w, h);
        setLayout(UI2dContainer.Layout.HORIZONTAL);
        setPadding(PADDING, PADDING+1, PADDING, PADDING);
        setChildMargin(PADDING);
        setBackgroundColor(0xff333333);

        this.ui = ui;
        this.device = device;
        this.focusedModulator = null;

        UI2dContainer listControls = (UI2dContainer)
            new UI2dContainer(0, 0, w, h)
            .addToContainer(this);

        this.modulatorList = (UIItemList.ScrollList)
            new UIItemList.ScrollList(ui, 0, PADDING, w, h - ADD_LFO_HEIGHT - 3*PADDING)
            .addToContainer(listControls);
        this.modulatorList.setRenamable(true);

        new UIButton(0, this.height - ADD_LFO_HEIGHT-PADDING, 24, ADD_LFO_HEIGHT) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    device.modulation.addModulator(new VariableLFO());
                }
            }
        }
        .setMomentary(true)
        .setLabel("+")
        .setDescription("Adds a new modulator to this device")
        .addToContainer(listControls);

        this.mapButton = (UIButton) new UIButton(PADDING + 24, this.height-ADD_LFO_HEIGHT-PADDING, getContentWidth() - 3*PADDING - 24, ADD_LFO_HEIGHT) {
            @Override
            public void onToggle(boolean on) {
                if (on && focusedModulator != null) {
                    ui.mapModulationSource(device.modulation, focusedModulator.getModulationSource());
                } else {
                    ui.mapModulationSource(null);
                }
            }
        }
        .setEnabled(false)
        .setIcon(ui.theme.iconMap)
        .setDescription("Maps the selected device modulator to a device parameter")
        .addToContainer(listControls);

        device.getLX().engine.mapping.mode.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                if (device.getLX().engine.mapping.getMode() != LXMappingEngine.Mode.MODULATION_TARGET) {
                    mapButton.setActive(false);
                }
            }
        });

        for (LXModulator modulator : device.modulation.modulators) {
            addModulator(modulator);
        }

        device.modulation.addListener(new LXModulationEngine.Listener() {

            @Override
            public void modulatorAdded(LXModulationEngine engine, LXModulator modulator) {
                addModulator(modulator);
            }

            @Override
            public void modulatorRemoved(LXModulationEngine engine, LXModulator modulator) {
                removeModulator(modulator);
            }

            @Override
            public void modulationAdded(LXModulationEngine engine, LXCompoundModulation modulation) {
                uiModulators.get(modulation.source).addModulation(modulation);
            }

            @Override
            public void modulationRemoved(LXModulationEngine engine, LXCompoundModulation modulation) {
                uiModulators.get(modulation.source).removeModulation(modulation);
            }

            @Override
            public void triggerAdded(LXModulationEngine engine, LXTriggerModulation modulation) {

            }

            @Override
            public void triggerRemoved(LXModulationEngine engine, LXTriggerModulation modulation) {

            }

        });
    }

    private void addModulator(LXModulator modulator) {
        DeviceModulatorItem item = new DeviceModulatorItem(modulator);
        this.modulatorItems.put(modulator, item);
        this.modulatorList.addItem(item);

        UIModulator uiModulator = null;
        if (modulator instanceof VariableLFO) {
            uiModulator = new UIVariableLFO(this.ui, (VariableLFO) modulator, 0, PADDING+2, 140, getContentHeight() - 2*PADDING - 2);
        }
        if (uiModulator != null) {
            uiModulator.setVisible(false);
            uiModulator.addToContainer(this);
            this.uiModulators.put(modulator, uiModulator);
        }

    }

    private void removeModulator(LXModulator modulator) {
        DeviceModulatorItem item = this.modulatorItems.get(modulator);
        if (item != null) {
            this.modulatorList.removeItem(item);
        }
        UIModulator uiModulator = this.uiModulators.get(modulator);
        if (uiModulator != null) {
            uiModulator.removeFromContainer();
        }
    }

    private void focusModulator(LXModulator modulator) {
        if (modulator != null) {
            UIModulator uiModulator = this.uiModulators.get(modulator);
            if (uiModulator != this.focusedModulator) {
                if (this.focusedModulator != null) {
                    this.focusedModulator.setVisible(false);
                    this.focusedModulator = null;
                }
            }
            if (uiModulator != null) {
                this.focusedModulator = uiModulator;
                this.focusedModulator.setVisible(true);
                this.mapButton.setEnabled(true);
            } else {
                this.focusedModulator = null;
                this.mapButton.setEnabled(false);
            }
        } else {
            if (this.focusedModulator != null) {
                this.focusedModulator.setVisible(false);
                this.focusedModulator = null;
            }
            this.mapButton.setEnabled(false);
        }
    }

    class DeviceModulatorItem extends UIItemList.AbstractItem {

        private final LXModulator modulator;

        DeviceModulatorItem(LXModulator modulator) {
            this.modulator = modulator;
        }

        @Override
        public void onFocus() {
            focusModulator(this.modulator);
        }

        public String getLabel() {
            return this.modulator.getLabel();
        }

        @Override
        public void onRename(String label) {
            this.modulator.label.setValue(label);
        }

        @Override
        public void onDelete() {
            focusModulator(null);
            device.modulation.removeModulator(this.modulator);
        }
    }
}
