package com.symmetrylabs.shows.cubes;

import com.symmetrylabs.slstudio.SLStudio;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.*;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import processing.core.PConstants;

/**
 * Mapping Mode: UI Window
 */
public class UINateMapper extends UICollapsibleSection {
    private static final int SPACING = 0;
    private int yOff = 0;
    private NateMappingMode mappingMode;

    private static final int ITEM_HEIGHT = 20;

    public UINateMapper(LX lx, UI ui, float x, float y, float w) {
        super(ui, x, y, w, 108);


        mappingMode = NateMappingMode.getInstance(lx);

        setTitle("TOP_Mapper");
        setTitleX(20);

        addTopLevelComponent(new UIButton(4, 4, 12, 12) {
            @Override
            public void onToggle(boolean isOn) {
                redraw();
            }
        }.setParameter(mappingMode.enabled).setBorderRounding(8));

        final UIToggleSet toggleMode = new UIToggleSet(0, yOff, getContentWidth(), ITEM_HEIGHT)
         .setEvenSpacing().setParameter(mappingMode.mode);
        toggleMode.addToContainer(this);
        yOff += ITEM_HEIGHT;


//        final UITextBox controllerIDa = new UITextBox(0, 20, getContentWidth()/2, 20).setParameter(mappingMode.selectedModelFixture);
        final UIIntegerBox controllerIDa = new UIIntegerBox(0, yOff, getContentWidth() / 2, ITEM_HEIGHT) {
        }.setParameter(mappingMode.selectedModelFixture);
        controllerIDa.addToContainer(this);

        UILabel selectedFixtureLabel = new UILabel(getContentWidth()/2, yOff, getContentWidth()/2, ITEM_HEIGHT).setLabel(mappingMode.selectedModelFixture.getOption());
        selectedFixtureLabel.setBackgroundColor(0xff333333)
            .setFont(SLStudio.applet.createFont("ArialUnicodeMS-10.vlw", 20))
            .setTextAlignment(PConstants.CENTER, PConstants.TOP);
        selectedFixtureLabel.addToContainer(this);
        mappingMode.selectedModelFixture.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                selectedFixtureLabel.setLabel(mappingMode.selectedModelFixture.getOption());
            }
        });
        ///////////////////////
        yOff += ITEM_HEIGHT;
        ///////////////////////

        int labelSize = 60;
        new UILabel(6, yOff, labelSize, ITEM_HEIGHT)
            .setLabel("Remap to...")
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .addToContainer(this);

        final UITextBox remapTarget = new UITextBox(labelSize, yOff, getContentWidth() - (labelSize + SPACING), ITEM_HEIGHT);
        remapTarget.setParameter(mappingMode.selectedOutput);
        remapTarget.addToContainer(this);

        ///////////////////////
        yOff += ITEM_HEIGHT;
        ///////////////////////

        new UIButton(0, yOff, getContentWidth(), ITEM_HEIGHT)
            .setParameter(mappingMode.saveVal)
            .setLabel("SAVE")
            .addToContainer(this);
    }


}
