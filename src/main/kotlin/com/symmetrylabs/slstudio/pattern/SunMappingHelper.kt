package com.symmetrylabs.slstudio.pattern

import com.symmetrylabs.slstudio.SLStudio
import com.symmetrylabs.slstudio.mappings.FultonStreetLayout
import com.symmetrylabs.slstudio.model.CurvedStrip
import com.symmetrylabs.slstudio.model.Strip.INCHES_PER_METER
import com.symmetrylabs.slstudio.util.abs
import com.symmetrylabs.slstudio.util.double
import heronarts.lx.LX
import heronarts.lx.color.LXColor
import heronarts.lx.model.LXPoint
import heronarts.lx.parameter.LXParameterListener
import processing.core.PVector

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
class SunMappingHelper(lx: LX) : KPattern(lx) {
	val enabledParam = booleanParam("ON", false)
	val sunIndex = discreteParameter("SUN", 0, 0, model.suns.size)
	val stripIndex = discreteParameter("STRIP", 0, 0, 1)

	val stripRotationCoarse = compoundParam("ROT FINE", 0.0, -12.0, 12.0)
	val stripRotationFine = compoundParam("ROT COARSE", 0.0, -2.0, 2.0)

	val stripFirst = discreteParameter("FIRST", 0, 0, 1)
	val stripLast = discreteParameter("LAST", 0, 0, 1)


	val ledPitch = INCHES_PER_METER / 60

	val selectedSun get() = model.suns[sunIndex.valuei]
	val selectedStrip get() = selectedSun.strips[stripIndex.valuei] as CurvedStrip

	init {
		enabledParam.addListener { enableSunOutputs() }
		sunIndex.addListener {
			stripIndex.setRange(0, selectedSun.strips.size)
			enableSunOutputs()
			updateStripRotationParam()
		}
		stripIndex.addListener {
			updateStripRotationParam()
		}
		val stripRotationListener = LXParameterListener {
			val offset = stripRotationCoarse.valuef + stripRotationFine.valuef
			selectedStrip.updateOffset(offset)

			SLStudio.applet.lx.ui.preview.pointCloud.updateVertexPositions()
			FultonStreetLayout.updateRotation(selectedSun, selectedStrip, offset)
		}
		stripRotationCoarse.addListener(stripRotationListener)
		stripRotationFine.addListener(stripRotationListener)
		val stripLengthListener = LXParameterListener {
			var length = stripLast.valuei - stripFirst.valuei + 1
			if (selectedStrip.points.isEmpty()) length = 0
			FultonStreetLayout.updateStripLength(selectedSun, selectedStrip, length)
			System.out.println("Sun ${selectedSun.id}, strip ${selectedStrip.fixture.id} length = $length")
		}
		stripFirst.addListener(stripLengthListener)
		stripLast.addListener(stripLengthListener)
	}

	private fun updateStripRotationParam() {
		stripRotationCoarse.value = FultonStreetLayout.rotationForStrip(
			selectedSun,
			selectedSun.strips.indexOf(selectedStrip)
		).double
		stripRotationFine.value = 0.0
		stripFirst.range = if (selectedStrip.points.isNotEmpty()) selectedStrip.points.size else 1
		stripFirst.value = 0.0
		stripLast.range = if (selectedStrip.points.isNotEmpty()) selectedStrip.points.size else 1
		stripLast.value = if (selectedStrip.points.isNotEmpty()) (selectedStrip.points.size - 1).double else 0.0
	}

	private fun enableSunOutputs() {
		if (enabledParam.isOn) {
			lx.engine.output.enabled.setValue(true)
			SLStudio.applet.pixlites.forEach { it.enabled.setValue(it.slice.id.startsWith(selectedSun.id)) }
		}
	}

	override fun run(v: Double) {
		if (enabledParam.isOn) {
			colors.fill(0)
			selectedSun.points.forEach { point ->
				val distance = point.x - selectedSun.center.x

				if (distance.abs < ledPitch) {
					val brightness = distance / ledPitch
					point.color = LX.hsb(
						0f,
						if (selectedStrip.points.contains(point)) 0f else 100f,
						brightness * 100
					)
				} else {
					point.color = 0xFF001100.toInt()
				}
			}
			selectedSun.strips.forEach { strip ->
				if (strip.points.isNotEmpty()) {
					strip.points.first().color = LXColor.BLUE
					strip.points.last().color = LXColor.BLUE
				}
			}
			if (selectedStrip.points.isNotEmpty()) {
				selectedStrip.points[stripFirst.valuei].color = LXColor.GREEN
				selectedStrip.points[stripLast.valuei].color = LXColor.GREEN
			}
		}
	}
}


private val PVector.zxAngle inline get() = Math.atan2(z.double, x.double)

private operator fun LXPoint.minus(other: PVector) = PVector(this.x - other.x, this.y - other.y, this.z - other.z)
private fun LXPoint.copy() = LXPoint(this.x, this.y, this.z)
