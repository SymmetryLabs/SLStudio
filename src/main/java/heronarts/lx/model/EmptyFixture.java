package heronarts.lx.model;

import java.util.*;

import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;

public class EmptyFixture implements LXFixture {

	private List<LXPoint> points = new ArrayList<LXPoint>();

	public List<LXPoint> getPoints() {
		return points;
	}
}