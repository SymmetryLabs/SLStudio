package com.symmetrylabs.shows.mikey;

import com.google.common.collect.Lists;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.CandyBar;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.model.DoubleStrip;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXTransform;
import java.util.ArrayList;
import java.util.List;

public class MikeyShow implements Show {
    public static final String SHOW_NAME = "mikey";

    @Override
    public SLModel buildModel() {
        return MikeyModel.create();
    }

    @Override
    public void setupLx(LX lx) {
        MikeyPixlite pixlite = new MikeyPixlite(lx, "wledatom.local", (MikeyModel) lx.model);
        lx.addOutput(pixlite);
    }

    static class MikeyModel extends StripsModel<Strip> {
        public MikeyModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
        }

        public static MikeyModel create()
        {
            List<Strip> strips = new ArrayList<Strip>();
            LXTransform t = new LXTransform();

            // rotate starting position 180
            t.rotateY(3.14159); // 180 == 3.142

            // sides

            for (int i=0; i<10; i++)
            {
                // row k
                strips.add(new Strip("1", new Strip.Metrics(1, 1), t)); // add strip
                t.translate(0.5f,0,0);
                t.rotateY(-1.5708); // rotate 90
                t.translate(0.5f,0,0);

                strips.add(new Strip("1", new Strip.Metrics(4, 1), t)); // add strip
                t.translate(3.5f,0,0); // move to end of strip
                t.rotateY(-1.5708); // rotate 90
                t.translate(0.5f,0,0);

                strips.add(new Strip("1", new Strip.Metrics(4, 1), t)); // add strip
                t.translate(3.5f,0,0); // move to end of strip
                t.rotateY(-1.5708); // rotate 90
                t.translate(0.5f,0,0);

                strips.add(new Strip("1", new Strip.Metrics(4, 1), t)); // add strip
                t.translate(3.5f,0,0); // move to end of strip
                t.rotateY(-1.5708); // rotate 90
                t.translate(0.5f,0,0);

                strips.add(new Strip("1", new Strip.Metrics(2, 1), t)); // add strip
                t.translate(1,0,0); // move to end of strip


                t.translate(0,0.5f,0); // move up to next row
                t.rotateY(3.14159); // 180

                // row k+1  (same but reversed, because serpentine)
                strips.add(new Strip("1", new Strip.Metrics(2, 1), t)); // add strip
                t.translate(1.5f,0,0); // move to end of strip
                t.rotateY(1.5708); // rotate 90
                t.translate(0.5f,0,0); // move to end of strip

                strips.add(new Strip("1", new Strip.Metrics(4, 1), t)); // add strip
                t.translate(3.5f,0,0); // move to end of strip
                t.rotateY(1.5708); // rotate 90
                t.translate(0.5f,0,0);

                strips.add(new Strip("1", new Strip.Metrics(4, 1), t)); // add strip
                t.translate(3.5f,0,0); // move to end of strip
                t.rotateY(1.5708); // rotate 90
                t.translate(0.5f,0,0);

                strips.add(new Strip("1", new Strip.Metrics(4, 1), t)); // add strip
                t.translate(3.5f,0,0); // move to end of strip
                t.rotateY(1.5708); // rotate 90
                t.translate(0.5f,0,0);

                strips.add(new Strip("1", new Strip.Metrics(1, 1), t)); // add strip

                t.translate(0,0.5f,0); // move up to next row
                t.rotateY(3.14159); // 180
            }


            // top
            t.rotateY(-1.5708); // rotate 90
            t.translate(1.5f,0,0);

            for (int i=0; i<4; i++)
            {
                strips.add(new Strip("1", new Strip.Metrics(2, 1), t)); // add strip
                t.translate(2,0,0); // move to end of strip
                t.rotateY(-1.5708); // rotate 90
                t.translate(1,0,0);
            }


  




        /* sprial layout
            int sideLengths[] = {3,4,4,3,4,4,3,4,3,4,4,3,4,4,3,4,3,4,4,3,4,4,3,4,4,3,4,4,3,4,4,3,4,3,4,4,4,3,4,3,4,4,3,4,4,3,4,4,3,4,4,3,4,4,3,4,4,3,4,4,3,4,3,4,4,3,4,4,3,4,4,4,3,4,3,4,4,3,4,3};

            // rotate starting position 180
            t.rotateZ(3.15); // 180 == 3.142
            // 3.06 to far left
            // 3.0 is more left
            // 3.1 too far left
            // 3.5 way too far right
            // 3.2 too far right
            // 3.15  close enough

            // sides
            for (int len : sideLengths)
            {
                t.rotateZ(-0.05f); // rotate up
                t.translate(5 - len,0,0); // move to start of strip
                strips.add(new Strip("1", new Strip.Metrics(len, 1), t)); // add strip
                t.translate(len,0,0); // move to end of strip
                t.rotateZ(0.05f); // rotate down
                t.rotateY(1.5708); // rotate 90
            }

            // top
            int topLengths[] = {2,2,2,2};

            // t.rotateY(1.5708);
            t.translate(1,-0.5f,-1);

            for (int len : topLengths)
            {
                t.translate(1,0,0);
                strips.add(new Strip("1", new Strip.Metrics(len, 1), t));
                t.translate(len,0,0);
                t.rotateY(1.5708);
            }

        */

            return new MikeyModel(strips);
        }
    }
    static class MikeyPixlite extends SimplePixlite{
        public MikeyPixlite(LX lx, String ip, MikeyModel model) {
            super(lx, ip);

            PointsGrouping grouping = new PointsGrouping("1");

            for (int i=0; i<104; i++)
            {
                grouping.addPoints(model.getStripByIndex(i).getPoints());
            }

            addPixliteOutput(grouping);


            // addPixliteOutput(
            //     new PointsGrouping("1")
            //     .addPoints(model.getStripByIndex(0).getPoints())
            //     .addPoints(model.getStripByIndex(1).getPoints())
            // );
        }

        @Override
        public SimplePixlite addPixliteOutput(PointsGrouping pointsGrouping) {
            try {
                SimplePixliteOutput spo = new SimplePixliteOutput(pointsGrouping);
                spo.setLogConnections(false);
                addChild(spo);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this;
        }
    }
}
