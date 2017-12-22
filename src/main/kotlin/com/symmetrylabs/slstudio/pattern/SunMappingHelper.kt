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

class SunMappingHelper(lx: LX) : KPattern(lx) {
	val enabledParam = booleanParam("ON", false)
	val sunIndex = discreteParameter("SUN", 0, 0, model.suns.size)
	val stripIndex = discreteParameter("STRIP", 0, 0, 1)

	val stripRotationCoarse = compoundParam("ROT COARSE", 0.0, -12.0, 12.0)
	val stripRotationFine = compoundParam("ROT FINE", 0.0, -2.0, 2.0)

	val stripLength = discreteParameter("LENGTH", 0, 0, 180)


	val ledPitch = INCHES_PER_METER / 60

	val selectedSun get() = model.suns[sunIndex.valuei]
	val selectedStrip get() = selectedSun.strips[stripIndex.valuei] as CurvedStrip

	init {
		stripLength.shouldSerialize = false
//		enabledParam.addListener { enableSunOutputs() }
		sunIndex.addListener {
			stripIndex.setRange(0, selectedSun.strips.size)
//			enableSunOutputs()
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
		stripLength.addListener {
			val previousStripLength = FultonStreetLayout.stripLength(selectedSun, selectedStrip)
			FultonStreetLayout.updateStripLength(selectedSun, selectedStrip, it.value.toInt())
			val delta = -(it.value.toInt() - previousStripLength)
			if (delta == 0) return@addListener
			var nextStripIndex = (selectedSun.strips.indexOf(selectedStrip) + 1)
			while (nextStripIndex < selectedSun.strips.size) {
				val nextStrip = selectedSun.strips[nextStripIndex] as CurvedStrip
				if (nextStrip.sliceId != selectedStrip.sliceId) {
					break
				}
				val previousNextStripLength = FultonStreetLayout.stripLength(selectedSun, nextStrip)
				if (previousNextStripLength == 0) {
					nextStripIndex++
					continue
				}
				FultonStreetLayout.updateStripLength(selectedSun, nextStrip, previousNextStripLength + delta)
				break
			}
//			System.out.println("Sun ${selectedSun.id}, strip ${selectedStrip.fixture.id} length = $length")
		}
	}

	private fun updateStripRotationParam() {
		stripRotationCoarse.value = FultonStreetLayout.rotationForStrip(
			selectedSun,
			selectedSun.strips.indexOf(selectedStrip)
		).double
		stripRotationFine.value = 0.0
		stripLength.value = FultonStreetLayout.stripLength(selectedSun, selectedStrip).toDouble()
	}

	private fun enableSunOutputs() {
		if (enabledParam.isOn) {
			lx.engine.output.enabled.setValue(true)
			SLStudio.applet.pixlites.forEach { it.enabled.setValue(it.slice.id.startsWith(selectedSun.id)) }
		}
	}

	override fun run(v: Double) {
		if (enabledParam.isOn) {
			colors.fill(LXColor.BLACK)
			selectedSun.points.forEach { point ->
				val distance = point.x - selectedSun.center.x

				if (distance.abs < ledPitch) {
					val brightness = distance / ledPitch
					point.color = LX.hsb(
						0f,
						100f,
						brightness * 100
					)
//				} else {
//					point.color = 0xFF001100.toInt()
				}
			}

			model.suns.forEach { sun ->
				val sunStripLengths = FultonStreetLayout.stripLengths(sun.id)!!
				var afterIndex = -1
				sun.slices.forEach { slice ->
					var runningIndex = slice.points[0].index
					val sliceStripLengths = sunStripLengths[slice.id]!!
					slice.strips.forEach { strip ->
						val stripLengths = sliceStripLengths[Integer.parseInt(strip.id)].toInt()
						if (stripLengths > 0) {
							val firstIndex = runningIndex
							val lastIndex = runningIndex + stripLengths - 1

							if (sun == selectedSun) {
								if (strip == selectedStrip) {
									for (i in firstIndex..lastIndex) {
										colors[constrain(i, 0, colors.size-1)] = LXColor.rgb(50,50,50)
									}
									colors[constrain(lastIndex, 0, colors.size-1)] = LXColor.GREEN
									afterIndex = constrain(lastIndex + 1, 0, colors.size-1)

								}
							}

							runningIndex += stripLengths
						}
					}
					if (afterIndex != -1) colors[afterIndex] = LXColor.BLUE
				}
			}
			selectedStrip.points.forEach { point ->
				val distance = point.x - selectedSun.center.x

				if (distance.abs < ledPitch) {
					val brightness = distance / ledPitch
					point.color = LX.hsb(
						50f,
						0f,
						brightness * 100
					)
				}
			}
		}
	}

	fun constrain(a: Int, min: Int, max: Int): Int {
		return if (a < min) min else if (a > max) max else a
	}
}


private val PVector.zxAngle inline get() = Math.atan2(z.double, x.double)

private operator fun LXPoint.minus(other: PVector) = PVector(this.x - other.x, this.y - other.y, this.z - other.z)
private fun LXPoint.copy() = LXPoint(this.x, this.y, this.z)
