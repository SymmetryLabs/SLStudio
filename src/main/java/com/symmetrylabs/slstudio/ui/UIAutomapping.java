package com.symmetrylabs.slstudio.ui;

import java.util.List;
import java.util.ArrayList;

import processing.event.KeyEvent;

import heronarts.lx.LX;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.component.UITextBox;
import heronarts.p3lx.ui.studio.UICollapsibleSection;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.automapping.Automapper;
import com.symmetrylabs.slstudio.automapping.CVFixture;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListListener;

import static processing.core.PConstants.BACKSPACE;

public class UIAutomapping extends UICollapsibleSection {
    SLStudioLX lx;
    SLStudioLX.UI ui;

    private CVFixture previous = null;
    private Automapper automapper;

    public UIAutomapping(SLStudioLX lx, SLStudioLX.UI ui, float x, float y, float w) {
        super(ui, x, y, w, 140);

        this.lx = lx;
        this.ui = ui;

        automapper = Automapper.getInstance(lx);

        setTitle("AUTOMAPPING");
        setTitleX(20);

        int yOffset = 0;
        int padding = 3;

        yOffset += buildEnabledButton(yOffset, w);
        yOffset += buildStartMappingButton(yOffset, w);
        yOffset += padding;
        yOffset += buildMappedCubeList(yOffset, w);
        yOffset += padding;
        yOffset += buildJSONSaver(yOffset, w);
        yOffset += padding;
    }

    int buildEnabledButton(float yOffset, float w) {
        addTopLevelComponent(new UIButton(4, 4, 12, 12) {
                    @Override
                    public void onToggle(boolean isOn) {
                    }
                }
            .setParameter(automapper.running)
            .setBorderRounding(4));

        return 0;
    }

    void centerView() {
        ui.preview.setCenter(0, 0, 0);
        ui.preview.setPhi(0);
        ui.preview.setPerspective(0);
        ui.preview.setTheta(0);
    }

    int buildStartMappingButton(float yOffset, float w) {
        final String disconnected = "No App Connected";
        final String connected = "Start Mapping";
        final String inProgress = "Mapping in Progress";

        int h = 18;

        int buttonW = 120;
        int margin = 3;
        float bigW = w - (buttonW + margin + 8);
        float smallerW = (buttonW - margin) / 2;

        final UIButton startMapping = new UIButton(0, yOffset, bigW, h) {
            @Override
                protected void onToggle(boolean active) {
                if (!active)
                    return;

                switch (automapper.state.getEnum()) {
                case DISCONNECTED:
                    break;
                case CONNECTED:
                    automapper.sendStartCommand();
                    break;
                case RUNNING:
                    break;
                default:
                    throw new RuntimeException("Invalid state in mapping button");
                }
            }
        }
        .setLabel(disconnected).setMomentary(true);
        startMapping.addToContainer(this);

        automapper.state.addListener(param -> {
            String label;
            switch (automapper.state.getEnum()) {
            case DISCONNECTED:
                label = disconnected;
                break;
            case CONNECTED:
                label = connected;
                break;
            case RUNNING:
                label = inProgress;
                break;
            default:
                throw new RuntimeException("Invalid state in button label");
            }

            startMapping.setLabel(label);
        });

        new UIButton(bigW + margin, yOffset, smallerW, h) {
            @Override
                protected void onToggle(boolean active) {
                if (!active) return;

                centerView();
                // automapper.center();
            }
        }
        .setLabel("C. View")
            .setMomentary(true)
            .addToContainer(this);

        new UIButton(bigW + 2*margin + smallerW, yOffset, smallerW, h) {
            @Override
                protected void onToggle(boolean active) {
                if (!active) return;

                // centerView();
                automapper.center();
            }
        }
        .setLabel("C. Model")
            .setMomentary(true)
            .addToContainer(this);

        return h;
    }

    int buildMappedCubeList(float yOffset, float w) {

        int h = 78;

        final List<UIItemList.Item> items = new ArrayList<>();
        final MappedCubeList outputList = new MappedCubeList(ui, 0, yOffset, w-8, h);


        for (CVFixture c : automapper.mappedFixtures) {
            items.add(new MappedCubeItem(c));
        }


        outputList.setItems(items).setSingleClickActivate(true);
        outputList.addToContainer(this);

        final Runnable update = new Runnable() {
            public void run() {
                final List<UIItemList.Item> localItems = new ArrayList<UIItemList.Item>();
                int i = 0;
                for (CVFixture c : automapper.mappedFixtures) {
                    localItems.add(new MappedCubeItem(c));
                }
                outputList.setItems(localItems);
                redraw();
            }
        };

        final Dispatcher dispatcher = Dispatcher.getInstance(lx);
        automapper.mappedFixtures.addListener(new ListListener<CVFixture>() {
            public void itemAdded(final int index, final CVFixture c) {
                dispatcher.dispatchUi(update);
            }
            public void itemRemoved(final int index, final CVFixture c) {
                dispatcher.dispatchUi(update);
            }
        });

        return h;
    }

    int buildJSONSaver(float yOffset, float w) {
        int h = 16;
        int buttonW = 120;
        int margin = 3;
        float textW = w - (buttonW + margin + 8);
        float subButtonW = (buttonW - margin) / 2;

        new UIButton(textW + margin, yOffset, subButtonW, h) {
            @Override
                public void onToggle(boolean isOn) {
                if (!isOn) return;

                automapper.saveToJSON();
            }
        }
        .setLabel("Save")
            .setMomentary(true)
            .addToContainer(this);

        new UIButton(textW + (margin * 2) + subButtonW, yOffset, subButtonW, h) {
            @Override
                public void onToggle(boolean isOn) {
                if (!isOn) return;

                automapper.loadJSON();
                automapper.center();
            }
        }
        .setLabel("Load")
            .setMomentary(true)
            .addToContainer(this);

        new UITextBox(0, yOffset, textW, h) {
            public String getDescription() {
                return "JSON file (inside data folder) to save mapped cubes. Hit enter to edit.";
            }
        }
        .setParameter(automapper.saveFile)
            .addToContainer(this);
        return h;
    }

    class MappedCubeItem extends UIItemList.AbstractItem {
        final CVFixture cube;

        MappedCubeItem(CVFixture _cube) {
            this.cube = _cube;
        }

        @Override
        public String getLabel() {
            return cube.getLabel();
        }

        @Override
            public void onFocus() {
            if (previous != null && previous != cube) {
                previous.setSelected(false);
            }
            cube.setSelected(true);
            previous = cube;
        }
    }

    class MappedCubeList extends UIItemList.ScrollList {

        MappedCubeList(UI ui, float x, float y, float w, float h) {
            super(ui, x, y, w, h);
        }

        @Override
        public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {

            MappedCubeItem item = (MappedCubeItem)getFocusedItem();
            if (item == null) return;

            CVFixture cube = item.cube;

            if (keyCode == BACKSPACE) {
                automapper.removeCube(cube.id);
                return;
            }

            if (keyChar == 'h') {
                System.out.println("SWAPPING LABELS");
                automapper.hideLabels = !automapper.hideLabels;
                return;
            }

            String dirs = "udlrbf";

            if (dirs.indexOf(keyChar) != -1) {
                automapper.rotateCubes(keyChar);
                return;
            }

            super.onKeyPressed(keyEvent, keyChar, keyCode);
        }

        @Override
        public void onBlur() {
            if (previous != null) {
                previous.setSelected(false);
                previous = null;
            }
        }
    }
}
