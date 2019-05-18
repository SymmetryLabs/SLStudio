package com.symmetrylabs.slstudio.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.symmetrylabs.util.DebugPointCloud.FixtureViewer;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXModel;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class DXFLines {
	class Line{
		ArrayList<Double> start = new ArrayList<>();
		ArrayList<Double> end = new ArrayList <> ();
	}
	ArrayList<Line> lines = new ArrayList<>();
}

class InterpolatedStripTest {
    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .excludeFieldsWithoutExposeAnnotation()
        .create();


    @Test
    public void testFixtureViewer() throws IOException {

        // DXF IMPORT LOGIC
        BufferedReader br = new BufferedReader(
            new FileReader("data/importDXF/tetrahedron.json"));

        DXFLines dxfLines = (new Gson()).fromJson(br, DXFLines.class);
        System.out.println(dxfLines);


        ArrayList<InterpolatedStrip> interpolatedStrips = new ArrayList<>();

        for(DXFLines.Line line : dxfLines.lines){
            System.out.print(line.start + " --- ");
            System.out.println(line.end);
            InterpolatedStrip interpolatedStrip = new InterpolatedStrip("test", new InterpolatedStrip.Metrics(29,
                line.start, line.end));
            interpolatedStrips.add(interpolatedStrip);
        }

        LXFixture[] fixtures = new LXFixture[interpolatedStrips.size()];
        int i = 0;
        for (InterpolatedStrip s : interpolatedStrips){
            fixtures[i++] = s.fixture;
        }

        SLModel model = new SLModel(fixtures);
        // END DXF IMPORT.  Lines are stored as strips in 'model'

//        JsonWriter writer = new JsonWriter(new FileWriter("data/test.json"));

        FixtureViewer viewer = new FixtureViewer(model);
        viewer.viewFixture();
//        System.out.println(gson.toJson(strip));
    }


}
