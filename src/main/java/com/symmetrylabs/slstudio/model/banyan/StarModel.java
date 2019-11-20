package com.symmetrylabs.slstudio.model.banyan;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.transform.LXTransform;

public class StarModel extends SLModel{
    static int NUM_SYMMETRY = 8;

    public StarModel(String modelId) {
        super(modelId, new Fixture());
    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture() {
            LXTransform t = new LXTransform();

            for (int i = 0; i < NUM_SYMMETRY; i++){
//                t.rotateZ(45);
                TipperModel tipper = new TipperModel("tipper", t);
//                t.translate(10, 20, 0);
                TipModel shard = new TipModel("tip", t);
                points.addAll(shard.getPoints());
                points.addAll(tipper.getPoints());
            }
        }
    }
}
