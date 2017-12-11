package com.symmetrylabs.slstudio.model

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
	val fixture: Fixture = Fixture(id, metrics, coordinates, rotations, transform)
) : Strip(id, metrics.metrics, fixture.points) {

	constructor(
		id: String,
		metrics: CurvedMetrics,
		coordinates: FloatArray,
		rotations: FloatArray,
		transform: LXTransform
	) : this(id, metrics, coordinates, rotations, transform, Fixture(id, metrics, coordinates, rotations, transform))

	fun updateRotationY(angle: Float) {
		fixture.updatePoints(0f, angle, 0f)
	}

	class CurvedMetrics(val arcWidth: Float, numPoints: Int) {
		val metrics: Strip.Metrics
		val pitch: Float
		val numPoints: Int

		init {
			this.metrics = Strip.Metrics(numPoints, PIXEL_PITCH)
			this.pitch = metrics.POINT_SPACING
			this.numPoints = metrics.numPoints
		}
	}

	class Fixture constructor(
		val id: String,
		val metrics: CurvedMetrics,
		val coordinates: FloatArray,
		val rotations: FloatArray,
		val transform: LXTransform
	) : LXAbstractFixture() {
		init {
			for (i in 0 until metrics.numPoints) {
				points.add(LXPointNormal(0.0, 0.0, 0.0))
			}
			updatePoints()
		}

		fun updatePoints(
			xRot: Float = 0f,
			yRot: Float = 0f,
			zRot: Float = 0f
		) {
			transform.push()
			transform.translate(coordinates[0], coordinates[1], coordinates[2])
			transform.rotateX(rotations[1].degToRad + xRot)
			transform.rotateY(rotations[2].degToRad + yRot)
			transform.rotateZ(rotations[0].degToRad + zRot)

			for (i in 0 until metrics.numPoints) {
				transform.push()
				val t = i / metrics.numPoints.toFloat()
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
