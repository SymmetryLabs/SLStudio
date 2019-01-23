package com.symmetrylabs.slstudio.pattern.explosions;

import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import static processing.core.PApplet.dist;
import static processing.core.PApplet.max;

/**
 * Contains the current state of an explosion.
 */
public class L8onExplosion implements LXParameterListener {
    float center_x;
    float center_y;
    float center_z;
    float stroke_width;
    float hue_value;
    float chill_time;
    float time_chillin;

    private BooleanParameter trigger_parameter;
    public LXModulator radius_modulator;
    private boolean radius_modulator_triggered = false;

    public L8onExplosion(LXModulator radius_modulator, BooleanParameter trigger_parameter, float stroke_width, float center_x, float center_y, float center_z) {
        this.setRadiusModulator(radius_modulator, stroke_width);

        this.trigger_parameter = trigger_parameter;
        this.trigger_parameter.addListener(this);

        this.center_x = center_x;
        this.center_y = center_y;
        this.center_z = center_z;
    }

    public void setChillTime(float chill_time) {
        this.chill_time = chill_time;
        this.time_chillin = 0;
    }

    public boolean isChillin(float deltaMs) {
        this.time_chillin += deltaMs;

        return time_chillin < this.chill_time;
    }

    public float distanceFromCenter(float x, float y, float z) {
        return dist(this.center_x, this.center_y, this.center_z, x, y, z);
    }

    public void setRadiusModulator(LXModulator radius_modulator, float stroke_width) {
        this.radius_modulator = radius_modulator;
        this.stroke_width = stroke_width;
        this.radius_modulator_triggered = false;
    }

    public void setCenter(float x, float y, float z) {
        this.center_x = x;
        this.center_y = y;
        this.center_z = z;
    }

    public void explode() {
        this.radius_modulator_triggered = true;
        this.radius_modulator.trigger();
    }

    public boolean hasExploded() {
        return this.radius_modulator_triggered;
    }

    public boolean isExploding() {
        if (this.radius_modulator == null) {
            return false;
        }

        return this.radius_modulator.isRunning();
    }

    public boolean isFinished() {
        if (this.radius_modulator == null) {
            return true;
        }

        return !this.radius_modulator.isRunning();
    }

    public boolean onExplosion(float x, float y, float z) {
        float current_radius = this.radius_modulator.getValuef();
        float min_dist = max(0.0f, current_radius - (stroke_width / 2.0f));
        float max_dist = current_radius + (stroke_width / 2.0f);
        float point_dist = this.distanceFromCenter(x, y, z);

        return (point_dist >= min_dist && point_dist <= max_dist);
    }

    public void onParameterChanged(LXParameter parameter) {
        if (!(parameter == this.trigger_parameter)) { return; }

        if (this.trigger_parameter.getValueb() && this.isFinished()) {
            this.setChillTime(0);
        }
    }
}
