package com.symmetrylabs.slstudio.model;

import com.google.gson.annotations.Expose;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.List;

import static com.symmetrylabs.util.MathUtils.lerp;


public class InterpolatedStrip extends SLModel {

	public final String id;

	Fixture fixture = (Fixture) this.fixtures.get(0);

	@Expose
	public final Metrics metrics;


	public InterpolatedStrip(String id, Metrics metrics) {
		super(new Fixture(metrics));

		this.id = id;
		this.metrics = metrics;
	}



	private static class Fixture extends LXAbstractFixture {
		private Fixture(Metrics metrics) {
			assert metrics.start.size() == 3 && metrics.end.size() ==  3 : "should be 3 coordinates... should protect with a class..";
			// start interpolation.

			LXVector start = new LXVector(metrics.start.get(0).floatValue(), metrics.start.get(1).floatValue(), metrics.start.get(2).floatValue());
			LXVector end = new LXVector(metrics.end.get(0).floatValue(), metrics.end.get(1).floatValue(), metrics.end.get(2).floatValue());

			for (int i = 0; i < metrics.numPoints; i++) {
				LXVector v = start.lerp(end, (float)i/metrics.numPoints);
                LXPoint addme = v.getPoint();
				this.points.add(addme);
			}
		}
	}

	public static class Metrics {
		@Expose
		public final int numPoints;

		@Expose
		public final double pixelPitch;

		@Expose
		public final ArrayList<Double> start;

		@Expose
		public final ArrayList<Double> end;

		private double calcPixelPitch(){
			// dist end <--> start / numpix
			return 1.11; // placeholder
		}

		public Metrics(int numPoints, ArrayList<Double> start, ArrayList<Double> end) {

			this.numPoints = numPoints;
			this.start = start;
			this.end = end;

			pixelPitch = calcPixelPitch();
		}
	}
}
