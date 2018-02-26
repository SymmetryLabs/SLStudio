package com.symmetrylabs.slstudio.model.suns

import com.symmetrylabs.slstudio.mappings.StripMapping
import com.symmetrylabs.slstudio.model.LXPointNormal
import com.symmetrylabs.slstudio.model.Strip
import com.symmetrylabs.slstudio.model.suns.Slice.PIXEL_PITCH
import com.symmetrylabs.slstudio.util.CurveUtils.bezierPoint
import com.symmetrylabs.slstudio.util.degToRad
import heronarts.lx.model.LXAbstractFixture
import heronarts.lx.transform.LXTransform


class CurvedStrip(
		val mappings: StripMapping,
		id: String,
		metrics: CurvedMetrics,
		val sunId: String,
		val sliceId: String,
		val fixture: Fixture
) : Strip(id, metrics, fixture.points) {

	constructor(
			mappings: StripMapping,
			id: String,
			metrics: CurvedMetrics,
			coordinates: FloatArray,
			rotations: FloatArray,
			transform: LXTransform,
			sunId: String,
			sliceId: String
	) : this(mappings, id, metrics, sunId, sliceId, Fixture(mappings, metrics, coordinates, rotations, transform))

	fun updateOffset(offset: Float) {
        mappings.rotation = offset
        fixture.updatePoints(offset)
	}

	class CurvedMetrics(val arcWidth: Float, numPoints: Int) : Metrics(numPoints)

    class Fixture constructor(
			private val mappings: StripMapping,
			private val metrics: CurvedMetrics,
			private val coordinates: FloatArray,
			private val rotations: FloatArray,
			transform: LXTransform
	) : LXAbstractFixture() {
		val transform = LXTransform(transform.matrix)

		init {
			for (i in 0 until metrics.numPoints) {
				points.add(LXPointNormal(0.0, 0.0, 0.0))
			}
			mappings.points = (if (mappings.reversed) points.reversed() else points).toTypedArray()
			updatePoints(mappings.rotation)
		}

		fun updatePoints(
			curveOffset: Float = 0f
		) {
			transform.push()
			transform.translate(coordinates[0], coordinates[1], coordinates[2])
			transform.rotateX(rotations[1].degToRad)
			transform.rotateY(rotations[2].degToRad)
			transform.rotateZ(rotations[0].degToRad)

			for (i in 0 until metrics.numPoints) {
				transform.push()

				calculatePointTransform(i, metrics.numPoints, metrics.arcWidth, curveOffset, transform)
				setPoint(i, transform)

				transform.pop()
			}

			transform.pop()
		}

		private fun setPoint(i: Int, transform: LXTransform) {
			val point = points[i]

			point.x = transform.x()
			point.y = transform.y()
			point.z = transform.z()

			point.update()
		}

		private fun bezierPoint(a: Float, b: Float, c: Float, d: Float, t: Float): Float {
			val t1 = 1.0f - t
			return (a * t1 + 3f * b * t) * (t1 * t1) + (3f * c * t1 + d * t) * (t * t)
		}
	}

	companion object {
		fun calculatePointTransform(i: Int, numPoints: Int, arcWidth: Float,
									curveOffset: Float, transform: LXTransform) {
			// arclength of bezier(0, 0.2, 0.8, 1) = 1.442
			// arclength of bezier(0, -0.3, -0.3, 0) = 1.122
			// arclength = 1.168
			// arclength at center = 107 in
			val arcLength = 1.3f * arcWidth
			val t = 0.5f + ((i - numPoints / 2.0f) * PIXEL_PITCH + curveOffset) / arcLength
//			if (t > 1 || t < 0) {
//				throw RuntimeException("Placing pixel off sun: i = $i, arc length = $arcLength")
//			}

			val x = bezierPoint(0f, arcWidth * 0.2f, arcWidth * 0.8f, arcWidth, t)
			val z = bezierPoint(0f, arcWidth * -0.3f, arcWidth * -0.3f, 0f, t)
			transform.translate(x, 0f, z)
		}
	}
}

