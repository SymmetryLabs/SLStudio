package com.symmetrylabs.slstudio.output;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import java.io.IOException;
import java.io.Writer;

public class ModelCsvWriter {
    public static void write(LXModel m, Writer w) throws IOException {
        w.write("Index,X,Y,Z\n");
        for (int i = 0; i < m.points.length; i++) {
            LXPoint p = m.points[i];
            w.write(String.format("%d,%f,%f,%f\n", i, p.x, p.y, p.z));
        }
    }
}
