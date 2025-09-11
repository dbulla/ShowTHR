package com.marginallyclever.showthr

import com.nurflugel.showthr.NormalizedThetaRho
import com.nurflugel.showthr.Settings
import com.nurflugel.showthr.Settings.Companion.RELAX_MARGIN
import com.nurflugel.showthr.Utilities.Companion.calculateDistanceRhoTheta

// Ball class for handling ball movement and position
internal class Ball(val name: String, val radius: Int, val settings: Settings) {
    internal var position: NormalizedThetaRho = NormalizedThetaRho(0.0, 0.0)
    private var target: NormalizedThetaRho = NormalizedThetaRho(0.0, 0.0)

    var atTarget: Boolean = false
    val ballRelaxedMargin = (radius * RELAX_MARGIN).toInt()

    fun setPosition(normalizedThetaRho: NormalizedThetaRho) {
        this.position = normalizedThetaRho
    }

    fun setTarget(normalizedThetaRho: NormalizedThetaRho) {
        target = normalizedThetaRho
        val distance = calculateDistanceRhoTheta(position, target)
        atTarget = distance < .001
    }

    /**
     * This currently draws a straight line from the ball's current position to its target.
     *
     * What we want to do is draw a curve if theta changes, but rho does not. This is a LOT more complicated than it sounds like, as position is
     * in X, Y coordinates, and easy math is in theta, rho.
     *
     * This is fixed in SandSimulation.expandSequence(), where we tweak the initial theta and rho to produce an expanded list where the current
     * functionality will work even though it's wrong.
     */
    fun updatePosition(settings: Settings) {

        val distance = calculateDistanceRhoTheta(position, target)
        if (settings.proposedLength >= distance || (distance - settings.proposedLength) < 0.0001) { // we're there!  Or, close enough.   todo be clever enough to do this w/o the ||
            atTarget = true
            position = target
            println("$name at target, distance: $distance, settings.proposedLength: ${settings.proposedLength}")
        }
        else {
            val percentageOfProposedLength = settings.proposedLength / distance
            // Now, assume a constant change in theta and rho (reasonable, especially for small changes)
            val deltaRho = percentageOfProposedLength * (target.rho - position.rho)
            val deltaTheta = percentageOfProposedLength * (target.theta - position.theta)
            val updatedTheta = position.theta + deltaTheta
            val updatedRho = position.rho + deltaRho
            position = NormalizedThetaRho(updatedTheta, updatedRho)
//            println("theta: ${position.theta}, rho: ${position.rho}, distance: $distance, percentageOfProposedLength=$percentageOfProposedLength in pixels: ${percentageOfProposedLength * settings.tableDiameter} ")
        }
    }

    override fun toString(): String {
        return "Ball(name='$name',  Position:($position),  target:($target), speed=$settings.speed, atTarget=$atTarget)"
    }
}
