package com.symmetrylabs.slstudio.model;

import com.symmetrylabs.slstudio.ApplicationState;
import heronarts.lx.model.LXModel;
import java.util.Iterator;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;

import static com.symmetrylabs.util.DistanceUtils.squaredEuclideanDistance;

/**
 * A model with strips.
 */
public class StripsModel<T extends Strip> extends SLModel {
    private static final float DEFAULT_STRIP_JOINING_DISTANCE = 0;

    protected final List<T> strips = new ArrayList<>();
    protected final List<T> stripsUnmodifiable = Collections.unmodifiableList(strips);
    protected final Map<String, Strip> stripTable = new HashMap<>();

    private StripsTopology topology = null;
    boolean topologyInferenceAttempted = false;
    private float orderTolerance = 2;  // tolerance for combining strips into bundles; inches along bundle's axis
    private float bucketTolerance = 6;  // tolerance for combining strips into bundles; inches perpendicular to bundle's axis
    private float endpointTolerance = 6;  // tolerance for combining endpoints into junctions; inches in any direction

    public StripsModel() {
    }

    public StripsModel(List<T> strips) {
        this(null, strips);
    }

    protected StripsModel(LXFixture fixture) {
        this(null, fixture);
    }

    protected StripsModel(LXFixture[] fixtures) {
        this(null, fixtures);
    }

    public StripsModel(String modelId, List<T> strips) {
        super(modelId, new Fixture<T>(strips));

        this.strips.addAll(strips);
    }

    protected StripsModel(String modelId, LXFixture fixture) {
        super(modelId, fixture);
    }

    protected StripsModel(String modelId, LXFixture[] fixtures) {
        super(modelId, fixtures);
    }

    public StripsTopology getTopology() {
        /* This is here so that we don't repeatedly fail to load the topology
         * when inference fails. */
        if (topologyInferenceAttempted) {
            return topology;
        }
        topologyInferenceAttempted = true;

        if (topology == null) {
            try {
                topology = new StripsTopology(this, orderTolerance, bucketTolerance, endpointTolerance);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return topology;
    }

    public List<T> getStrips() {
        return stripsUnmodifiable;
    }

    @Override
    public Iterator<? extends LXModel> getChildren() {
        return getStrips().iterator();
    }

    public Strip getStripById(String id) {
        return this.stripTable.get(id);
    }

    public Strip getStripByIndex(int i) {
        return strips.get(i);
    }

    private static class Fixture<T extends Strip> extends LXAbstractFixture {
        public Fixture(List<T> strips) {
            for (T strip : strips) {
                points.addAll(strip.getPoints());
            }
        }
    }

    private float stripJoiningDistance = DEFAULT_STRIP_JOINING_DISTANCE;
    private Map<T, List<T>> connectivityGraph = null;

    public void setTopologyTolerances(float orderTolerance, float bucketTolerance, float endpointTolerance) {
        if (topology == null) {
            this.orderTolerance = orderTolerance;
            this.bucketTolerance = bucketTolerance;
            this.endpointTolerance = endpointTolerance;
        } else {
            ApplicationState.setWarning("StripsModel", "Cannot change topology tolerances after topology is built");
        }
    }

    public void setJoiningDistance(float dist) {
        stripJoiningDistance = dist;
    }

    public float getJoiningDistance() {
        return stripJoiningDistance;
    }

    public Map<T, List<T>> getConnectivityGraph() {
        if (connectivityGraph != null)
            return connectivityGraph;

        Map<T, List<T>> graph = new HashMap<>();

        List<T> strips = getStrips();
        float joiningDistance = getJoiningDistance();
        float squaredJoiningDistance = joiningDistance * joiningDistance;

        // TODO: use smarter/faster clustering
        for (T a : strips) {
            graph.put(a, new ArrayList<T>());

            List<LXPoint> aPoints = a.getPoints();
            if (aPoints.isEmpty())
                continue;

            LXPoint aStart = aPoints.get(0);
            LXPoint aEnd = aPoints.get(aPoints.size() - 1);

            for (T b : strips) {
                List<LXPoint> bPoints = b.getPoints();
                if (bPoints.isEmpty())
                    continue;

                LXPoint bStart = bPoints.get(0);
                LXPoint bEnd = bPoints.get(bPoints.size() - 1);

                float minSquaredDist = Math.min(squaredEuclideanDistance(aStart, bStart),
                        Math.min(squaredEuclideanDistance(aStart, bEnd),
                        Math.min(squaredEuclideanDistance(aEnd, bStart),
                        squaredEuclideanDistance(aEnd, bEnd))));

                if (minSquaredDist < squaredJoiningDistance) {
                    graph.get(a).add(b);
                }
            }
        }

        connectivityGraph = graph;

        return graph;
    }
}
