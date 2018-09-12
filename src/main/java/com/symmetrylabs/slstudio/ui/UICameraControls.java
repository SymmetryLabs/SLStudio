package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.slstudio.SLStudio;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContext;
import heronarts.p3lx.ui.UI3dContext;
import heronarts.p3lx.ui.component.UIButton;
import processing.core.PConstants;
import processing.core.PGraphics;

public class UICameraControls extends UI2dContext implements LXParameterListener {
    private static final float PAD = 2;
    private static final float BW = 25;
    private static final float BH = 25;
    private static final int BROWS = 5;
    private static final int BCOLS = 2;
    private static final float HEIGHT = BROWS * BH + (BROWS + 1) * PAD;
    private static final float WIDTH = BCOLS * BW + (BCOLS + 1) * PAD;
    private static final double ISO_ELEVATION = Math.asin(1 / Math.sqrt(3));

    private final UI3dContext parent;

    private final BooleanParameter gotoUp = makeDirParam("up");
    private final BooleanParameter gotoDown = makeDirParam("down");
    private final BooleanParameter gotoLeft = makeDirParam("left");
    private final BooleanParameter gotoRight = makeDirParam("right");
    private final BooleanParameter gotoFront = makeDirParam("front");
    private final BooleanParameter gotoBack = makeDirParam("back");
    private final BooleanParameter gotoIsoLeft = makeDirParam("iso-left");
    private final BooleanParameter gotoIsoRight = makeDirParam("iso-right");

    public UICameraControls(UI ui, UI3dContext parent) {
        super(ui, 0, 0, WIDTH, HEIGHT);
        this.parent = parent;
        reposition();

        float x = PAD;
        float y = PAD;

        makeDirButton("F", gotoFront, x, y);
        x += BW + PAD;
        makeDirButton("B", gotoBack, x, y);
        x = PAD;
        y += BH + PAD;

        makeDirButton("D", gotoDown, x, y);
        x += BW + PAD;
        makeDirButton("U", gotoUp, x, y);
        x = PAD;
        y += BH + PAD;

        makeDirButton("L", gotoLeft, x, y);
        x += BW + PAD;
        makeDirButton("R", gotoRight, x, y);
        x = PAD;
        y += BH + PAD;

        makeDirButton("IL", gotoIsoLeft, x, y);
        x += BW + PAD;
        makeDirButton("IR", gotoIsoRight, x, y);
        x = PAD;
        y += BH + PAD;

        new UIButton(x, y, 2 * BW + PAD, BH)
            .setIcon(SLStudio.applet.loadImage("ortho.png"))
            .setParameter(parent.ortho)
            .setFont(SLStudio.MONO_FONT.getFont())
            .addToContainer(this);
    }

    private UIButton makeDirButton(String label, BooleanParameter param, float x, float y) {
        UIButton b = new UIButton(x, y, BW, BH);
        b.setParameter(param);
        b.setFont(SLStudio.MONO_FONT.getFont());
        b.setIcon(SLStudio.applet.loadImage(param.getLabel() + ".png"));
        b.addToContainer(this);
        return b;
    }

    private BooleanParameter makeDirParam(String name) {
        BooleanParameter p = new BooleanParameter(name, false);
        p.setMode(BooleanParameter.Mode.MOMENTARY);
        p.setShouldSerialize(false);
        p.addListener(this);
        return p;
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        pg.textFont(SLStudio.MONO_FONT.getFont());
        pg.noStroke();
        pg.textAlign(PConstants.CENTER, PConstants.CENTER);

        pg.clear();
        pg.fill(0x22FFFFFF);
        pg.rectMode(PConstants.CORNERS);
        pg.rect(0, 0, width, height);
    }

    public void reposition() {
        setPosition(
            parent.getX() + parent.getWidth() - width,
            parent.getY() + parent.getHeight() - height);
    }

    private void gotoLatLon(double theta, double phi) {
        parent.setTheta(theta);
        parent.setPhi((float) phi);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == gotoUp) {
            gotoLatLon(0, -Math.PI / 2f);
        } else if (p == gotoDown) {
            gotoLatLon(0, Math.PI / 2f);
        } else if (p == gotoLeft) {
            gotoLatLon(-Math.PI / 2f, 0);
        } else if (p == gotoRight) {
            gotoLatLon(Math.PI / 2f, 0);
        } else if (p == gotoFront) {
            gotoLatLon(0, 0);
        } else if (p == gotoBack) {
            gotoLatLon(Math.PI, 0);
        } else if (p == gotoIsoLeft) {
            gotoLatLon(-Math.PI / 4, ISO_ELEVATION);
        } else if (p == gotoIsoRight) {
            gotoLatLon(Math.PI / 4, ISO_ELEVATION);
        }
    }
}
