package com.symmetrylabs.layouts.falcon;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

import java.util.ArrayList;
import java.util.List;

import static com.symmetrylabs.util.MathConstants.HALF_PI;

public class MillenniumFalconModel extends StripsModel<Strip> {

    protected final List<FalconStrip> falconstrips = new ArrayList<>();


    //protected final Strip[] stripsArr = new Strip[0];


//    public final Metrics metrics;
//
//    public static class Metrics {
//        final Strip.Metrics numPoints;
//        final Strip.Metrics spacing;
//        public Metrics(Strip.Metrics numPoints, Strip.Metrics spacing)
//            this.numPoints = numPoints;
//          this.spacing = spacing;
//    }

    public MillenniumFalconModel(List<FalconStrip> falconstrips, LXTransform transform) {

        super(new Fixture(falconstrips, transform));

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

            private Fixture(List<FalconStrip> falconstrips, LXTransform transform) {
                //transform.push();

                for (FalconStrip strip : falconstrips) {
                    if (strip != null) {
//                        transform.translate(strip.);
//                        transform.rotateZ(HALF_PI);


                        for (LXPoint point : strip.points) {

                            this.points.add(point);

                        }
                    }
                }
            }
        }

    }




