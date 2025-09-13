package com.marginallyclever.showthr

import com.nurflugel.showthr.NormalizedThetaRho
import com.nurflugel.showthr.Settings
import com.nurflugel.showthr.Settings.Companion.RELAX_MARGIN
import com.nurflugel.showthr.Utilities.Companion.calculateDistanceRhoTheta
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min

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
     *
     * @return True if at target, false if not yet
     */
    fun updatePosition(): Boolean {

        val distance = calculateDistanceRhoTheta(position, target)
        val proposedLength = settings.proposedLength
        val weOvershot = proposedLength > distance
        val weAreThere = (distance - proposedLength) < 0.0001
        val thetaDistance = abs(target.theta - position.theta)

        val closeEnoughDistance = weAreThere || weOvershot
        val closeEnoughTheta = thetaDistance < 0.0001

        if (closeEnoughDistance && closeEnoughTheta) { // we're there!  Or, close enough.
            atTarget = true
            position = target
            return true
            //            println("$name at target, distance: $distance, settings.proposedLength: ${settings.proposedLength}")
        }
        else {  // todo - if closeEnoughDistance, then check for theta - here, since closeEnoughDistance is true, we'd overshoot if we tried it again,
            val percentageOfProposedLength = proposedLength / distance
            //            val percentageOfProposedTheta = proposedLength / distance
            // Now, assume a constant change in theta and rho (reasonable, especially for small changes)
            var deltaRho = 0.0
            var deltaTheta = 0.0
            when {
                percentageOfProposedLength < 1.0 -> { // we'll need at least one more step to get to the target
                    deltaRho = percentageOfProposedLength * (target.rho - position.rho)
                    deltaTheta = percentageOfProposedLength * (target.theta - position.theta)
                }

                else                             -> {
                    deltaRho = 0.0  // we're at the right rho, but theta is off - adjust theta
                    deltaTheta = min(PI / 20, (target.theta - position.theta))
                }
            }
            //                        val deltaTheta = percentageOfProposedLength * (target.theta - position.theta)
//            println("$name Delta theta: $deltaTheta, Delta rho: $deltaRho")
            val updatedTheta = position.theta + deltaTheta
            val updatedRho = position.rho + deltaRho
            position = NormalizedThetaRho(updatedTheta, updatedRho)
            if (deltaRho < .00001 && deltaTheta < .00001) {
                atTarget = true
                return true
            }
            else
                return false
            //            println("theta: ${position.theta}, rho: ${position.rho}, distance: $distance, percentageOfProposedLength=$percentageOfProposedLength in pixels: ${percentageOfProposedLength * settings.tableDiameter} ")
        }
    }

    override fun toString(): String {
        return "Ball(name='$name',  Position:($position),  target:($target), speed=$settings.speed, atTarget=$atTarget)"
    }
}
