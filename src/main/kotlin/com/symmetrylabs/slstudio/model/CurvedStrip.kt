package com.symmetrylabs.slstudio.model

import com.symmetrylabs.slstudio.mappings.FultonStreetLayout
import com.symmetrylabs.slstudio.model.Slice.PIXEL_PITCH
import com.symmetrylabs.slstudio.util.degToRad
import heronarts.lx.model.LXAbstractFixture
import heronarts.lx.transform.LXTransform


class CurvedStrip(
	id: String,
	metrics: CurvedMetrics,
	coordinates: FloatArray,
	rotations: FloatArray,
	transform: LXTransform,
	val sliceId: String,
	val fixture: Fixture = Fixture(id, metrics, coordinates, rotations, transform)
) : Strip(id, metrics.metrics, fixture.points) {

	constructor(
		id: String,
		metrics: CurvedMetrics,
		coordinates: FloatArray,
		rotations: FloatArray,
		transform: LXTransform,
		sliceId: String
	) : this(id, metrics, coordinates, rotations, transform, sliceId, Fixture(id, metrics, coordinates, rotations, transform))

	fun updateOffset(offset: Float) {
		fixture.updatePoints(offset)
	}

	class CurvedMetrics(val arcWidth: Float, numPoints: Int) {
		val metrics: Strip.Metrics
		val numPoints: Int

		init {
			this.metrics = Strip.Metrics(numPoints)
			this.numPoints = metrics.numPoints
		}
	}

	class Fixture constructor(
		val id: String,
		val metrics: CurvedMetrics,
		val coordinates: FloatArray,
		val rotations: FloatArray,
		transform: LXTransform
	) : LXAbstractFixture() {
		val transform = LXTransform(transform.matrix)

		init {
			for (i in 0 until metrics.numPoints) {
				points.add(LXPointNormal(0.0, 0.0, 0.0))
			}
			updatePoints()
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

				// arclength of bezier(0, 0.2, 0.8, 1) = 1.442
				// arclength of bezier(0, -0.3, -0.3, 0) = 1.122
				// arclength = 1.168
				// arclength at center = 107 in
				val arcLength = 1.13f * metrics.arcWidth
				System.out.println("strip length = " + (metrics.numPoints * PIXEL_PITCH) + ",  arc length = $arcLength")
				val t = 0.5f + ((i - metrics.numPoints / 2.0f) * PIXEL_PITCH + curveOffset) / arcLength
				if (t > 1 || t < 0) {
					throw RuntimeException("Placing pixel off sun: i = $i, arc length = $arcLength")
				}
				val x = bezierPoint(0f, metrics.arcWidth * 0.2f, metrics.arcWidth * 0.8f, metrics.arcWidth, t)
				val z = bezierPoint(0f, metrics.arcWidth * -0.3f, metrics.arcWidth * -0.3f, 0f, t)
				transform.translate(x, 0f, z)

				setPoint(i, transform)

				transform.pop()
			}

			transform.pop()
		}

		private fun setPoint(i: Int, pt: LXTransform) {
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
		private val counter = 0
	}
}

