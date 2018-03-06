package com.symmetrylabs.slstudio.model;

import com.symmetrylabs.util.DebugPointCloud.FixtureViewer;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CandyBarTest {
    @Test
    void getLocalSpacePoints() {
    }

    @Test
    void getPoints() {
    }

    @Test
    void displayPoints() throws IOException {
        FixtureViewer viewer = new FixtureViewer(new CandyBar());
        viewer.viewFixture();
    }

}
