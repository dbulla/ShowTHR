package com.marginallyclever.showthr

import com.marginallyclever.showthr.Utilities.Companion.getBall2Rho
import com.marginallyclever.showthr.Utilities.Companion.setValueFromArg
import java.lang.Math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UtilitiesTest {

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

    /**
     * Tests for the `getBall2Rho` function in the `Companion` object of `Utilities`.
     *
     * Description:
     * The `getBall2Rho` function computes the complementary value of the given rho value,
     * as `1.0 - rho`. The result represents the flipped radial coordinate in the context
     * of a two-ball system on a coordinate plane.
     */

    @Test
    fun `test getBall2Rho with rho as zero`() {
        // Test case when rho is 0
        val actual = getBall2Rho(0.0)
        assertEquals(1.0, actual, "getBall2Rho(0.0) should return 1.0")
    }

    @Test
    fun `test getBall2Rho with rho as one`() {
        val actual = getBall2Rho(1.0)
        assertEquals(0.0, actual, "getBall2Rho(1.0) should return 0.0")
    }

    @Test
    fun `test getBall2Rho with rho as half`() {
        val actual = getBall2Rho(0.5)
        assertEquals(0.5, actual, "getBall2Rho(0.5) should return 0.5")
    }

    @Test
    fun `test getBall2Rho with rho as small positive value`() {
        // Test case when rho is a small positive value
        val actual = getBall2Rho(0.1)
        assertEquals(0.9, actual, "getBall2Rho(0.1) should return 0.9")
    }

    @Test
    fun `test getBall2Rho with rho as large positive value less than one`() {
        // Test case when rho is a large positive value less than one
        val actual = getBall2Rho(0.9)
        assertEquals(0.1, actual, .001, "getBall2Rho(0.9) should return 0.1")
    }

    @Test
    fun `test getBall2Theta with theta as zero`() {
        val actual = Utilities.getBall2Theta(0.0)
        assertEquals(PI, actual, "getBall2Theta(0.0) should return PI")
    }

    @Test
    fun `test getBall2Theta with theta as PI divided by 2`() {
        // Test case when theta is PI/2
        val actual = Utilities.getBall2Theta(PI / 2)
        assertEquals(PI * 3 / 2, actual, "getBall2Theta(PI/2) should return 3*PI/2")
    }

    @Test
    fun `test getBall2Theta with theta as PI`() {
        val actual = Utilities.getBall2Theta(PI)
        assertEquals(2 * PI, actual, "getBall2Theta(PI) should return 2*PI")
    }

    @Test
    fun `test getBall2Theta with theta as three times PI divided by 2`() {
        val actual = Utilities.getBall2Theta(3 * PI / 2)
        assertEquals(2 * PI + PI / 2, actual, "getBall2Theta(3*PI/2) should return 5*PI/2")
    }

    @Test
    fun `test getBall2Theta with theta as negative angle`() {
        // Test case when theta is a negative value
        val actual = Utilities.getBall2Theta(-PI / 2)
        assertEquals(PI / 2, actual, "getBall2Theta(-PI/2) should return PI/2")
    }
}