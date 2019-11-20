package com.symmetrylabs.slstudio.model.banyan;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;

public class StarModel extends SLModel {
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
        1,1,1,1,1,1,1,1,1,1,1,1,0,0,
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


    public StarModel(String modelId) {
        super(modelId, new Fixture(bitmap));
    }

    private static class Fixture extends LXAbstractFixture {

        private Fixture(byte[] config) {
           int wide = 14;

           for (int i = 0; i < config.length; i+=wide){
               boolean rightTraversal = config[i] == 0x1;
               if (rightTraversal){
                   for (int j = i + 1; j < i + wide; j++){
                       if (config[j] == 1){
                           points.add(new LXPoint(j%wide, i/wide));
                       }
                   }
               } else {
                   for (int j = i + wide - 1; j > i; j--){
                       if (config[j] == 1){
                           points.add(new LXPoint(j%wide, i/wide));
                       }
                   }
               }
           }
        }
    }
}
