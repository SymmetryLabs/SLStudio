package com.symmetrylabs.layouts.tree.config;


public class TwigConfig {
    public float x;
    public float y;
    public float z;
    public float theta;
    public float tilt;
    public int wiringIndex;

    public TwigConfig(float x, float y, float z, float theta, float tilt) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.theta = theta;
        this.tilt = tilt;
        this.wiringIndex = 0;
    }
}