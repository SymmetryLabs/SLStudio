package com.symmetrylabs.util;

import heronarts.lx.midi.MidiTime;

import java.awt.image.BufferedImage;

public class Hunk {
    public BufferedImage img;
    public int hunkIndex;

   public Hunk(BufferedImage buffer_in, int hunkIndex){
       this.img = buffer_in;
       this.hunkIndex = hunkIndex;
   }
}
