package com.symmetrylabs.shows.summerstage;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.StripsTopology;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestLoadTopology {
    @Test
    public void testLoadTopology() {
        SummerStageShow sss = new SummerStageShow();
        SLModel slm = sss.buildModel();
        StripsTopology topo = ((CubesModel) slm).getTopology();
        Assertions.assertNotNull(topo);
    }
}
