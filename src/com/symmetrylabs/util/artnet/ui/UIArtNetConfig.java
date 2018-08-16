package com.symmetrylabs.util.artnet.ui;

import com.symmetrylabs.util.artnet.ArtNetEngine;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIIntegerBox;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import processing.core.PConstants;

public class UIArtNetConfig extends UICollapsibleSection {

    private static final int HEIGHT = 111;

    public UIArtNetConfig(final UI ui, final ArtNetEngine artNetEngine, float x, float y, float w) {
        super(ui, x, y, w, HEIGHT);
        setTitle("Art-Net/DMX");

        UI2dContainer border = (UI2dContainer) new UI2dContainer(0, 0, getContentWidth(), getContentHeight())
                        .setBackgroundColor(ui.theme.getDarkBackgroundColor())
                        .setBorderRounding(4)
                        .addToContainer(this);

        float yp = 4;

        new UILabel(56, yp, 64, 12).setLabel("Universe").setTextAlignment(PConstants.CENTER, PConstants.CENTER).addToContainer(border);
        yp += 15;

        new UILabel(6, yp+2, 46, 12).setLabel("Input").setTextAlignment(PConstants.LEFT, PConstants.CENTER).addToContainer(border);
        new UIIntegerBox(56, yp, 64, 16).setParameter(artNetEngine.inputUniverse).setMappable(false).addToContainer(border);
        new UIButton(124, yp, 16, 16).setParameter(artNetEngine.inputEnabled).setMappable(false).setBorderRounding(4).addToContainer(border);
        yp += 20;

        new UILabel(6, yp+2, 46, 12).setLabel("Output").setTextAlignment(PConstants.LEFT, PConstants.CENTER).addToContainer(border);
        new UIIntegerBox(56, yp, 64, 16).setParameter(artNetEngine.outputUniverse).setMappable(false).addToContainer(border);
        new UIButton(124, yp, 16, 16).setParameter(artNetEngine.outputEnabled).setMappable(false).setBorderRounding(4).addToContainer(border);
        yp += 20;

        border.setHeight(yp);

        float yp2 = 4;
        new UILabel(156, yp2, 64, 12).setLabel("SubNet").setTextAlignment(PConstants.CENTER, PConstants.CENTER).addToContainer(border);
        yp2 += 15;
        new UIIntegerBox(156, yp2, 64, 16).setParameter(artNetEngine.subNet).addToContainer(border);

        border = (UI2dContainer) new UI2dContainer(0, border.getY() + border.getHeight() + 4, getContentWidth(), getContentHeight())
                        .setBackgroundColor(ui.theme.getDarkBackgroundColor())
                        .setBorderRounding(4)
                        .addToContainer(this);

        yp = 4;
        new UILabel(6, yp+2, 46, 12).setLabel("Network").setTextAlignment(PConstants.LEFT, PConstants.CENTER).addToContainer(border);
        new UITextBox(56, yp, 108, 16).setParameter(artNetEngine.networkAddress).setCanEdit(false).addToContainer(border);
        yp += 20;

        border.setHeight(yp);
    }
}
