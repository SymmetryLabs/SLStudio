package com.symmetrylabs.slstudio.pattern;

import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.transform.LXVector;

import static com.symmetrylabs.util.MathUtils.*;
import static com.symmetrylabs.util.MathConstants.*;

import com.symmetrylabs.shows.summerstage.SummerStageShow;

public class Voronoi extends LXPattern {
    public static final String GROUP_NAME = SummerStageShow.SHOW_NAME;

    final CompoundParameter hue = new CompoundParameter("HUE", 0, 0, 360);
    final CompoundParameter sat = new CompoundParameter("SAT", 100, 0, 100);

    final CompoundParameter speed = new CompoundParameter("SPEED", 1.8, 0, 10);
    final CompoundParameter width = new CompoundParameter("WIDTH", 0.2, 0.1, 1);
    final DiscreteParameter num = new DiscreteParameter("NUM", 14, 5, 28);
    private final List<Site> sites = new ArrayList<Site>();
    private float xMaxDist = model.xMax - model.xMin;
    private float yMaxDist = model.yMax - model.yMin;
    private float zMaxDist = model.zMax - model.zMin;

    class Site {
        float xPos = 0;
        float yPos = 0;
        float zPos = 0;
        LXVector velocity = new LXVector(0,0,0);

        public Site() {
                xPos = random(model.xMin, model.xMax);
                yPos = random(model.yMin, model.yMax);
                zPos = random(model.zMin, model.zMax);
                velocity = new LXVector(random(-1, 1), random(-1, 1), random(-1, 1));
        }

        public void move(float speed) {
            xPos += speed * velocity.x;
            if ((xPos < model.xMin - 20) || (xPos > model.xMax + 20)) {
                velocity.x *= -1;
            }
            yPos += speed * velocity.y;
            if ((yPos < model.yMin - 20) || (yPos > model.yMax + 20)) {
                velocity.y *= -1;
            }
            zPos += speed * velocity.z;
            if ((zPos < model.zMin - 20) || (zPos > model.zMax + 20)) {
                velocity.z *= -1;
            }
        }
    }

    public Voronoi(LX lx) {
        super(lx);
        addParameter(hue);
        addParameter(sat);
        addParameter(speed);
        addParameter(width);
        addParameter(num);
    }

    public void run(double deltaMs) {
        for (LXVector p : getVectors()) {
            float numSites = num.getValuef();
            float lineWidth = width.getValuef();

            while(sites.size()>numSites){
                sites.remove(0);
            }

            while(sites.size()<numSites){
                sites.add(new Site());
            }

            float minDistSq = 10000;
            float nextMinDistSq = 10000;
            float calcRestraintConst = 20 / (numSites + 15);
            lineWidth = lineWidth * 40 / (numSites + 20);

            for (Site site : sites) {
                float dx = site.xPos - p.x;
                float dy = site.yPos - p.y;
                float dz = site.zPos - p.z;

                if (abs(dy) < yMaxDist * calcRestraintConst &&
                        abs(dx) < xMaxDist * calcRestraintConst &&
                        abs(dz) < zMaxDist * calcRestraintConst) { //restraint on calculation
                    float distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq < nextMinDistSq) {
                        if (distSq < minDistSq) {
                            nextMinDistSq = minDistSq;
                            minDistSq = distSq;
                        } else {
                            nextMinDistSq = distSq;
                        }
                    }
                }
            }
            colors[p.index] = lx.hsb(
                hue.getValuef(),
                sat.getValuef(),
                max(0f, min(100, 100 - sqrt(nextMinDistSq - minDistSq) / lineWidth))
            );
        }
        for (Site site: sites) {
            site.move(lx.engine.speed.getValuef() * speed.getValuef());
        }
    }
}
