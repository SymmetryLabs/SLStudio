//package com.symmetrylabs.slstudio.ui;
//
//import heronarts.lx.LX;
//import heronarts.p3lx.ui.UI;
//import heronarts.p3lx.ui.UI2dContainer;
//import heronarts.p3lx.ui.component.UILabel;
//import heronarts.p3lx.ui.component.UISlider;
//
//
//public class UIPowerCap extends UI2dContainer {
//    public UIPowerCap(UI ui, final LX lx, float x, float y, float w) {
//        super(x, y, w, 20);
//        setBackgroundColor(0xff404000); //ui.theme.getDeviceBackgroundColor()
//        setBorderRounding(4);
//
//        new UILabel(5, 2, 50, 12)
//            .setLabel("MaxPower")
//            .addToContainer(this);
//
//        new UISlider(45, 0, 130, 20)
//            .setParameter(lx.engine.powerCap)
//            .setShowLabel(false)
//            .addToContainer(this);
//    }
//}
