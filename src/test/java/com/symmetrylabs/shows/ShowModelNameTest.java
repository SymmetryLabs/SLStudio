package com.symmetrylabs.shows;

import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.util.Utils;
import heronarts.lx.model.LXModel;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;

public class ShowModelNameTest {
    @BeforeClass
    public static void setupGlobals() {
        Utils.setSketchPath(Paths.get(System.getProperty("user.dir")).toString());
    }

    @Test
    public void testModelNamesMatchShowNames() {
        for (final String showName : ShowRegistry.getNames()) {
            ApplicationState.setProvider(new ApplicationState.DummyProvider() {
                @Override
                public String showName() {
                    return showName;
                }
            });
            Show show = ShowRegistry.getShow(showName);
            LXModel model = show.buildModel();
            Assert.assertEquals("root model ID must match show name", showName, model.modelId);
        }
    }
}
