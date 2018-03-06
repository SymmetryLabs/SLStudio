package com.symmetrylabs.util.DebugPointCloud;

import com.google.gson.annotations.Expose;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.List;

public class PointCloudViewer {
    class SimplePoint {
        @Expose
        public double[] point = new double[3];
        public SimplePoint(double x, double y, double z){
            point[0] = x;
            point[1] = y;
            point[2] = z;
        }
    }

    private List<SimplePoint> pointcloud = new ArrayList<SimplePoint>();

    public PointCloudViewer(LXFixture cloud){
        for (LXPoint pt : cloud.getPoints()){
            pointcloud.add(new SimplePoint(pt.x, pt.y, pt.z));
        }
    }
}
