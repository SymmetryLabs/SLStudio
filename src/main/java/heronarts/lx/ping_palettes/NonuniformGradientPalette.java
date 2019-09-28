package heronarts.lx.ping_palettes;

import com.google.common.base.Preconditions;
import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Spaces;
import heronarts.lx.color.LXColor;

import java.util.Arrays;

public class NonuniformGradientPalette implements ColorPalette {
	public static class Point implements Comparable<Point> {
		public final int color;
		public final double t;

		public Point(int color, double t) {
			this.color = 0xFF000000 | color;
			this.t = t;
		}

		@Override
		public String toString() {
			return String.format("new com.symmetrylabs.slstudio.palettes.NonuniformGradientPalette.Point(0x%08X, %f)", color, t);
		}

		@Override
		public int compareTo(Point p) {
			return Double.compare(t, p.t);
		}
	}

	public final Point[] points;

	public NonuniformGradientPalette(Point[] points) {
		Preconditions.checkArgument(points.length >= 2);
		this.points = Arrays.copyOf(points, points.length);
		Arrays.sort(this.points);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Point p : points) {
			sb.append("\n\t");
			sb.append(p.toString());
			sb.append(",");
		}
		String result = sb.toString();
		result = result.substring(0, result.length() - 2);
		return "com.symmetrylabs.slstudio.palettes.NonuniformGradientPalette(new com.symmetrylabs.slstudio.palettes.NonuniformGradientPalette.Point[] {" + result + "})";
	}

	double clamp(double value, double low, double high) {
		return Math.min(Math.max(value, low), high);
	}

	public int getColor(double p) {
		if (p <= points[0].t) {
			return points[0].color;
		}
		for (int i = 0; i < points.length - 1; i++) {
			if (p < points[i + 1].t) {
				double ratio = (p - points[i].t) / (points[i + 1].t - points[i].t);
				return LXColor.lerp(points[i].color, points[i + 1].color, ratio);
			}
		}
		return points[points.length - 1].color;
	}

	public long getColor16(double p) {
		if (p <= points[0].t) {
			return points[0].color;
		}
		for (int i = 0; i < points.length - 1; i++) {
			if (p < points[i].t) {
				double ratio = (p - points[i].t) / (points[i + 1].t - points[i].t);
				return Ops16.blend(Spaces.rgb8ToRgb16(points[i].color), Spaces.rgb8ToRgb16(points[i + 1].color), ratio);
			}
		}
		return points[points.length - 1].color;
	}
}
