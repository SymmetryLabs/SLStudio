package com.symmetrylabs.slstudio.model.banyan;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

public class TipperModel extends SLModel {
    static byte[] tip = new byte[] {
        1,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,
        1,0,0,0,0,0,0,1,1,0,1,1,0,0,0,0,0,0,
        0,0,0,0,0,0,1,1,1,0,1,1,1,0,0,0,0,0,
        1,0,0,0,0,1,1,1,1,0,1,1,1,1,0,0,0,0,
        0,0,0,0,0,1,1,1,1,0,1,1,1,1,0,0,0,0,
        1,0,0,0,1,1,1,1,1,0,1,1,1,1,1,0,0,0,
        0,0,0,1,1,1,1,1,1,0,1,1,1,1,1,1,0,0,
        1,0,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,0,
        0,0,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,0,
        1,0,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,0,
        0,0,0,1,1,1,1,1,1,0,1,1,1,1,1,1,0,0,
        1,1,0,0,1,1,1,1,1,0,1,1,1,1,1,0,0,1,
        0,1,1,0,1,1,1,1,1,0,1,1,1,1,1,0,1,1,
        1,1,1,0,0,1,1,1,1,0,1,1,1,1,0,0,1,1,
        0,1,1,1,0,0,1,1,1,0,1,1,1,0,0,1,1,1,
        1,1,1,1,1,0,0,1,1,0,1,1,0,0,1,1,1,1,
        0,1,1,1,1,1,0,1,1,0,1,1,0,1,1,1,1,1,
        1,1,1,1,1,1,1,0,1,0,1,0,1,1,1,1,1,1,
        0,1,1,1,1,1,1,0,0,0,0,0,1,1,1,1,1,1,
        1,0,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,0,
        0,0,0,1,1,1,1,1,1,0,1,1,1,1,1,1,0,0,
        1,0,0,0,1,1,1,1,1,0,1,1,1,1,1,0,0,0,
        0,0,0,0,0,1,1,1,1,0,1,1,1,1,0,0,0,0,
        1,0,0,0,0,0,1,1,1,0,1,1,1,0,0,0,0,0,
        0,0,0,0,0,0,0,1,1,0,1,1,0,0,0,0,0,0,
        1,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    };



    public TipperModel(String modelId, LXTransform t) {
        super(modelId, new Fixture(tip, t));
    }

    private static class Fixture extends LXAbstractFixture {

        private Fixture(byte[] config, LXTransform t) {
           int wide = 18;

           for (int i = 0; i < config.length; i+=wide){
               boolean rightTraversal = config[i] == 0x1;
               if (rightTraversal){
                   for (int j = i + 1; j < i + wide; j++){
                       if (config[j] == 1){
                           points.add(new LXPoint(j%wide + t.x(), i/wide + t.y()));
                       }
                   }
               } else {
                   for (int j = i + wide - 1; j > i; j--){
                       if (config[j] == 1){
                           points.add(new LXPoint(j%wide + t.x(), i/wide + t.y()));
                       }
                   }
               }
           }
        }
    }
}
