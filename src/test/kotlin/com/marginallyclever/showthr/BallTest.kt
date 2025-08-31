package com.marginallyclever.showthr

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.vecmath.Vector2d

internal class BallTest {

    private lateinit var settings: Settings

    @BeforeEach
    fun setup() {
        settings = Settings().apply { tableDiameter = 200 } // Assuming Settings has a parameterless constructor
        settings.calculateCenter()
    }

    @Test
    fun `test getRho with position at origin`() {
        val ball = Ball("TestBall", 5, settings)
        ball.setPositionThetaRho(0.0,0.0)
        assertEquals(0.0, ball.getRho(), 0.0001)
    }

    @Test
    fun `test getRho with position on positive X-axis`() {
        val ball = Ball("TestBall", 5, settings)
        ball.setPositionThetaRho(0.0,1.0)
        val actual = ball.getRho()
        assertEquals(1.0, actual, 0.0001)
    }

    @Test
    fun `test getRho with position on positive Y-axis`() {
        val ball = Ball("TestBall", 5, settings)
        ball.setPositionThetaRho(Math.PI/2,1.0)
        assertEquals(1.0, ball.getRho(), 0.0001)
    }

    @Test
    fun `test getRho with position on diagonal`() {
        val ball = Ball("TestBall", 5, settings)
        ball.setPositionThetaRho(Math.PI/4,1.0)
        assertEquals(1.0, ball.getRho(), 0.0001)
    }

    @Test
    fun `test getRho with position on negative X-axis`() {
        val ball = Ball("TestBall", 5, settings)
        ball.setPositionThetaRho(Math.PI,1.0)
        assertEquals(1.0, ball.getRho(), 0.0001)
    }

    @Test
    fun `test getRho with position on negative Y-axis`() {
        val ball = Ball("TestBall", 10, settings)
        ball.setPositionThetaRho(Math.PI*3/2,1.0)
        assertEquals(10.0, ball.getRho(), 0.0001)
    }

    @Test
    fun `test getTheta with position at origin`() {
        val ball = Ball("TestBall", 10, settings)
        ball.setPositionThetaRho(0.0,1.0)
        assertEquals(0.0, ball.getTheta(), 0.0001) // Assuming 0.0 radians at origin
    }

    @Test
    fun `test getTheta with position on positive X-axis`() {
        val ball = Ball("TestBall", 10, settings)
        ball.setPositionThetaRho(0.0,1.0)
        assertEquals(0.0, ball.getTheta(), 0.0001) // 0 radians (or 0 degrees)
    }

    @Test
    fun `test getTheta with position on positive Y-axis`() {
        val ball = Ball("TestBall", 10, settings)
        ball.setPositionThetaRho(Math.PI/2,1.0)
        assertEquals(Math.PI / 2, ball.getTheta(), 0.0001) // Pi/2 radians (90 degrees)
    }

    @Test
    fun `test getTheta with position on negative X-axis`() {
        val ball = Ball("TestBall", 10, settings)
        ball.setPositionThetaRho(Math.PI,1.0)
        assertEquals(Math.PI, ball.getTheta(), 0.0001) // Pi radians (180 degrees)
    }

    @Test
    fun `test getTheta with position on negative Y-axis`() {
        val ball = Ball("TestBall", 10, settings)
        ball.setPositionThetaRho(Math.PI*3/2,1.0)
        assertEquals(-Math.PI / 2, ball.getTheta(), 0.0001) // -Pi/2 radians (-90 degrees)
    }

    @Test
    fun `test getTheta with position in first quadrant`() {
        val ball = Ball("TestBall", 10, settings)
        ball.position = Vector2d(3.0, 4.0) // Angle calculated: arctan(4/3)

        val theta = ball.getTheta()

        assertEquals(Math.atan2(4.0, 3.0), theta, 0.0001)
    }

    @Test
    fun `test getTheta with position in second quadrant`() {
        val ball = Ball("TestBall", 10, settings)
        ball.position = Vector2d(-3.0, 4.0) // Angle calculated: arctan(4/-3)

        val theta = ball.getTheta()

        assertEquals(Math.atan2(4.0, -3.0), theta, 0.0001)
    }

    @Test
    fun `test getTheta with position in third quadrant`() {
        val ball = Ball("TestBall", 10, settings)
        ball.position = Vector2d(-3.0, -4.0) // Angle calculated: arctan(-4/-3)

        val theta = ball.getTheta()

        assertEquals(Math.atan2(-4.0, -3.0), theta, 0.0001)
    }

    @Test
    fun `test getTheta with position in fourth quadrant`() {
        val ball = Ball("TestBall", 10, settings)
        ball.position = Vector2d(3.0, -4.0) // Angle calculated: arctan(-4/3)

        val theta = ball.getTheta()

        assertEquals(Math.atan2(-4.0, 3.0), theta, 0.0001)
    }
}