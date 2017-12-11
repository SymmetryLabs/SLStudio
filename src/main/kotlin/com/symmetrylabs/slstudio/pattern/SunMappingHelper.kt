package com.symmetrylabs.slstudio.pattern

import com.symmetrylabs.slstudio.SLStudio
import com.symmetrylabs.slstudio.mappings.FultonStreetLayout
import com.symmetrylabs.slstudio.model.CurvedStrip
import com.symmetrylabs.slstudio.model.Strip.INCHES_PER_METER
import com.symmetrylabs.slstudio.util.abs
import com.symmetrylabs.slstudio.util.double
import heronarts.lx.LX
import heronarts.lx.model.LXPoint
import processing.core.PVector

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
class SunMappingHelper(lx: LX) : KPattern(lx) {
	val sunIndex = discreteParameter("SUN", 0, 0, model.suns.size)
	val stripIndex = discreteParameter("STRIP", 0, 0, 1)

	val stripRotation = compoundParam("ROT", 0.0, -0.08, 0.08)

	val ledPitch = INCHES_PER_METER / 60

	val selectedSun get() = model.suns[sunIndex.valuei]
	val selectedStrip get() = selectedSun.strips[stripIndex.valuei] as CurvedStrip

	init {
		sunIndex.addListener {
			stripIndex.setRange(0, selectedSun.strips.size)
			lx.engine.output.enabled.setValue(true)
			SLStudio.applet.pixlites.forEach { it.enabled.setValue(it.slice.id.startsWith(selectedSun.id)) }
			stripRotation.value = FultonStreetLayout.rotationForStrip(selectedSun, selectedSun.strips.indexOf(selectedStrip)).double
		}
		stripIndex.addListener {
			stripRotation.value = FultonStreetLayout.rotationForStrip(selectedSun, selectedSun.strips.indexOf(selectedStrip)).double
		}
		stripRotation.addListener {
			selectedStrip.updateOffset(it.valuef)

			SLStudio.applet.lx.ui.preview.pointCloud.updateVertexPositions()
			FultonStreetLayout.updateRotation(selectedSun, selectedStrip, it.valuef)
		}
	}

	override fun run(v: Double) {
		colors.fill(0)
		selectedSun.points.forEach { point ->
			val distance = point.x - selectedSun.center.x

			if (distance.abs < ledPitch) {
				val brightness = distance / ledPitch
				point.color = LX.hsb(
					0f,
					if (selectedStrip.points.contains(point)) 0f else 100f,
					brightness*100
				)
			} else {
				point.color = 0xFF001100.toInt()
			}
		}
	}
}


private val PVector.zxAngle inline get() = Math.atan2(z.double, x.double)

private operator fun LXPoint.minus(other: PVector) = PVector(this.x - other.x, this.y - other.y, this.z - other.z)
private fun LXPoint.copy() = LXPoint(this.x, this.y, this.z)