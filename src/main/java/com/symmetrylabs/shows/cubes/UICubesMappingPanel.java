package com.symmetrylabs.shows.cubes;

import heronarts.p3lx.ui.component.*;
import processing.core.PConstants;

import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.studio.UICollapsibleSection;

import com.symmetrylabs.slstudio.SLStudio;

/**
 * Mapping Mode: UI Window
 */
public class UICubesMappingPanel extends UICollapsibleSection {
    private int yOff = 0;
    private int ITEM_SIZE = 20;
    private CubesMappingMode mappingMode;

    public UICubesMappingPanel(LX lx, UI ui, float x, float y, float w) {
        super(ui, x, y, w, 142);

        mappingMode = CubesMappingMode.getInstance(lx);

        setTitle("MAPPING");
        setTitleX(20);

        addTopLevelComponent(new UIButton(4, 4, 12, 12) {
            @Override
            public void onToggle(boolean isOn) {
                redraw();
            }
        }.setParameter(mappingMode.enabled).setBorderRounding(4));

        final UIToggleSet toggleMode = new UIToggleSet(0, yOff, getContentWidth(), ITEM_SIZE)
         .setEvenSpacing().setParameter(mappingMode.mode);
        toggleMode.addToContainer(this);
        yOff += ITEM_SIZE;

        final UIMappedPanel mappedPanel = new UIMappedPanel(ui, 0, yOff, getContentWidth(), ITEM_SIZE*2);
        mappedPanel.setVisible(mappingMode.inMappedMode());
        mappedPanel.addToContainer(this);

        final UIUnMappedPanel unMappedPanel = new UIUnMappedPanel(ui, 0,yOff, getContentWidth(), ITEM_SIZE*2);
        unMappedPanel.setVisible(mappingMode.inUnMappedMode());
        unMappedPanel.addToContainer(this);

        mappingMode.mode.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                mappedPanel.setVisible(mappingMode.inMappedMode());
                unMappedPanel.setVisible(mappingMode.inUnMappedMode());
                redraw();
            }
        });

    }

    private class UIMappedPanel extends UI2dContainer {
        UIMappedPanel(UI ui, float x, float y, float w, float h) {
            super(x, y, w, h);

            final UIToggleSet toggleDisplayMode = new UIToggleSet(0, 2, 112, 18)
             .setEvenSpacing().setParameter(mappingMode.displayMode);
            toggleDisplayMode.addToContainer(this);


            final UITextBox controllerIDa = new UITextBox(0, 20, getContentWidth()/2, 20).setParameter(mappingMode.selectedControllerA);
            controllerIDa.addToContainer(this);
            final UITextBox controllerIDb = new UITextBox(getContentWidth()/2, 20, getContentWidth()/2, 20).setParameter(mappingMode.selectedControllerB);
            controllerIDb.addToContainer(this);

//            final UILabel selectedFixtureLabel = new UILabel(0, 24, getContentWidth(), 54)
//                .setLabel("");
//            selectedFixtureLabel.setBackgroundColor(0xff333333)
//                .setFont(SLStudio.applet.createFont("ArialUnicodeMS-10.vlw", 20))
//                .setTextAlignment(PConstants.CENTER, PConstants.TOP);
//            selectedFixtureLabel.addToContainer(this);
            mappingMode.selectedMappedFixture.addListener(new LXParameterListener() {
                public void onParameterChanged(LXParameter p) {
                    if (mappingMode.inMappedMode())
                        mappingMode.getSelectedMappedFixtureId();
                }
            });

            mappingMode.displayMode.addListener(new LXParameterListener() {
                public void onParameterChanged(LXParameter p) {
                    String label = mappingMode.inDisplayAllMode()
                        ? "" : mappingMode.getSelectedMappedFixtureId();
                    redraw();
                }
            });

            final UIButton decrementSelectedFixture = new UIButton(122, 2, 25, 18) {
                @Override
                protected void onToggle(boolean active) {
                    if (mappingMode.inDisplayAllMode() || !active) return;
                    mappingMode.selectedMappedFixture.decrement(1);
                }
            }.setLabel("-").setMomentary(true);
            decrementSelectedFixture.addToContainer(this);

            final UIButton incrementSelectedFixture = new UIButton(147, 2, 25, 18) {
                @Override
                protected void onToggle(boolean active) {
                    if (mappingMode.inDisplayAllMode() || !active) return;
                    mappingMode.selectedMappedFixture.increment(1);
                }
            }.setLabel("+").setMomentary(true);
            incrementSelectedFixture.addToContainer(this);
        }
    }

    private class UIUnMappedPanel extends UI2dContainer {
        UIUnMappedPanel(UI ui, float x, float y, float w, float h) {
            super(x, y, w, h);

            final UIToggleSet toggleDisplayMode = new UIToggleSet(0, 2, 112, 18)
             .setEvenSpacing().setParameter(mappingMode.displayMode);
            toggleDisplayMode.addToContainer(this);

            final UITextBox controllerIDa = new UITextBox(0, 20, getContentWidth()/2, 20).setParameter(mappingMode.selectedControllerA);
            controllerIDa.addToContainer(this);
            final UITextBox controllerIDb = new UITextBox(getContentWidth()/2, 20, getContentWidth()/2, 20).setParameter(mappingMode.selectedControllerB);
            controllerIDb.addToContainer(this);

//            final UILabel selectedFixtureLabel = new UILabel(0, 24, getContentWidth(), 54)
//                .setLabel("");
//            selectedFixtureLabel.setBackgroundColor(0xff333333)
//                .setFont(SLStudio.applet.createFont("ArialUnicodeMS-10.vlw", 20))
//                .setTextAlignment(PConstants.CENTER, PConstants.TOP);
//            selectedFixtureLabel.addToContainer(this);
            mappingMode.selectedUnMappedFixture.addListener(new LXParameterListener() {
                public void onParameterChanged(LXParameter p) {
                    if (mappingMode.inUnMappedMode())
                        mappingMode.getSelectedUnMappedFixtureId();
                }
            });

//            mappingMode.displayMode.addListener(new LXParameterListener() {
//                public void onParameterChanged(LXParameter p) {
//                    String label = mappingMode.inDisplayAllMode()
//                        ? "" : mappingMode.getSelectedUnMappedFixtureId();
//                    selectedFixtureLabel.setLabel(label);
//                    redraw();
//                }
//            });

            final UIButton decrementSelectedFixture = new UIButton(122, 2, 25, 18) {
                @Override
                protected void onToggle(boolean active) {
                    if (mappingMode.inDisplayAllMode() || !active) return;
                    mappingMode.selectedUnMappedFixture.decrement(1);
                }
            }.setLabel("-").setMomentary(true);
            decrementSelectedFixture.addToContainer(this);

            final UIButton incrementSelectedFixture = new UIButton(147, 2, 25, 18) {
                @Override
                protected void onToggle(boolean active) {
                    if (mappingMode.inDisplayAllMode() || !active) return;
                    mappingMode.selectedUnMappedFixture.increment(1);
                }
            }.setLabel("+").setMomentary(true);
            incrementSelectedFixture.addToContainer(this);
        }
    }
}
