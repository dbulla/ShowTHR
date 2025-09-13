package com.nurflugel.showthr

import com.nurflugel.showthr.Utilities.Companion.calculateDistanceRhoTheta
import com.nurflugel.showthr.Utilities.Companion.calculateSandX
import com.nurflugel.showthr.Utilities.Companion.calculateSandY
import kotlin.math.PI
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CompanionTest {

    val settings: Settings = Settings()

    /** Creates settings for a table with diameter of 200, radius of 100, max radius of 80 */
    @BeforeTest
    public fun configureSettings() {
        settings.apply {
            tableDiameter = 200
            calculateCenter()
        }
    }
    //   public fun configureSettings(): Settings = Settings().apply {
    //        tableDiameter = 200
    //        calculateCenter()
    //    }
//
//    @Test
//    fun `validate Polar Coord Translation`() {
//        assertEquals(0.0, calculateThetaFromPolarXY(0.0, 0.0, 0.0))
//        assertEquals(0.0, calculateThetaFromPolarXY(100.0, 0.0, 100.0))
//        assertEquals(PI / 2, calculateThetaFromPolarXY(0.0, 100.0, 100.0))
//        assertEquals(PI, calculateThetaFromPolarXY(-100.0, 0.0, 100.0))
//        assertEquals(PI * 3 / 2, calculateThetaFromPolarXY(0.0, -100.0, 100.0))
//    }


    @Test
    fun `calculate position horizontal`() {
        assertEquals(
            4.0, calculateDistanceRhoTheta(
                NormalizedThetaRho(0.0, 0.0),
                NormalizedThetaRho(0.0, 4.0)
            )
        )
    }

    @Test
    fun `calculate position vertical`() {
        assertEquals(
            4.0, calculateDistanceRhoTheta(
                NormalizedThetaRho(PI / 2, 0.0),
                NormalizedThetaRho(PI / 2, 4.0)
            )
        )
    }

    @Test
    fun `calculate position diagonal`() {
        assertEquals(
            4.0, calculateDistanceRhoTheta(
                NormalizedThetaRho(PI / 4, 0.0),
                NormalizedThetaRho(PI / 4, 4.0)
            )
        )
    }

    @Test
    fun `calculate xy at 0 degrees`() {
        val thetaRho = NormalizedThetaRho(0.0, 1.0)
        assertEquals(100.0, calculateSandX(thetaRho, settings), .001)
        assertEquals(180.0, calculateSandY(thetaRho, settings), .001)
    }

    @Test
    fun `calculate xy at 90 degrees`() {
        val thetaRho = NormalizedThetaRho(PI / 2, 1.0)
        assertEquals(180.0, calculateSandX(thetaRho, settings), .001)
        assertEquals(100.0, calculateSandY(thetaRho, settings), .001)
    }

    @Test
    fun `calculate xy at 180 degrees`() {
        val thetaRho = NormalizedThetaRho(PI, 1.0)
        assertEquals(100.0, calculateSandX(thetaRho, settings), .001)
        assertEquals(20.0, calculateSandY(thetaRho, settings), .001)
    }

    @Test
    fun `calculate xy at 270 degrees`() {
        val thetaRho = NormalizedThetaRho(PI * 3 / 2, 1.0)
        assertEquals(20.0, calculateSandX(thetaRho, settings), .001)
        assertEquals(100.0, calculateSandY(thetaRho, settings), .001)
    }

    @Test
    fun `calculate xy at 360 degrees`() {
        val thetaRho = NormalizedThetaRho(2 * PI, 1.0)
        assertEquals(100.0, calculateSandX(thetaRho, settings), .001)
        assertEquals(180.0, calculateSandY(thetaRho, settings), .001)
    }
}