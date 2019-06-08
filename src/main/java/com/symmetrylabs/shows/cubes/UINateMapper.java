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
    private int yOff = 0;
    private int ITEM_SIZE = 20;
    private NateMappingMode mappingMode;

    private static final int ITEM_HEIGHT = 20;

    public UINateMapper(LX lx, UI ui, float x, float y, float w) {
        super(ui, x, y, w, 142);


        mappingMode = NateMappingMode.getInstance(lx);

        setTitle("NateMapper");
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
        yOff += ITEM_SIZE;


//        final UITextBox controllerIDa = new UITextBox(0, 20, getContentWidth()/2, 20).setParameter(mappingMode.selectedModelFixture);
        final UIIntegerBox controllerIDa = new UIIntegerBox(0, yOff, getContentWidth() / 2, ITEM_HEIGHT) {
        }.setParameter(mappingMode.selectedModelFixture);
        controllerIDa.addToContainer(this);
        yOff += ITEM_SIZE;


        final UIKnob componentLabel = new UIKnob(0, yOff+10, getContentWidth(), ITEM_HEIGHT);
        componentLabel.setParameter(mappingMode.selectedModelFixture);
        componentLabel.addToContainer(this);
        yOff += ITEM_SIZE;
    }


}
