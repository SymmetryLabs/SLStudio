package com.symmetrylabs.shows.summerstage;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.MathUtils;
import com.symmetrylabs.util.NoiseUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.LXPattern;
import heronarts.lx.LXUtils;
import heronarts.lx.transform.LXVector;

import java.util.List;
import java.util.LinkedList;
import processing.core.PImage;
import java.util.Iterator;

import java.lang.Math;
import static processing.core.PApplet.*;

import heronarts.lx.color.*;

import java.util.*;

import com.symmetrylabs.shows.cubes.*;
import com.symmetrylabs.slstudio.SLStudio;


public class UnusedFinder extends SLPattern<CubesModel> {
    final CubesShow show;

    public UnusedFinder(LX lx) {
        super(lx);

        show = CubesShow.getInstance(lx);

     
    }

    public void run(double deltaMs) {
        HashSet<String> used = new HashSet();
        for (CubesController c : show.controllers) {
            used.add(c.id);
        }

        for (CubesModel.Cube c : model.getCubes()) {
                CubesModel.DoubleControllerCube c2 = (CubesModel.DoubleControllerCube) c;
                int numOut = 0;
                if (!used.contains(c2.idA)) {
                    numOut++;
                }
                if (!used.contains(c2.idB)) {
                    numOut++;
                }
                int col = LXColor.BLACK;
                if (numOut == 1) {
                    col = LXColor.BLUE;
                }
                if (numOut == 2) {
                    col = LXColor.RED;
                }
                setColor(c, col);
        }

    }
        
}