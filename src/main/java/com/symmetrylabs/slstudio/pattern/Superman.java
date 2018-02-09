package com.symmetrylabs.slstudio.pattern;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.slstudio.pattern.base.DPat;


public class Superman extends DPat {
        //PImage img = loadImage("data/superman.png");

        CompoundParameter xSizeOuter = new CompoundParameter("xSizeB", 0.5);
        CompoundParameter ySizeOuter = new CompoundParameter("ySizeB", 0.5);
        CompoundParameter xTrimOuter = new CompoundParameter("xTrB");
        CompoundParameter yTrimOuter = new CompoundParameter("yTrB");

        CompoundParameter xSizeMiddle = new CompoundParameter("xSizeR", 0.5);
        CompoundParameter ySizeMiddle = new CompoundParameter("ySizeR", 0.5);
        CompoundParameter xTrimMiddle = new CompoundParameter("xTrR");
        CompoundParameter yTrimMiddle = new CompoundParameter("yTrR");

        CompoundParameter xSizeInner = new CompoundParameter("xSizeY", 0.5);
        CompoundParameter ySizeInner = new CompoundParameter("ySizeY", 0.5);
        CompoundParameter xTrimInner = new CompoundParameter("xTrY");
        CompoundParameter yTrimInner = new CompoundParameter("yTrY");

        public Superman(LX lx) {
                super(lx);
                //img.loadPixels();

                addParameter(xSizeOuter);
                addParameter(ySizeOuter);
                addParameter(xTrimOuter);
                addParameter(yTrimOuter);

                addParameter(xSizeMiddle);
                addParameter(ySizeMiddle);
                addParameter(xTrimMiddle);
                addParameter(yTrimMiddle);

                addParameter(xSizeInner);
                addParameter(ySizeInner);
                addParameter(xTrimInner);
                addParameter(yTrimInner);
        }

        public void StartRun(double deltaMs) {

        }

        public int CalcPoint(PVector p) {
            int c = 0;
            // int x, y;

            // x = (int)((p.x-(model.xRange*xTrimOuter.getValuef())) / ((model.xMax - model.xMin)*xSizeOuter.getValuef()) * img.width);
            // y = (int)((p.y-(model.xRange*yTrimOuter.getValuef())) / ((model.yMax - model.yMin)*ySizeOuter.getValuef()) * img.height);
            // if (img.get(x, y) != 0) c = LXColor.BLUE;

            // x = (int)((p.x-(model.xRange*xTrimMiddle.getValuef())) / ((model.xMax - model.xMin)*xSizeMiddle.getValuef()) * img.width);
            // y = (int)((p.y-(model.xRange*yTrimMiddle.getValuef())) / ((model.yMax - model.yMin)*ySizeMiddle.getValuef()) * img.height);
            // if (img.get(x, y) != 0) c = LXColor.RED;

            // x = (int)((p.x-(model.xRange*xTrimInner.getValuef())) / ((model.xMax - model.xMin)*xSizeInner.getValuef()) * img.width);
            // y = (int)((p.y-(model.xRange*yTrimInner.getValuef())) / ((model.yMax - model.yMin)*ySizeInner.getValuef()) * img.height);
            // if (img.get(x, y) != 0) c = lx.hsb(61, 100, 100);

            return c;
        }

}