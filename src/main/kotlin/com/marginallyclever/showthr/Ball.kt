package com.marginallyclever.showthr

import com.marginallyclever.showthr.Utilities.Companion.calculateDistanceRhoTheta

// Ball class for handling ball movement and position
internal class Ball(val name: String, val radius: Int, val settings: Settings) {
    internal var position: ThetaRho = ThetaRho(0.0, 0.0)
    private var target: ThetaRho = ThetaRho(0.0, 0.0)
    private val speed = 1.0 // Arbitrary speed value
    var atTarget: Boolean = false
    val ballRelaxedMargin = (radius * settings.RELAX_MARGIN).toInt()

    fun setPosition(thetaRho: ThetaRho) {
        this.position = thetaRho
    }

    fun setTarget(thetaRho: ThetaRho) {
        target = thetaRho
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
    fun updatePosition(deltaTime: Double) {
        //        val direction = Vector2d(target)
        //        direction.sub(position)
        //
        //        val len = direction.lengthSquared()
        //        if (len < speed * deltaTime) { // don't overshoot
        //            position.set(target)
        //            atTarget = true
        //        }
        //        else {
        //            direction.normalize()
        //            direction.scale(speed * deltaTime)
        //            position.add(direction)
        //            atTarget = false
        //        }
        // take the deltas
        //        val distance = calculateDistanceRhoTheta(position, target)
        val deltaTheta = speed * deltaTime * (target.theta - position.theta)
        val deltaRho = speed * deltaTime * (target.rho - position.rho)
        position = ThetaRho(position.theta + deltaTheta,position.rho + deltaRho )
        val distance = calculateDistanceRhoTheta(position, target)
        atTarget = distance < 0.01
//        println("position: $position, distance: $distance  ")
    }

    fun getRho(): Double {
        //        return calculateRho(position.x.toInt(), position.y.toInt(), settings)
        return position.rho
    }

    fun getTheta(): Double {
        //        return calculateTheta(position.x.toInt(), position.y.toInt(), settings)
        return position.theta
    }

    override fun toString(): String {
        return "Ball(name='$name',  Position:($position),  target:($target), speed=$speed, atTarget=$atTarget)"
    }
}
