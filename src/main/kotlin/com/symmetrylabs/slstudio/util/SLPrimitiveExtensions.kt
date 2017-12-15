package com.symmetrylabs.slstudio.util

// Conversions
val Int.float inline get() = this.toFloat()
val Int.double inline get() = this.toDouble()

val Double.float inline get() = this.toFloat()
val Double.int inline get() = this.toInt()

val Float.double inline get() = this.toDouble()
val Float.int inline get() = this.toInt()

// abs
val Int.abs inline get() = if (this < 0) - this else this
val Float.abs inline get() = if (this < 0) - this else this
val Double.abs inline get() = if (this < 0) - this else this

// floor
val Float.floor inline get() = Math.floor(this.toDouble()).toFloat()
val Double.floor inline get() = Math.floor(this)

// ceil
val Float.ceil inline get() = Math.ceil(this.toDouble()).toFloat()
val Double.ceil inline get() = Math.ceil(this)


// Angles
val Double.radToDeg inline get() = this * 180/Math.PI
val Double.degToRan inline get() = this * Math.PI/180

val Float.radToDeg inline get() = (this * 180/Math.PI).float
val Float.degToRad inline get() = (this * Math.PI/180).float
