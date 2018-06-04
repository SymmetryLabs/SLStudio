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
    public SubPanel getSubPanelByIndex(int i) {

        return subpanels.get(i);

    }

    private static class Fixture extends LXAbstractFixture {
        private final String id;
        private final List<SubPanel> subpanels = new ArrayList<>();
//      private final List<LXPoint> points = new ArrayList<>();

        public Fixture(String id, float x, float y, float z, float rx, float ry, float rz, LXTransform t) {
            float SUBPANEL_LENGTH = 14.6666666f;

            this.id = id;
            float[] stringLengths1 = {5.0f, 5.0f, 5.0f};
            float[] stringLengths = {0.0f, 0.0f, 0.0f};
            t.push();
            t.translate(x, y, z);
            t.rotateX(rx * PI/180);
            t.rotateY(ry * PI/180);
            t.rotateZ(rz * PI/180);
            int rotY = 0;


            SubPanel subpanel1 = new SubPanel("subpanel_1", 0, 0,0 ,0, 0, 0, stringLengths,  t);
            subpanels.add(subpanel1);
            System.out.println("adding subpanel");
            for (LXPoint point : subpanel1.points){

                points.add(point);
                this.points.add(point);

            }

            SubPanel subpanel2 = new SubPanel("subpanel_2" , SUBPANEL_LENGTH, 0, 0, 0, 0, rotY, stringLengths,  t);
            subpanels.add(subpanel2);
            System.out.println("adding subpanel");
            for (LXPoint point : subpanel2.points){

                points.add(point);
                this.points.add(point);

            }

            SubPanel subpanel3 = new SubPanel("subpanel_3", SUBPANEL_LENGTH*2, 0, 0, 0, 0, rotY, stringLengths,  t);
            subpanels.add(subpanel3);
            System.out.println("adding subpanel");
            for (LXPoint point : subpanel3.points){

                points.add(point);
                this.points.add(point);

            }
//            t.translate(SUBPANEL_LENGTH, 0, 0);
//            t.rotateY(PI);

            SubPanel subpanel4 = new SubPanel("subpanel_4", SUBPANEL_LENGTH, SUBPANEL_LENGTH, 0, 0f, 180, 0f, stringLengths,  t);
            subpanels.add(subpanel4);
            System.out.println("adding subpanel");
            for (LXPoint point : subpanel4.points){

                points.add(point);
                this.points.add(point);

            }

            SubPanel subpanel5 = new SubPanel("subpanel_5", SUBPANEL_LENGTH*2, SUBPANEL_LENGTH, 0, 0f, 180, 0f, stringLengths,  t);
            subpanels.add(subpanel5);
            System.out.println("adding subpanel");
            for (LXPoint point : subpanel5.points){

                points.add(point);
                this.points.add(point);

            }

            SubPanel subpanel6 = new SubPanel("subpanel_6", SUBPANEL_LENGTH*3, SUBPANEL_LENGTH, 0, 0f, 180, 0f, stringLengths,  t);
            subpanels.add(subpanel6);
            System.out.println("adding subpanel");
            for (LXPoint point : subpanel6.points){

                points.add(point);
                this.points.add(point);

            }

            SubPanel subpanel7 = new SubPanel("subpanel_7", 0, SUBPANEL_LENGTH*2, 0, 0f, 0f, 0f, stringLengths,  t);
            subpanels.add(subpanel7);
            System.out.println("adding subpanel");
            for (LXPoint point : subpanel7.points){

                points.add(point);
                this.points.add(point);

            }

            SubPanel subpanel8 = new SubPanel("subpanel_8", SUBPANEL_LENGTH, SUBPANEL_LENGTH*2, 0, 0f, 0, 0f, stringLengths,  t);
            subpanels.add(subpanel8);
            System.out.println("adding subpanel");
            for (LXPoint point : subpanel8.points){

                points.add(point);
                this.points.add(point);

            }
            SubPanel subpanel9 = new SubPanel("subpanel_9", SUBPANEL_LENGTH*2, SUBPANEL_LENGTH*2, 0, 0f, 0, 0f, stringLengths,  t);
            subpanels.add(subpanel9);
            System.out.println("adding subpanel");
            for (LXPoint point : subpanel9.points){

                points.add(point);
                this.points.add(point);

            }
//
//          for (int iPanel = 0; iPanel < 4; iPanel++) { // < 9
//                if (iPanel == 3) {
//                  rotY = 180;
//                }
//
//                SubPanel subpanel = new SubPanel("Panel_" + iPanel+1, t.x(), t.y(), t.z(), 0, 0, rotY, stringLengths,  t);
//                subpanels.add(subpanel);
//                System.out.println("adding subpanel");
//                for (LXPoint point : subpanel.points){
//
//                    points.add(point);
//                    this.points.add(point);
//
//                }
//                t.translate(SUBPANEL_LENGTH, 0, 0);
//                if (iPanel % 3 == 2) {
//                    t.translate(0, SUBPANEL_LENGTH, 0);
////                    t.rotateY(180);
//
//                }

                //subpanels.add(subpanel);
//            }
            t.pop();
        }
//        return new Panel;
    }

}
