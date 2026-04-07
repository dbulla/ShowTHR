package com.marginallyclever.showthr

import com.nurflugel.showthr.RhoTheta
import com.nurflugel.showthr.Utilities.Companion.calculateRho
import com.nurflugel.showthr.Utilities.Companion.calculateThetaInDegrees
import com.nurflugel.showthr.Utilities.Companion.calculateX
import com.nurflugel.showthr.Utilities.Companion.calculateY
import com.nurflugel.showthr.Settings
import com.nurflugel.showthr.Utilities.Companion.calculateTheta
import javax.vecmath.Vector2d

/**
 * Class for handling ball movement and position
 */
class Ball(val name: String, val radius: Int, val settings: Settings) {
    internal var positionXy: Vector2d = Vector2d()
    var startPosition: Vector2d = Vector2d()
    internal var positionRhoTheta: RhoTheta = RhoTheta(0.0, 0.0)
    internal var targetRhoTheta: RhoTheta = positionRhoTheta
    private var target: Vector2d = Vector2d()
    private val speed = 1.0 // Arbitrary speed value
    var atTarget: Boolean = false

    fun setPositionRhoTheta(rhoTheta: RhoTheta) {
        positionRhoTheta = rhoTheta
        positionXy.x = calculateX(rhoTheta, settings)
        positionXy.y = calculateY(rhoTheta, settings)
    }

    fun setTargetRhoTheta(rhoTheta: RhoTheta) {
        targetRhoTheta = rhoTheta
        val x = calculateX(rhoTheta, settings)
        val y = calculateY(rhoTheta, settings)
        target.set(x, y)
        val diff = Vector2d(target)
        diff.sub(positionXy)
        val lengthSquared = diff.lengthSquared()
        atTarget = lengthSquared < 0.1
        // we need this for the relaxation step
        startPosition = positionXy
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
    fun updatePosition(): RhoTheta {
        val deltaTime = settings.deltaTime
        val direction = Vector2d(target)
        direction.sub(positionXy)
        val len = direction.lengthSquared()
        if (len < speed * deltaTime) {
            positionXy.set(target)
            atTarget = true
        }
        else {
            direction.normalize()
            direction.scale(speed * deltaTime)
            positionXy.add(direction)
            atTarget = false
        }
        // make sure that the changes in x, y are echoed in rho and theta
        updateRhoThetaPosition()
        return positionRhoTheta
    }

    fun updateRhoThetaPosition() {
        val rho = calculateRho(positionXy.x, positionXy.y, settings)
        val theta = calculateTheta(positionXy.x, positionXy.y)
        positionRhoTheta = RhoTheta(rho, theta)
    }

    fun getRhoTheta(): RhoTheta {
        return positionRhoTheta
    }

    override fun toString(): String {
        val rho = calculateRho(positionXy.x, positionXy.y, settings)
        val theta = calculateThetaInDegrees(positionXy.x, positionXy.y)
        val targetRho = calculateRho(target.x, target.y, settings)
        val targetTheta = calculateThetaInDegrees(target.x, target.y)
        return "Ball(name='$name',  Position:(rho=$rho, theta=$theta), positionXY=$positionXy, target:(rho=$targetRho, theta=$targetTheta), $target, speed=$speed, atTarget=$atTarget,)"
    }
}
