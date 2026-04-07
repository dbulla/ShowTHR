package com.marginallyclever.showthr

import com.nurflugel.showthr.RhoTheta
import com.nurflugel.showthr.Utilities.Companion.calculateRho
import com.nurflugel.showthr.Utilities.Companion.calculateThetaInDegrees
import com.nurflugel.showthr.Utilities.Companion.calculateX
import com.nurflugel.showthr.Utilities.Companion.calculateY
import com.nurflugel.showthr.Utilities.Companion.setValueFromArg
import com.nurflugel.showthr.Settings
import java.lang.Math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.math.cos
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class UtilitiesTest {

    /**
     * Tests for the calculateY function in the Companion class of Utilities.
     * This function calculates the y-coordinate based on the given angle (theta),
     * radial distance ratio (rho), and settings object.
     */

    @Test
    fun `test calculateY with zero values`() {
        val rhoTheta = RhoTheta(0.0, 0.0)
        val settings = Settings().apply { baseTableDiameter = 1000; calculateCenter() }

        val result = calculateY(rhoTheta, settings)

        assertEquals(0.0, result, "calculateY should return the same centerY when rho is zero")
    }

    @Test
    fun `test calculateY with positive theta and rho`() {
        val theta = PI / 4  //45 degrees
        val rho = 0.5
        val rhoTheta = RhoTheta(rho, theta)
        val settings = Settings().apply { baseTableDiameter = 600; calculateCenter() }
        val result = calculateY(rhoTheta, settings)
        val expected =  cos(theta) * rho * settings.tableRadius
        assertEquals(expected, result, 0.0001, "calculateY should compute the correct y value for positive theta and rho")
    }

    @Test
    fun `test calculateY with negative theta and positive rho`() {
        val theta = -PI / 2
        val rho = 1.0
        val rhoTheta = RhoTheta(rho, theta)
        val settings = Settings().apply { baseTableDiameter = 800; calculateCenter() }
        val result = calculateY(rhoTheta, settings)
        assertEquals(-380.0, result, 0.0001, "calculateY should compute the correct y value for negative theta and positive rho")
    }

    @Test
    fun `test calculateY with theta as PI`() {
        val rhoTheta = RhoTheta(1.0, PI)
        val settings = Settings().apply { baseTableDiameter = 1200; calculateCenter() }
        val result = calculateY(rhoTheta, settings)
        assertEquals(0.0, result, 0.0001, "calculateY should compute the correct y value when theta is PI")
    }

    @Test
    fun `test calculateY with maximum rho`() {
        val theta = 0.0
        val rho = 1.0
        val rhoTheta = RhoTheta(rho, theta)
        val settings = Settings().apply { baseTableDiameter = 1400; calculateCenter() }
        val result = calculateY(rhoTheta, settings)
        assertEquals(0.0, result, 0.0001, "calculateY should compute the correct y value for maximum rho")
    }

    @Test
    fun `test calculate normalized Rho with point at the center of the table`() {
        val settings = Settings().apply { baseTableDiameter = 200; calculateCenter() }
        settings.calculateCenter()
        val x = 0.0
        val y = 0.0
        val result = calculateRho(x, y, settings)
        assertEquals(0.0, result, "calculateRho should return 0 when the point is at the center of the table")
    }

    @Test
    fun `test calculate normalized Rho with point at the edge of the table radius`() {
        val settings = Settings().apply { baseTableDiameter = 200; calculateCenter() }
        val result = calculateRho(100.0, 0.0, settings)
        assertEquals(1.25, result, "calculateRho should return 1 when the point is at the edge of the table radius")
    }

    @Test
    fun `test calculate normalized Rho with point outside the table radius`() {
        val settings = Settings().apply { baseTableDiameter = 200; calculateCenter() }
        val x = 400.0
        val y = 100.0
        val result = calculateRho(x, y, settings)
        assertTrue(result > 1.0, "calculateRho should return a value greater than 1 when the point is outside the table radius")
    }

    @Test
    fun `test calculate normalized Rho with negative x and y coordinates`() {
        val settings = Settings().apply { baseTableDiameter = 400; calculateCenter() }
        val result = calculateRho(0.0, 0.0, settings)
        val expected = 0.0
        assertEquals(expected, result, "calculateRho should correctly calculate rho for negative coordinates")
    }

    @Test
    fun `test calculateX with zero theta and rho`() {
        val settings = Settings().apply { baseTableDiameter = 200; calculateCenter() }
        val rhoTheta = RhoTheta(0.0, 0.0)
        val result = calculateX(rhoTheta, settings)
        assertEquals(0.0, result, "calculateX should return centerX when theta and rho are 0")
    }

    @Test
    fun `test calculateX with positive theta and rho`() {
        val settings = Settings().apply { baseTableDiameter = 200; calculateCenter() }
        val rhoTheta = RhoTheta(1.0, PI / 2)
        val result = calculateX(rhoTheta, settings)
        assertEquals(0.0, result, 0.00001)
    }

    @Test
    fun `test calculateX with negative theta`() {
        val settings = Settings().apply { baseTableDiameter = 200; calculateCenter() }
        val rhoTheta = RhoTheta(1.0, -PI / 2)
        val result = calculateX(rhoTheta, settings)
        val expected = 0.0
        assertEquals(expected, result, 0.00001)
    }

    @Test
    fun `test calculateX with zero rho`() {
        val settings = Settings().apply { baseTableDiameter = 200; calculateCenter() }
        val rhoTheta = RhoTheta(0.0, PI / 3)
        val result = calculateX(rhoTheta, settings)
        assertEquals(0.0, result, "calculateX should return centerX when rho is 0, regardless of theta")
    }

    @Test
    fun `test calculateX with maximum rho`() {
        val settings = Settings().apply { baseTableDiameter = 200; calculateCenter() }
        val theta = PI / 2 // straight up
        val rho = 1.0
        val rhoTheta = RhoTheta(rho, theta)
        val result = calculateX(rhoTheta, settings)
        val expected = 0.0
        assertEquals(expected, result, 0.00001)
    }

    @Test
    fun `test setValueFromArg with valid index`() {
        val args = arrayOf("arg1", "arg2", "arg3")
        val result = setValueFromArg(1, args)
        assertEquals("arg2", result, "setValueFromArg should return the argument at the specified index")
    }

    @Test
    fun `test setValueFromArg with missing index error`() {
        val args = arrayOf("arg1", "arg2", "arg3")
        val exception = assertFailsWith<IllegalArgumentException> {
            setValueFromArg(3, args)
        }
        assertEquals("Missing value for arg3", exception.message, "setValueFromArg should throw an exception with the correct missing value error")
    }

    @Test
    fun `test calculateTheta with point on positive X-axis`() {
        val settings = Settings().apply { baseTableDiameter = 200; calculateCenter() }
        val x = 200.0
        val y = 0.0
        val result = calculateThetaInDegrees(x, y)
        assertEquals(0.0, result, "calculateTheta should return 0 degrees when the point is on the positive X-axis")
    }

    @Test
    fun `test calculateTheta with point on positive Y-axis`() {
        val settings = Settings().apply { baseTableDiameter = 200; calculateCenter() }
        val x = 0.0
        val y = 200.0
        val result = calculateThetaInDegrees(x, y)
        assertEquals(90.0, result, "calculateTheta should return 90 degrees when the point is on the positive Y-axis")
    }

    @Test
    fun `test calculateTheta with point on negative X-axis`() {
        val settings = Settings().apply { baseTableDiameter = 200; calculateCenter() }
        val x = -100.0
        val y = 0.0
        val result = calculateThetaInDegrees(x, y)
        assertEquals(180.0, result, "calculateTheta should return 180 degrees when the point is on the negative X-axis")
    }

    @Test
    fun `test calculateTheta with point on negative Y-axis`() {
        val x = 0.0
        val y = -100.0
        val result = calculateThetaInDegrees(x, y)
        assertEquals(-90.0, result, "calculateTheta should return -90 degrees when the point is on the negative Y-axis")
    }

    @Test
    fun `test calculateTheta with point in first quadrant`() {
        val x = 150.0
        val y = 150.0
        val result = calculateThetaInDegrees(x, y)
        assertEquals(45.0, result, "calculateTheta should return 45 degrees when the point is in the first quadrant")
    }

    @Test
    fun `test calculateTheta with point in third quadrant`() {
        val x = -50.0
        val y = -50.0
        val result = calculateThetaInDegrees(x, y)
        assertEquals(-135.0, result, )
    }
}