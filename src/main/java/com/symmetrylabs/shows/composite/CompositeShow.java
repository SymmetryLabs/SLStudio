package com.symmetrylabs.shows.composite;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.mikey.MikeyShow;
import com.symmetrylabs.shows.ysiadsparty.YsiadsPartyShow;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.Strip;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;
import java.util.ArrayList;
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
        // Add YsiadsParty outputs (CubesControllers)
        YsiadsPartyShow.YsiadsPartyModel ysiadsModel = model.getYsiadsModel();
        YsiadsPartyShow.CubesControllers cubesControllers = new YsiadsPartyShow.CubesControllers(lx, ysiadsModel);
        lx.addOutput(cubesControllers);
    }

    /**
     * CompositeModel contains both MikeyModel and YsiadsPartyModel as children.
     */
    public static class CompositeModel extends SLModel {
        private final MikeyShow.MikeyModel mikeyModel;
        private final YsiadsPartyShow.YsiadsPartyModel ysiadsModel;

        public CompositeModel(MikeyShow.MikeyModel mikeyModel, YsiadsPartyShow.YsiadsPartyModel ysiadsModel) {
            super(SHOW_NAME, combinePoints(mikeyModel, ysiadsModel));
            this.mikeyModel = mikeyModel;
            this.ysiadsModel = ysiadsModel;
        }

        public static CompositeModel create() {
            MikeyShow.MikeyModel mikeyModel = MikeyShow.MikeyModel.create();
            YsiadsPartyShow.YsiadsPartyModel ysiadsModel = YsiadsPartyShow.YsiadsPartyModel.create();
            return new CompositeModel(mikeyModel, ysiadsModel);
        }

        public MikeyShow.MikeyModel getMikeyModel() { return mikeyModel; }
        public YsiadsPartyShow.YsiadsPartyModel getYsiadsModel() { return ysiadsModel; }

        @Override
        public Iterator<? extends LXModel> getChildren() {
            List<LXModel> children = new ArrayList<>();
            children.add(mikeyModel);
            children.add(ysiadsModel);
            return children.iterator();
        }

        private static List<LXPoint> combinePoints(LXModel... models) {
            List<LXPoint> all = new ArrayList<>();
            for (LXModel m : models) {
                all.addAll(m.points);
            }
            return all;
        }
    }
}
