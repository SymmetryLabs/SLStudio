package com.symmetrylabs.shows.composite;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.mikey.MikeyShow;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesController;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.Strip;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * CompositeShow merges MikeyShow and YsiadsPartyShow so both models and outputs coexist.
 */
public class CompositeShow implements Show {
    public static final String SHOW_NAME = "composite";

    @Override
    public SLModel buildModel() {
        return CompositeModel.create();
    }

    @Override
    public void setupLx(LX lx) {
        CompositeModel model = (CompositeModel) lx.model;
        // Add Mikey outputs (Pixlite)
        MikeyShow.MikeyModel mikeyModel = model.getMikeyModel();
        MikeyShow.MikeyPixlite pixlite1 = new MikeyShow.MikeyPixlite(lx, "192.168.1.42", mikeyModel, 0);
        MikeyShow.MikeyPixlite pixlite2 = new MikeyShow.MikeyPixlite(lx, "192.168.0.193", mikeyModel, 8);
        lx.addOutput(pixlite1);
        lx.addOutput(pixlite2);
        // Add Cubes outputs (CubesControllers)
        CubesModel cubesModel = model.getCubesModel();
        // You may need to adjust the CubesController constructor arguments as needed for your setup
        CubesController cubesController = new CubesController(lx, "192.168.1.100"); // Example IP
        lx.addOutput(cubesController);
    }

    /**
     * CompositeModel contains both MikeyModel and YsiadsPartyModel as children.
     */
    public static class CompositeModel extends SLModel {
        private final MikeyShow.MikeyModel mikeyModel;
        private final CubesModel cubesModel;

        public CompositeModel(MikeyShow.MikeyModel mikeyModel, CubesModel cubesModel) {
            super(SHOW_NAME, combinePoints(mikeyModel, cubesModel));
            this.mikeyModel = mikeyModel;
            this.cubesModel = cubesModel;
        }

        public static CompositeModel create() {
            MikeyShow.MikeyModel mikeyModel = MikeyShow.MikeyModel.create();
            // Use YsiadsPartyShow to build the cubes model
            CubesModel cubesModel = (CubesModel) new com.symmetrylabs.shows.ysiadsparty.YsiadsPartyShow().buildModel();
            return new CompositeModel(mikeyModel, cubesModel);
        }

        public MikeyShow.MikeyModel getMikeyModel() { return mikeyModel; }
        public CubesModel getCubesModel() { return cubesModel; }

        @Override
        public Iterator<? extends LXModel> getChildren() {
            List<LXModel> children = new ArrayList<>();
            children.add(mikeyModel);
            children.add(cubesModel);
            return children.iterator();
        }

        private static List<LXPoint> combinePoints(LXModel... models) {
            List<LXPoint> all = new ArrayList<>();
            for (LXModel m : models) {
                Object pts = m.points;
                if (pts instanceof List) {
                    all.addAll((List<LXPoint>) pts);
                } else if (pts instanceof LXPoint[]) {
                    all.addAll(Arrays.asList((LXPoint[]) pts));
                }
            }
            return all;
        }
    }
}
