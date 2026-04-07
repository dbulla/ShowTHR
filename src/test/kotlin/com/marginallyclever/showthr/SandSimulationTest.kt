package com.marginallyclever.showthr

import com.nurflugel.showthr.RhoTheta
import com.nurflugel.showthr.Settings
import com.nurflugel.showthr.Utilities.Companion.getArmLength
import com.nurflugel.showthr.Utilities.Companion.getBall2RhoTheta
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.cos
import kotlin.math.sin

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class SandSimulationTest {

    private lateinit var settings: Settings

    @BeforeEach
    fun setup() {
        settings = Settings().apply { tableDiameterWithPadding = 300; calculateCenter() } // Assuming Settings has a parameterless constructor
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
        settings = Settings().apply { tableDiameterWithPadding = 1000; calculateCenter() }

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
        assertEquals(1.0, armLength, 0.0001)
    }

    @Test
    @Throws(IOException::class)
    fun testSandSimulationSpiral() {
        settings = Settings().apply { baseTableDiameter = 100; ballRadius = 2; calculateCenter() }
        val sandSimulation = SandSimulation(settings)
        sandSimulation.setTarget(RhoTheta(0.0, 100.0))
        var radius = (settings.tableDiameterWithPadding) / 2.0 - settings.SHOULDER_WIDTH
        var angleInDegrees = 0.0
        for (iteration in 0..9999) {
            sandSimulation.update()
            if (sandSimulation.ballAtTarget(sandSimulation.ball)) {
                val angleInRadians = Math.toRadians(angleInDegrees)
                sandSimulation.setTarget(
                    RhoTheta(
                        settings.tableDiameterWithPadding / 2.0 + sin(angleInRadians) * radius,
                        settings.tableDiameterWithPadding / 2.0 + cos(angleInRadians) * radius
                    )
                )
                radius = ((settings.tableDiameterWithPadding) / 2.0 - settings.SHOULDER_WIDTH) - (angleInDegrees / 360.0) * 10
                angleInDegrees += 5.0
            }
            if (iteration % 100 == 0) {
                println(iteration)
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
    @Order(Integer.MAX_VALUE) // run this last so it saves the image
    @Throws(IOException::class)
    fun testSandSimulationFromFile() {
        settings = Settings().apply { baseTableDiameter = 100; ballRadius = 2; calculateCenter() }
        val sandSimulation = SandSimulation(settings)
        ShowTHR.processThrFile("src/test/resources/Vaporeon with Waves.thr", sandSimulation)

        val image: BufferedImage = sandSimulation.renderSandImage()
        // save the image to disk
        val imageFile = File("sand_simulation.png")
        ImageIO.write(image, "png", imageFile)
        println("Image saved to " + imageFile.absolutePath)
    }
}
