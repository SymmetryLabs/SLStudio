package com.symmetrylabs.slstudio.pattern;

import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.transform.LXVector;
import heronarts.lx.LXUtils;

public class Voronoi extends SLPattern {
    public CompoundParameter speed = new CompoundParameter("SPEED", 1.8, 0, 10);
    public CompoundParameter width = new CompoundParameter("WIDTH", 0.2, 0.1, 1);
    public CompoundParameter hue = new CompoundParameter("HUE", 0, 0, 360);
    public DiscreteParameter num = new DiscreteParameter("NUM", 14, 5, 28);
    private List<Site> sites = new ArrayList<Site>();
    public float xMaxDist = model.xMax - model.xMin;
    public float yMaxDist = model.yMax - model.yMin;
    public float zMaxDist = model.zMax - model.zMin;

    class Site {
        float xPos = 0;
        float yPos = 0;
        float zPos = 0;
        LXVector velocity = new LXVector(0,0,0);

        public Site() {
                xPos = (float)LXUtils.random((double)model.xMin, (double)model.xMax);
                yPos = (float)LXUtils.random((double)model.yMin, (double)model.yMax);
                zPos = (float)LXUtils.random((double)model.zMin, (double)model.zMax);
                velocity = new LXVector((float)LXUtils.random(-1, 1), (float)LXUtils.random(-1, 1), (float)LXUtils.random(-1, 1));
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
        addParameter(speed);
        addParameter(width);
        addParameter(hue);
        addParameter(num);
    }

    public void run(double deltaMs) {
        for (LXPoint p: model.points) {
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

                if (Math.abs(dy) < yMaxDist * calcRestraintConst &&
                        Math.abs(dx) < xMaxDist * calcRestraintConst &&
                        Math.abs(dz) < zMaxDist * calcRestraintConst) { //restraint on calculation
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
                (palette.getHuef() + hue.getValuef()) % 360,
                100,
                (float)Math.max(0.0, Math.min(100, 100 - Math.sqrt(nextMinDistSq - minDistSq) / lineWidth))
            );
        }
        for (Site site: sites) {
            site.move(speed.getValuef());
        }
    }
}