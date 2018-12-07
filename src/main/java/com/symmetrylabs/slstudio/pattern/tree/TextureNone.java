package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;

import com.symmetrylabs.shows.tree.TreeModel;

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