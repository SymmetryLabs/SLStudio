package com.symmetrylabs.util;

import heronarts.lx.midi.MidiTime;

import java.awt.image.BufferedImage;

public class BufferWithIndex {
    public BufferedImage img;
//    public MidiTime mt;
    public int hunkIndex;

   public BufferWithIndex(BufferedImage buffer_in, int mt_in){
       img = buffer_in;
       hunkIndex = mt_in;
   }
}
