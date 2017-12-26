package com.symmetrylabs.slstudio.pattern

import com.symmetrylabs.slstudio.model.SLModel
import heronarts.lx.LX
import heronarts.lx.model.LXPoint

/**
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
abstract class KPattern(lx: LX) : SunsPattern(lx) {
	override fun getModel() = super.getModel() as SLModel

	// Allows setting the color of a point with point.color instead of colors[point.index] and is just as fast
	var LXPoint.color: Int
		inline get() = colors[this.index]
		inline set(c: Int) { colors[this.index] = c }
}
