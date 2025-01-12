package com.marginallyclever.showthr

import javax.vecmath.Vector2d

// Ball class for handling ball movement and position
internal class Ball(val radius: Double) {
    internal var position = Vector2d(0.0, 0.0)
    private val target = Vector2d(0.0, 0.0)
    private val speed = 1.0 // Arbitrary speed value
    var atTarget: Boolean = false

    fun setTarget(x: Double, y: Double) {
        target[x] = y
        val diff = Vector2d(target)
        diff.sub(position)
        atTarget = diff.lengthSquared() < 0.1
    }

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
