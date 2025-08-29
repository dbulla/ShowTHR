package com.marginallyclever.showthr

import com.marginallyclever.showthr.Settings.Companion.centerX
import com.marginallyclever.showthr.Settings.Companion.centerY
import com.marginallyclever.showthr.Settings.Companion.width
import com.marginallyclever.showthr.Utilities.Companion.calculateX
import com.marginallyclever.showthr.Utilities.Companion.calculateY
import javax.vecmath.Vector2d

// Ball class for handling ball movement and position
internal class Ball(val radius: Int) {
    internal var position = Vector2d(0.0, 0.0)
    private val target = Vector2d(0.0, 0.0)
    private val speed = 1.0 // Arbitrary speed value
    var atTarget: Boolean = false

    fun setPositionThetaRho(theta: Double, rho: Double) {
        position.x = calculateX(theta, rho )
        position.y = calculateY(theta, rho )
    }

//    fun setTarget(x: Double, y: Double) {
//        target[x] = y
//        val diff = Vector2d(target)
//        diff.sub(position)
//        atTarget = diff.lengthSquared() < 0.1
//    }

    fun setTargetThetaRho(theta: Double, rho: Double) {
        val x = calculateX(theta, rho)
        val y = calculateY(theta, rho)
        target[x] = y
        val diff = Vector2d(target)
        diff.sub(position)
        atTarget = diff.lengthSquared() < 0.1
    }

    /**
     * This currently draws a straight line from the ball's current position to its target.
     *
     * What we want to do is draw a curve if theta changes, but rho does not. This is a LOT more complicated than it sounds like, as position is in X, Y coordinates, and easy math is in theta, rho.
     * This is fixed in SandSimulation.expandSequence(), where we tweak the initial theta and rho to produce an expanded list where the current
     * functionality will work even though it's wrong.
     */
    fun updatePosition(deltaTime: Double) {
        val direction = Vector2d(target)
        direction.sub(position)
        val len = direction.lengthSquared()
        if (len < speed * deltaTime) {
            position.set(target)
            atTarget = true
        }
        else {
            direction.normalize()
            direction.scale(speed * deltaTime)
            position.add(direction)
            atTarget = false
        }
    }
}
