package com.symmetrylabs.layouts.crystals;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.symmetrylabs.util.MathConstants.PI;


public class Panel extends SLModel {


    public final String id;
    protected final List<SubPanel> subpanels = new ArrayList<>();
    protected final Map<String, SubPanel> subpanelTable = new HashMap<>();

    public Panel(String id, float x, float y, float z, float rx, float ry, float rz, LXTransform t) {
        super(new Fixture(id, x, y, z, rx, ry, rz, t));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        this.id = id;
        this.subpanels.addAll(fixture.subpanels);
//      this.points.addAll(fixture.points);


    }

    private static class Fixture extends LXAbstractFixture {
        private final String id;
        private final List<SubPanel> subpanels = new ArrayList<>();
//      private final List<LXPoint> points = new ArrayList<>();

        public Fixture(String id, float x, float y, float z, float rx, float ry, float rz, LXTransform t) {
            float SUBPANEL_LENGTH = 14.6666666f;

            this.id = id;
            float[] stringLengths = {5.0f, 5.0f, 5.0f};
            t.push();
            t.translate(x, y, z);
            t.rotateX(rx * PI/180);
            t.rotateY(ry * PI/180);
            t.rotateZ(rz * PI/180);

            for (int iPanel = 0; iPanel < 9; iPanel++) {
                SubPanel subpanel = new SubPanel("Panel_" + iPanel+1, t.x(), t.y(), t.z(), 0, 0, 0, stringLengths,  t);
                subpanels.add(subpanel);
                System.out.println("adding subpanel");
                for (LXPoint point : subpanel.points){

                    points.add(point);
                    this.points.add(point);

                }
                t.translate(SUBPANEL_LENGTH, 0, 0);
                if (iPanel % 3 == 2) {
                    t.translate(0, SUBPANEL_LENGTH, 0);
                    t.rotateX(180);

                }

                //subpanels.add(subpanel);
            }
            t.pop();
        }
//        return new Panel;
    }

}
