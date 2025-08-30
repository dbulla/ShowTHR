package com.marginallyclever.showthr

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class ShowTHRTest {

    @Test
    fun `test expandSequence with empty input`() {
        val input = emptyList<Pair<Double, Double>>()
        val result = ShowTHR.expandSequence(input)
        assertTrue(result.isEmpty(), "Result should be empty for empty input")
    }

    @Test
    fun `test expandSequence with single point input`() {
        val input = listOf(Pair(1.0, 2.0))
        val result = ShowTHR.expandSequence(input)
        assertEquals(1, result.size, "Result should contain only one point")
        assertEquals(Pair(1.0, 2.0), result[0], "Point should match input")
    }

    @Test
    fun `test expandSequence with consecutive points requiring no expansion`() {
        val input = listOf(Pair(1.0, 2.0), Pair(1.00, 2.02))
        val result = ShowTHR.expandSequence(input)
        assertEquals(input.size, result.size, "Result should contain the same number of points as input")
        assertEquals(input, result, "Points should not change if no expansion is required")
    }

    @Test
    fun `test expandSequence with points requiring expansion`() {
        val input = listOf(Pair(0.0, 1.0), Pair(2.0, 1.0))
        val result = ShowTHR.expandSequence(input)
        assertTrue(result.size > input.size, "Result should contain more points after expansion")
        assertEquals(Pair(0.0, 1.0), result.first(), "First point should remain unchanged")
        assertEquals(Pair(2.0, 1.0), result.last(), "Last point should remain unchanged")
        result.forEachIndexed { index, point ->
            if (index > 0) {
                val prevPoint = result[index - 1]
                val deltaTheta = point.first - prevPoint.first
                val deltaRho = point.second - prevPoint.second
                assertTrue(deltaTheta <= 0.1 && deltaRho <= 0.01, "Points should be properly interpolated")
            }
        }
    }

    @Test
    fun `test expandSequence with zero rho values`() {
        val input = listOf(Pair(0.0, 0.0), Pair(3.14, 0.0))
        val result = ShowTHR.expandSequence(input)
        assertTrue(result.size > input.size, "Result should contain more points after expansion")
        assertEquals(Pair(0.0, 0.0), result.first(), "First point should remain unchanged")
        assertEquals(Pair(3.14, 0.0), result.last(), "Last point should remain unchanged")
    }
}