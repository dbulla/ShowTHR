package com.marginallyclever.showthr

import com.marginallyclever.showthr.Utilities.Companion.calculateRho
import com.marginallyclever.showthr.Utilities.Companion.calculateTheta
import com.marginallyclever.showthr.Utilities.Companion.calculateX
import com.marginallyclever.showthr.Utilities.Companion.calculateY
import com.marginallyclever.showthr.Utilities.Companion.setValueFromArg
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
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
        // Arrange
        val theta = 0.0
        val rho = 0.0
        val settings = Settings().apply { centerY = 500; maxRadius = 200 }

        // Act
        val result = calculateY(theta, rho, settings)

        // Assert
        assertEquals(500.0, result, "calculateY should return the same centerY when rho is zero")
    }

    @Test
    fun `test calculateY with positive theta and rho`() {
        // Arrange
        val theta = Math.PI / 4
        val rho = 0.5
        val settings = Settings().apply { centerY = 300; maxRadius = 100 }

        // Act
        val result = calculateY(theta, rho, settings)

        // Assert
        val expected = 300 - cos(theta) * rho * settings.maxRadius
        assertEquals(expected, result, 0.0001, "calculateY should compute the correct y value for positive theta and rho")
    }

    @Test
    fun `test calculateY with negative theta and positive rho`() {
        // Arrange
        val theta = -Math.PI / 6
        val rho = 0.7
        val settings = Settings().apply { centerY = 400; maxRadius = 150 }

        // Act
        val result = calculateY(theta, rho, settings)

        // Assert
        val expected = 400 - cos(theta) * rho * settings.maxRadius
        assertEquals(expected, result, 0.0001, "calculateY should compute the correct y value for negative theta and positive rho")
    }

    @Test
    fun `test calculateY with theta as PI`() {
        // Arrange
        val theta = Math.PI
        val rho = 1.0
        val settings = Settings().apply { centerY = 600; maxRadius = 250 }

        // Act
        val result = calculateY(theta, rho, settings)

        // Assert
        val expected = 600 - cos(theta) * rho * settings.maxRadius
        assertEquals(expected, result, 0.0001, "calculateY should compute the correct y value when theta is PI")
    }

    @Test
    fun `test calculateY with maximum rho`() {
        // Arrange
        val theta = 0.0
        val rho = 1.0
        val settings = Settings().apply { centerY = 700; maxRadius = 300 }

        // Act
        val result = calculateY(theta, rho, settings)

        // Assert
        val expected = 700 - cos(theta) * rho * settings.maxRadius
        assertEquals(expected, result, 0.0001, "calculateY should compute the correct y value for maximum rho")
    }

    @Test
    fun `test calculateRho with point at the center of the table`() {
        val settings = Settings().apply { centerX = 100; centerY = 100; tableDiameter = 200 }
        val x = 100
        val y = 100
        val result = calculateRho(x, y, settings)
        assertEquals(0.0, result, "calculateRho should return 0 when the point is at the center of the table")
    }

    @Test
    fun `test calculateRho with point at the edge of the table radius`() {
        val settings = Settings().apply { centerX = 100; centerY = 100; tableDiameter = 200 }
        val x = 300 // centerX + tableRadius
        val y = 100
        val result = calculateRho(x, y, settings)
        assertEquals(1.0, result, "calculateRho should return 1 when the point is at the edge of the table radius")
    }

    @Test
    fun `test calculateRho with point outside the table radius`() {
        val settings = Settings().apply { centerX = 100; centerY = 100; tableDiameter = 200 }
        val x = 400
        val y = 100
        val result = calculateRho(x, y, settings)
        assertTrue(result > 1.0, "calculateRho should return a value greater than 1 when the point is outside the table radius")
    }

    @Test
    fun `test calculateRho with negative x and y coordinates`() {
        val settings = Settings().apply { centerX = 100; centerY = 100; tableDiameter = 200 }
        val x = 0
        val y = 0
        val result = calculateRho(x, y, settings)
        val expected = sqrt((settings.centerX * settings.centerX + settings.centerY * settings.centerY).toDouble()) / settings.tableDiameter
        assertEquals(expected, result, "calculateRho should correctly calculate rho for negative coordinates")
    }

    @Test
    fun `test calculateX with zero theta and rho`() {
        val settings = Settings().apply { centerX = 100; maxRadius = 50 }
        val theta = 0.0
        val rho = 0.0
        val result = calculateX(theta, rho, settings)
        assertEquals(100.0, result, "calculateX should return centerX when theta and rho are 0")
    }

    @Test
    fun `test calculateX with positive theta and rho`() {
        val settings = Settings().apply { centerX = 100; maxRadius = 50 }
        val theta = PI / 2
        val rho = 1.0
        val result = calculateX(theta, rho, settings)
        assertEquals(150.0, result, "calculateX should calculate correctly for positive theta and rho")
    }

    @Test
    fun `test calculateX with negative theta`() {
        val settings = Settings().apply { centerX = 100; maxRadius = 50 }
        val theta = -PI / 4
        val rho = 1.0
        val result = calculateX(theta, rho, settings)
        val expected = 100 + (sin(theta) * rho * settings.maxRadius)
        assertEquals(expected, result, "calculateX should calculate correctly for negative theta")
    }

    @Test
    fun `test calculateX with zero rho`() {
        val settings = Settings().apply { centerX = 200; maxRadius = 100 }
        val theta = PI / 3
        val rho = 0.0
        val result = calculateX(theta, rho, settings)
        assertEquals(200.0, result, "calculateX should return centerX when rho is 0, regardless of theta")
    }

    @Test
    fun `test calculateX with maximum rho`() {
        val settings = Settings().apply { centerX = 300; maxRadius = 200 }
        val theta = PI / 3
        val rho = 1.0
        val result = calculateX(theta, rho, settings)
        val expected = 300 + (sin(theta) * rho * settings.maxRadius)
        assertEquals(expected, result, "calculateX should calculate correctly for maximum rho")
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
        val settings = Settings().apply { centerX = 100; centerY = 100 }
        val x = 200
        val y = 100
        val result = calculateTheta(x, y, settings)
        assertEquals(0.0, result, "calculateTheta should return 0 degrees when the point is on the positive X-axis")
    }

    @Test
    fun `test calculateTheta with point on positive Y-axis`() {
        val settings = Settings().apply { centerX = 100; centerY = 100 }
        val x = 100
        val y = 200
        val result = calculateTheta(x, y, settings)
        assertEquals(90.0, result, "calculateTheta should return 90 degrees when the point is on the positive Y-axis")
    }

    @Test
    fun `test calculateTheta with point on negative X-axis`() {
        val settings = Settings().apply { centerX = 100; centerY = 100 }
        val x = 0
        val y = 100
        val result = calculateTheta(x, y, settings)
        assertEquals(180.0, result, "calculateTheta should return 180 degrees when the point is on the negative X-axis")
    }

    @Test
    fun `test calculateTheta with point on negative Y-axis`() {
        val settings = Settings().apply { centerX = 100; centerY = 100 }
        val x = 100
        val y = 0
        val result = calculateTheta(x, y, settings)
        assertEquals(-90.0, result, "calculateTheta should return -90 degrees when the point is on the negative Y-axis")
    }

    @Test
    fun `test calculateTheta with point in first quadrant`() {
        val settings = Settings().apply { centerX = 100; centerY = 100 }
        val x = 150
        val y = 150
        val result = calculateTheta(x, y, settings)
        assertEquals(45.0, result, "calculateTheta should return 45 degrees when the point is in the first quadrant")
    }

    @Test
    fun `test calculateTheta with point in third quadrant`() {
        val settings = Settings().apply { centerX = 100; centerY = 100 }
        val x = 50
        val y = 50
        val result = calculateTheta(x, y, settings)
        assertEquals(-135.0, result, "calculateTheta should return -135 degrees when the point is in the third quadrant")
    }
}