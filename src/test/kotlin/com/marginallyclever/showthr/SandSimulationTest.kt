package com.marginallyclever.showthr

import com.nurflugel.showthr.RhoTheta
import com.nurflugel.showthr.Settings
import com.nurflugel.showthr.Utilities.Companion.getArmLength
import com.nurflugel.showthr.Utilities.Companion.getBall2RhoTheta
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.cos
import kotlin.math.sin

class SandSimulationTest {

    private lateinit var settings: Settings

    @BeforeEach
    fun setup() {
        settings = Settings().apply { tableDiameter = 300 } // Assuming Settings has a parameterless constructor
        settings.calculateCenter()
    }

    @Test
    @Throws(IOException::class)
    fun testSandSimulation() {
        val sandSimulation = SandSimulation(settings)
        val image = sandSimulation.renderSandImage()
        // save the image to disk
        val file = File("sand_simulation.png")
        ImageIO.write(image, "png", file)
        println("Image saved to " + file.absolutePath)
    }

    @Test
    fun testRho() {
        settings = Settings().apply { tableDiameter = 1000 }
        settings.calculateCenter()
        val ball1 = Ball("Ball1", 10, settings)
        val ball2 = Ball("Ball2", 10, settings)
        validateRho2(ball1, RhoTheta(0.0, 0.0), ball2)
        validateRho2(ball1, RhoTheta(0.0, 1.0), ball2)
        validateRho2(ball1, RhoTheta(0.5, 2.0), ball2)
        validateRho2(ball1, RhoTheta(0.5, 3.5), ball2)
        validateRho2(ball1, RhoTheta(0.5, 5.0), ball2)
        validateRho2(ball1, RhoTheta(0.5, 6.0), ball2)

    }

    private fun validateRho2(ball1: Ball, rhoTheta: RhoTheta, ball2: Ball) {
        ball1.setPositionRhoTheta(rhoTheta)
        val ball2RhoTheta = getBall2RhoTheta(rhoTheta)
        ball2.setPositionRhoTheta(ball2RhoTheta)
        ball2.setTargetRhoTheta(ball2RhoTheta)
        // This should always equal 1.0
        val armLength = getArmLength(ball1, ball2)
        assertEquals(1.0, armLength, 0.01)
    }

    @Test
    @Throws(IOException::class)
    fun testSandSimulationSpiral() {
        val sandSimulation = SandSimulation(settings)
        sandSimulation.setTarget(RhoTheta(0.0, 100.0))
        var d = (settings.tableDiameter - 40) / 2.0
        var a = 0.0
        for (i in 0..9999) {
            sandSimulation.update(0.5)
            if (sandSimulation.ballAtTarget()) {
                val r = Math.toRadians(a)
                sandSimulation.setTarget(
                    RhoTheta(
                        settings.tableDiameter / 2.0 + sin(r) * d,
                        settings.tableDiameter / 2.0 + cos(r) * d
                    )
                )
                d = ((settings.tableDiameter - 40) / 2.0) - (a / 360.0) * 10
                a += 5.0
            }
            if (i % 100 == 0) {
                println(i)
            }
        }

        val image = sandSimulation.renderSandImage()
        // save the image to disk
        val file = File("sand_simulation.png")
        ImageIO.write(image, "png", file)
        println("Image saved to " + file.absolutePath)
    }

    /**
     * Read a THR file and simulate the sand being pushed by the ball.
     * @throws IOException if the file cannot be read
     */
    @Test
    @Throws(IOException::class)
    fun testSandSimulationFromFile() {
        settings.tableDiameter = 100
        settings.tableDiameter = 100
        settings.calculateCenter()
        val sandSimulation = SandSimulation(settings)
        val showThr = ShowTHR
        showThr.processThrFile("src/test/resources/Vaporeon with Waves.thr", sandSimulation)

        val image = sandSimulation.renderSandImage()
        // save the image to disk
        val file = File("sand_simulation.png")
        ImageIO.write(image, "png", file)
        println("Image saved to " + file.absolutePath)
    }
}
