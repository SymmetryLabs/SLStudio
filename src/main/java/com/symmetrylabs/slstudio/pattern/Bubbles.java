package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.QuadraticEnvelope;
import heronarts.lx.parameter.CompoundParameter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Bubbles extends LXPattern {

    private final float MAX_VELOCITY = 1.5f;
    //private final float MAX_SIZE = 25;
    private final float MAX_SPROUT_TIME = 150;
    private final Random rand = new Random();

    private final CompoundParameter rate = new CompoundParameter("Number", 0.3);
    private final CompoundParameter popFrequency = new CompoundParameter("PopFrq", 30, 30, 500);
    private final CompoundParameter speed = new CompoundParameter("Speed", 0.01, 0.25, 1.0);
    private final CompoundParameter saturation = new CompoundParameter("Sat", 50, 0, 100);
    private final CompoundParameter maxBubbleSize = new CompoundParameter("Size", 20, 10, 50);
    private final CompoundParameter transparency = new CompoundParameter("Trans", 9, 0.1, 25);
    
    private final CompoundParameter zDep = new CompoundParameter("zDep", 2, 0.1, 5);

    private final List<Bubble> bubbles = new LinkedList<Bubble>();
    float leftoverMs = 0;

    public Bubbles(LX lx) {
        super(lx);

        addParameter(rate);
        addParameter(speed);
        addParameter(saturation);
        addParameter(maxBubbleSize);
        addParameter(transparency);
        addParameter(popFrequency);
        addParameter(zDep);
    }

    public void run(final double deltaMs) {
        leftoverMs += deltaMs;

        float msPerBubble = 20000 / ((rate.getValuef() + .01f) * 100);
        while (leftoverMs > msPerBubble) {
            leftoverMs -= msPerBubble;
            bubbles.add(new Bubble());
        }

        // if pop frequency is at "normalized zero", we will use sensor data
        // otherwise use as a frequency parameter for autopopping
        float popFreqV = popFrequency.getValuef();
        if (popFreqV > 30) {
            int indexToPop = (int) (bubbles.size() * popFreqV * rand.nextFloat());
            if (indexToPop < bubbles.size())
                bubbles.get(indexToPop).pop();
        }

        bubbles.parallelStream().forEach(bubble -> {
            bubble.run(deltaMs);
        });

        model.getPoints().parallelStream().forEach(point -> {
            colors[point.index] = 0;
            for (Bubble bubble : bubbles) {
                bubble.paint(point);
            }
        });

        Iterator<Bubble> i = bubbles.iterator();
        while (i.hasNext()) {
            Bubble bubble = i.next();
            if (bubble.isDead)
                i.remove();
        }
    }

    private class Bubble {
        float x = (rand.nextFloat() * model.xRange) + model.xMin;
        float y = (rand.nextFloat() * model.yRange) + model.yMin;
        float z = (rand.nextFloat() * model.zRange) + model.zMin;

        float xVelocity = rand.nextFloat() * rand.nextFloat() * MAX_VELOCITY;
        float yVelocity = rand.nextFloat() * rand.nextFloat() * MAX_VELOCITY;
        float zVelocity = rand.nextFloat() * rand.nextFloat() * MAX_VELOCITY;

        float size = rand.nextFloat() * maxBubbleSize.getValuef() + 4.0f;
        float sproutTime = rand.nextFloat() * MAX_SPROUT_TIME;
        float hue = rand.nextFloat() * 360;

        boolean hasGrown = false;
        boolean isPopped = false;
        boolean isDead = false;
        float radius = 0;
        int counter = 0;

        QuadraticEnvelope yMod = new QuadraticEnvelope(-0.2, 0.2, 2000);
        QuadraticEnvelope pop = new QuadraticEnvelope(0, 15, 200);

        public Bubble() {
            yMod.setEase(QuadraticEnvelope.Ease.BOTH);
            pop.setEase(QuadraticEnvelope.Ease.OUT);
            addModulator(yMod).start();
            addModulator(pop);
        }

        public void run(double deltaMs) {
            float xVel = xVelocity * speed.getValuef();
            float yVel = yVelocity * speed.getValuef();
            float zVel = zVelocity * speed.getValuef();

            if (!hasGrown) {
                x += xVel * 0.2f;
                y += yVel * 0.2f;
                z += zVel * 0.2f;
                radius = (float) counter / sproutTime * size;
                radius *= radius;
                if (radius > size)
                    hasGrown = true;
            } else {
                x += xVel;
                y += yVel + yMod.getValuef();
                z += zVel;
                radius = size;
            }
            counter++;

            if (isPopped) {
                if (radius < size * 1.5)
                    radius += pop.getValuef();
            }

            if (x > model.xMax + radius || x < model.xMin - radius
                || y > model.yMax + radius || y < model.yMin - radius) {
                isDead = true;
            }
        }

        public float distanceTo(LXPoint p) {
            return (float) Math.sqrt(Math.pow(Math.abs(x - p.x), 2) + Math.pow(
                Math.abs(y - p.y),
                2
            ) + Math.pow(Math.abs(z - p.z), 2));
        }

        public void paint(LXPoint p) {
            if (Math.abs(p.x - x) > radius
                || Math.abs(p.y - y) > radius
                || Math.abs(p.z - z) > radius * zDep.getValuef()) {
                return;
            }

            float distance = (float) LXUtils.distance((double) p.x, (double) p.y, (double) x, (double) y);
            //float distance = distanceTo(p);
            if (distance > radius) return;

            float gradient = 100 * (float) Math.pow(distance / radius, 6);
            float brightness = 0;
            float edge = size * 0.85f;
            float falloff = (float) Math.pow(Math.abs(distance - edge) / (size - edge), 1) * 100;

            brightness = Math.max(0, Math.min(100, gradient) - ((pop.getValuef() / 15) * 100))
                + transparency.getValuef();
            // if (hasGrown && distance > edge) {
            //   if (isPopped)
            //     falloff = (float)Math.pow(falloff, 1);
            //   brightness -= falloff;
            // }

            if (brightness < 5) brightness = 0; // ugh, fix this (popped bubbles dont come out to zero)

            colors[p.index] = LXColor.blend(
                colors[p.index],
                lx.hsb(
                    hue + 1.7f * ((x - p.x) + (y - p.y)),
                    saturation.getValuef(), //Math.min(100, gradient*1.2f+5.0f),
                    brightness
                ), LXColor.Blend.ADD
            );
        }

        public void pop() {
            if (isPopped) return;
            isPopped = true;
            pop.trigger();
        }
    }
}
