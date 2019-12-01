package com.symmetrylabs.slstudio.model.banyan;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

public class InsideShardPanel extends Panel {
    static byte[] bitmap = new byte[] {
        1,0,0,0,0,0,1,0,0,0,0,0,0,0,
        1,0,0,0,0,0,1,0,0,0,0,0,0,0,
        1,0,0,0,0,1,1,1,0,0,0,0,0,0,
        0,0,0,0,0,1,1,1,0,0,0,0,0,0,
        1,0,0,0,1,1,1,1,1,0,0,0,0,0,
        0,0,0,0,1,1,1,1,1,0,0,0,0,0,
        1,0,0,0,1,1,1,1,1,0,0,0,0,0,
        0,0,0,1,1,1,1,1,1,1,0,0,0,0,
        1,0,0,1,1,1,1,1,1,1,0,0,0,0,
        0,0,1,1,1,1,1,1,1,1,1,0,0,0,
        1,0,1,1,1,1,1,1,1,1,1,0,0,0,
        0,0,1,1,1,1,1,1,1,1,1,0,0,0,
        1,0,0,1,1,1,1,1,1,1,0,0,0,0,
        0,0,0,1,1,1,1,1,1,1,0,0,0,0,
        1,0,0,0,1,1,1,1,1,0,0,0,0,0,
        0,0,0,0,1,1,1,1,1,0,0,0,0,0,
        1,0,0,0,1,1,1,1,1,0,0,0,0,0,
        0,0,0,0,0,1,1,1,0,0,0,0,0,0,
        1,0,0,0,0,1,1,1,0,0,0,0,0,0,
        1,0,0,0,0,0,1,0,0,0,0,0,0,0,
        1,0,0,0,0,0,1,0,0,0,0,0,0,0
    };


    public InsideShardPanel(String modelId, LXTransform t) {
        super(modelId, new Fixture(bitmap, t));
    }

    private static class Fixture extends LXAbstractFixture {

        private Fixture(byte[] config, LXTransform t) {
           int wide = 14;

           for (int i = 0; i < config.length; i+=wide){
               boolean rightTraversal = config[i] == 0x1;
               if (rightTraversal){
                   for (int j = i + 1; j < i + wide; j++){
                       if (config[j] == 1){
                           t.push();
                           t.translate(j%wide, i/wide, 0);
                           points.add(new LXPoint(t));
                           t.pop();
                       }
                   }
               } else {
                   for (int j = i + wide - 1; j > i; j--){
                       if (config[j] == 1){
                           t.push();
                           t.translate(j%wide, i/wide, 0);
                           points.add(new LXPoint(t));
                           t.pop();
                       }
                   }
               }
           }
        }
    }
}
