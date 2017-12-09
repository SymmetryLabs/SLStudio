package com.symmetrylabs.util.dan;

import static com.symmetrylabs.util.Utils.random;


public class NDat {
    public float xz, yz, zz, hue, speed, angle, den;
    public float xoff, yoff, zoff;
    public float sinAngle;
    public float cosAngle;
    public boolean isActive;

    public NDat() {
        isActive = false;
    }

    public boolean Active() {
        return isActive;
    }

    public void set(float _hue, float _xz, float _yz, float _zz, float _den, float _speed, float _angle) {
        isActive = true;
        hue = _hue;
        xz = _xz;
        yz = _yz;
        zz = _zz;
        den = _den;
        speed = _speed;
        angle = _angle;
        xoff = random(100e3f);
        yoff = random(100e3f);
        zoff = random(100e3f);

    }
}
