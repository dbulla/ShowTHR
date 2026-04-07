package com.marginallyclever.showthr

import com.nurflugel.showthr.RhoTheta
import com.nurflugel.showthr.Settings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.Math.PI
import javax.vecmath.Vector2d
import kotlin.math.atan2

internal class BallTest {

    private lateinit var settings: Settings

    @BeforeEach
    fun setup() {
        settings = Settings().apply { tableDiameterWithPadding = 200; calculateCenter() } // Assuming Settings has a parameterless constructor
    }

    @Test
    fun `test getRho with position at origin`() {
        val ball = Ball("TestBall", 10, settings)
        ball.setPositionRhoTheta(RhoTheta(0.0, 0.0))
        assertEquals(0.0, ball.getRhoTheta().rho, 0.0001)
    }

    @Test
    fun `test getRho with position on positive X-axis`() {
        val ball = Ball("TestBall", 10, settings)
        ball.setPositionRhoTheta(RhoTheta(1.0, 0.0))
        val actual = ball.getRhoTheta().rho
        assertEquals(1.0, actual, 0.0001)
    }

    @Test
    fun `test getRho with position on positive Y-axis`() {
        val ball = Ball("TestBall", 10, settings)
        ball.setPositionRhoTheta(RhoTheta(1.0, PI / 2))
        assertEquals(1.0, ball.getRhoTheta().rho, 0.0001)
    }

    @Test
    fun `test getRho with position on diagonal`() {
        val ball = Ball("TestBall", 10, settings)
        ball.setPositionRhoTheta(RhoTheta(1.0, PI / 4))
        assertEquals(1.0, ball.getRhoTheta().rho, 0.0001)
    }

    @Test
    fun `test getRho with position on negative X-axis`() {
        val ball = Ball("TestBall", 10, settings)
        ball.setPositionRhoTheta(RhoTheta(1.0, PI))
        assertEquals(1.0, ball.getRhoTheta().rho, 0.0001)
    }

    @Test
    fun `test getRho with position on negative Y-axis`() {
        val ball = Ball("TestBall", 10, settings)
        ball.setPositionRhoTheta(RhoTheta(1.0, PI * 3 / 2))
        assertEquals(1.0, ball.getRhoTheta().rho, 0.0001)
    }

    @Test
    fun `test getTheta with position at origin`() {
        val ball = Ball("TestBall", 10, settings)
        ball.setPositionRhoTheta(RhoTheta(1.0, 0.0))
        assertEquals(0.0, ball.getRhoTheta().theta, 0.0001) // Assuming 0.0 radians at origin
    }

    @Test
    fun `test getTheta with position on positive X-axis`() {
        val ball = Ball("TestBall", 10, settings)
        ball.setPositionRhoTheta(RhoTheta(1.0, 0.0))
        assertEquals(0.0, ball.getRhoTheta().theta, 0.0001) // 0 radians (or 0 degrees)
    }

    @Test
    fun `test getTheta with position on positive Y-axis`() {
        val ball = Ball("TestBall", 10, settings)
        ball.setPositionRhoTheta(RhoTheta(1.0, PI / 2))
        assertEquals(PI / 2, ball.getRhoTheta().theta, 0.0001) // Pi/2 radians (90 degrees)
    }

    @Test
    fun `test getTheta with position on negative X-axis`() {
        val ball = Ball("TestBall", 10, settings)
        ball.setPositionRhoTheta(RhoTheta(1.0, PI))
        assertEquals(PI, ball.getRhoTheta().theta, 0.0001) // Pi radians (180 degrees)
    }

    @Test
    fun `test getTheta with position on negative Y-axis`() {
        val ball = Ball("TestBall", 10, settings)
        ball.setPositionRhoTheta(RhoTheta(1.0, PI * 3 / 2))
        assertEquals(PI * 3 / 2, ball.getRhoTheta().theta, 0.0001) // -Pi/2 radians (-90 degrees)
    }

    @Test
    fun `test getTheta with position in first quadrant`() {
        val ball = Ball("TestBall", 10, settings)
        ball.positionXy = Vector2d(3.0, 4.0) // Angle calculated: arctan(4/3)
        ball.updateRhoThetaPosition()
        val theta = ball.getRhoTheta().theta

        assertEquals(atan2(4.0, 3.0), theta, 0.0001)
    }

    @Test
    fun `test getTheta with position in second quadrant`() {
        val ball = Ball("TestBall", 10, settings)
        ball.positionXy = Vector2d( - 3.0,  4.0) // Angle calculated: arctan(4/-3)
        ball.updateRhoThetaPosition()
        val theta = ball.getRhoTheta().theta

        assertEquals(atan2(4.0, -3.0), theta, 0.0001)
    }

    @Test
    fun `test getTheta with position in third quadrant`() {
        val ball = Ball("TestBall", 10, settings)
        val x = -30.0
        val y = -40.0
        ball.positionXy = Vector2d( x, y) // Angle calculated: arctan(-4/-3)
        ball.updateRhoThetaPosition()
        val theta = ball.getRhoTheta().theta

        assertEquals(atan2(y, x), theta, 0.0001)
    }

    @Test
    fun `test getTheta with position in fourth quadrant`() {
        val ball = Ball("TestBall", 10, settings)
        ball.positionXy = Vector2d( 3.0,  -4.0) // Angle calculated: arctan(-4/3)
        ball.updateRhoThetaPosition()
        val actual = ball.getRhoTheta().theta
        val expected = atan2(-4.0, 3.0)
        assertEquals(expected, actual, 0.0001)
    }
}