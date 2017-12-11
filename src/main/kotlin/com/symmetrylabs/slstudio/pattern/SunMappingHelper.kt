package com.symmetrylabs.slstudio.pattern

import com.symmetrylabs.slstudio.model.CurvedStrip
import com.symmetrylabs.slstudio.model.Strip.INCHES_PER_METER
import com.symmetrylabs.slstudio.util.abs
import com.symmetrylabs.slstudio.util.double
import com.symmetrylabs.slstudio.util.float
import com.symmetrylabs.slstudio.util.radToDeg
import heronarts.lx.LX
import heronarts.lx.model.LXPoint
import processing.core.PVector

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
class SunMappingHelper(lx: LX) : KPattern(lx) {
	val sunIndex = discreteParameter("SUN", 0, 0, model.suns.size)
	val stripIndex = discreteParameter("STRIP", 0, 0, 1)

	val stripRotation = compoundParam("ROT", 0.0, -0.1, 0.1)

	val ledPitch = INCHES_PER_METER / 60

	val selectedSun get() = model.suns[sunIndex.valuei]
	val selectedStrip get() = selectedSun.strips[stripIndex.valuei] as CurvedStrip

	init {
		sunIndex.addListener { stripIndex.setRange(0, selectedSun.strips.size) }
		stripRotation.addListener {
			selectedStrip.updateOffset(it.valuef)
		}
	}

	override fun run(v: Double) {
		colors.fill(0)
		selectedSun.points.forEach { point ->
			val distance = point.x - selectedSun.center.x

			if (distance.abs < ledPitch) {
				val brightness = 1 - distance / ledPitch
				point.color = LX.hsb(0f, 100f, brightness*100)
			} else if (selectedStrip.points.contains(point)) {
				val angle = (point - selectedSun.center).zxAngle
				point.color = LX.hsb(angle.radToDeg.float, 100f, 100f)
			} else {
				point.color = 0xFF001100.toInt()
			}
		}
	}
}


private val PVector.zxAngle inline get() = Math.atan2(z.double, x.double)

private operator fun LXPoint.minus(other: PVector) = PVector(this.x - other.x, this.y - other.y, this.z - other.z)
private fun LXPoint.copy() = LXPoint(this.x, this.y, this.z)