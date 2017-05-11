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

import heronarts.lx.LXDeviceComponent;
import heronarts.lx.modulator.LXModulator;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIItemList;

public class UIDeviceModulators extends UI2dContainer {

    private final static int ADD_LFO_HEIGHT = 14;
    private final static int PADDING = 4;

    private final UIItemList.ScrollList modulatorList;
    private final UIButton mapButton;

    UIDeviceModulators(UI ui, LXDeviceComponent device, float x, float y, float w, float h) {
        super(x, y, w, h);
        setBackgroundColor(0xff333333);

        this.modulatorList = (UIItemList.ScrollList)
            new UIItemList.ScrollList(ui, PADDING, PADDING, w - 2*PADDING, h - ADD_LFO_HEIGHT - 3*PADDING)
            .addToContainer(this);

        this.modulatorList.setRenamable(true);

        new UIButton(PADDING, this.height-ADD_LFO_HEIGHT-PADDING, 24, ADD_LFO_HEIGHT) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    // TODO(mcslee): add LFO to device
                    modulatorList.addItem(new DeviceModulatorItem());
                }
            }
        }
        .setMomentary(true)
        .setLabel("+")
        .addToContainer(this);

        this.mapButton = (UIButton) new UIButton(2*PADDING + 24, this.height-ADD_LFO_HEIGHT-PADDING, getContentWidth() - 3*PADDING - 24, ADD_LFO_HEIGHT) {
            @Override
            public void onToggle(boolean on) {
                if (on) {

                }
            }
        }
        .setEnabled(false)
        .setIcon(ui.theme.iconMap)
        .addToContainer(this);

        device.addDeviceListener(new LXDeviceComponent.DeviceListener() {

            @Override
            public void lfoRemoved(LXDeviceComponent device, LXModulator modulator) {
                // TODO Auto-generated method stub

            }

            @Override
            public void lfoAdded(LXDeviceComponent device, LXModulator modulator) {
                // TODO Auto-generated method stub

            }
        });
    }

    class DeviceModulatorItem extends UIItemList.AbstractItem {

        private String label = "LFO";

        DeviceModulatorItem() {

        }

        @Override
        public void onFocus() {
            mapButton.setEnabled(true);
        }

        public String getLabel() {
            return this.label;
        }

        @Override
        public void onRename(String label) {
            this.label = label;
        }

        @Override
        public void onDelete() {
            mapButton.setEnabled(false);
            modulatorList.removeItem(this);
        }
    }
}
