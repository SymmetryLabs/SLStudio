package com.symmetrylabs.util.hardware.powerMon;

public class MetaSample {
    // how to initialize two dimensional array in Java
    // using for loop
    int NUM_CHANNEL = 8;
    int UINT16_PER_CHANNEL = 4;
    int[][] allSample = new int[NUM_CHANNEL][UINT16_PER_CHANNEL];
    public int[] analogSampleArray = new int[NUM_CHANNEL];

    public int powerOnStateMask = 0;

    public MetaSample(){
        for (int i = 0; i < NUM_CHANNEL; i++) {
            for (int j = 0; j < UINT16_PER_CHANNEL; j++) {
                allSample[i][j] = 0;
            }
        }
    }
}
