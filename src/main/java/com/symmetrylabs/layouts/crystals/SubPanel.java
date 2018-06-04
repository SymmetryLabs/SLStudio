package com.symmetrylabs.layouts.crystals;

import com.symmetrylabs.layouts.icicles.Icicle;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

import java.util.ArrayList;
import java.util.List;

import static com.symmetrylabs.util.MathConstants.PI;

public class SubPanel extends SLModel {

    public final String id;
    //public final List<float> stringLengths = new ArrayList<>();
    public final float stringLengths[] = new float[]{};

    //public final Metrics metrics;
    public SubPanel(String id, float x, float y, float z, float rx, float ry, float rz, float[] stringLengths, LXTransform t) {

        super(new Fixture(id, x, y, z, rx, ry, rz, stringLengths, t));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        this.id = id;
//        this.points.addAll(fixture.points);
//

    }


    private static class Fixture extends LXAbstractFixture {

//        private final List<LXPoint> points = new ArrayList<>();

        private Fixture(String id, float x, float y, float z, float xr, float yr, float zr, float[] stringLengths, LXTransform t) {
            float CRYSTAL_SPACING = 3.66666666f;
            t.push();
            t.translate(x, y, z);
            t.rotateX(xr * PI / 180f);
            t.rotateY(yr * PI / 180f);
            t.rotateZ(zr * PI / 180f);
            System.out.println("zr: " + zr);
            System.out.println("yr: " + yr);
            System.out.println("xr: " + xr);

            // do transforms1

            List<LXPoint> points = new ArrayList<>();
            t.push();
            t.translate(CRYSTAL_SPACING, CRYSTAL_SPACING, stringLengths[0]);
            LXPoint crystal1 = new LXPoint(t.x() ,t.y() , t.z());
            t.pop();
            t.push();
            t.translate(CRYSTAL_SPACING*3, CRYSTAL_SPACING*2, stringLengths[1]);
            LXPoint crystal2 = new LXPoint(t.x(), t.y(), t.z());
            t.pop();

            t.push();
            t.translate(CRYSTAL_SPACING, CRYSTAL_SPACING*3, stringLengths[2]);
            LXPoint crystal3 = new LXPoint(t.x(), t.y() , t.z());
            t.pop();

//            for (int iPoint = 0; iPoint< 3; iPoint++) {




                this.points.add(crystal1);
              this.points.add(crystal2);
              this.points.add(crystal3);

//            #for (LXPoint point : strip.points) {
//                    this.points.add(point);
//                }


            t.pop();
        }
    }

}
