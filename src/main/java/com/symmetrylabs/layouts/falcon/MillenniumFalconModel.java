package com.symmetrylabs.layouts.falcon;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

import java.util.ArrayList;
import java.util.List;

import static com.symmetrylabs.util.MathConstants.HALF_PI;
import static com.symmetrylabs.util.MathConstants.PI;

public class MillenniumFalconModel extends StripsModel<Strip> {
    String id;
    float x;
    float y;
    float z;
    float xRot;
    float yRot;
    float zRot;
    FalconStrip.Metrics metrics;

    public MillenniumFalconModel(String id, float x, float y, float z, float xRot, float yRot, float zRot, FalconStrip.Metrics metrics, LXTransform transform) {

        super(new Fixture(id, x, y, z, xRot, yRot, zRot, metrics, transform));

        this.id = id;
        this.metrics = metrics;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xRot = xRot;
        this.yRot = yRot;
        this.zRot = zRot;
        Fixture fixture = (Fixture) this.fixtures.get(0);
        for (int i = 0; i < strips.size(); i++) {
            //stripsArr[i] = strips.get(i);
            this.strips.add(strips.get(i));
        }

        //this.strips.addAll(fixture.strips);

        //this.stripsArr = new Strip[strips.length];
//        for (int i = 0; i < strips.length; i++) {
//            stripsArr[i] = strips[i];
//        }
//        for (Strip strip : strips) {
//            this.strips.add(strip);
//        }
    }

        private static class Fixture extends LXAbstractFixture {
//            private final List<Strip> strips = new ArrayList<>();

            private final List<FalconStrip> falconstrips = new ArrayList<>();

            private Fixture(String id, float x, float y, float z, float xRot, float yRot, float zRot, FalconStrip.Metrics metrics, LXTransform transform) {
                //transform.push();


                transform.push();
                transform.translate(x, y, z);
                transform.rotateX(xRot * PI / 180f);
                transform.rotateY(yRot * PI / 180f);
                transform.rotateZ(zRot * PI / 180f);

                for (int iStrip = 0; iStrip < metrics.NUM_STRIPS; iStrip++) {

                    // /if (strip != null) {
//                        transform.translate(strip.);
//                        transform.rotateZ(HALF_PI);
//            FalconStrip.Metrics metrics = config.metrics;
//            //int numPoints = config.numPoints;
//            //float spacing = config.spacing;
//
                    FalconStrip falconstrip = new FalconStrip(Integer.toString(iStrip), transform, metrics);
                    falconstrips.add(falconstrip);

//            FalconStrip.Metrics metrics = config.metrics;
//            //int numPoints = config.numPoints;
//            //float spacing = config.spacing;
//
//            FalconStrip falconstrip = new FalconStrip(id, x, y, z, xRot, yRot, zRot, globalTransform, metrics);
//            falconstrips.add(falconstrip);
                    transform.translate(falconstrip.metrics.length, 0, falconstrip.metrics.POINT_SPACING ); //0.5f

                    transform.rotateZ(PI);
                        for (LXPoint point : falconstrip.points) {

                            this.points.add(point);

                        }
                    }
                }
            }


    }




