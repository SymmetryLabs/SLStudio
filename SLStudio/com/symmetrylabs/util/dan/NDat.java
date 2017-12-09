package com.symmetrylabs.util.dan;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class NDat {
    float xz, yz, zz, hue, speed, angle, den;
    float xoff, yoff, zoff;
    float sinAngle, cosAngle;
    boolean isActive;

    NDat() {
        isActive = false;
    }

    boolean Active() {
        return isActive;
    }

    void set(float _hue, float _xz, float _yz, float _zz, float _den, float _speed, float _angle) {
        isActive = true;
        hue = _hue;
        xz = _xz;
        yz = _yz;
        zz = _zz;
        den = _den;
        speed = _speed;
        angle = _angle;
        xoff = random(100e3);
        yoff = random(100e3);
        zoff = random(100e3);

    }
}
