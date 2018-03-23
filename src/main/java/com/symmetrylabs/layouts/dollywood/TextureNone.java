package com.symmetrylabs.layouts.dollywood;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;


public class TextureNone extends TexturePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }
 
    public TextureNone(LX lx) {
        super(lx);
        setColors(0xffffffff);
    }
    
    public void run(double deltaMs) {}
}