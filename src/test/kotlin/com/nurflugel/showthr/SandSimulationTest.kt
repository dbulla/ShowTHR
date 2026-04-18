package com.nurflugel.showthr

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
        settings = Settings().apply { tableDiameterMinusPadding = 300; calculateCenter() } // Assuming Settings has a parameterless constructor
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
    @Throws(IOException::class)
    fun testSandSimulationSpiral() {
        settings = Settings().apply { baseTableDiameter = 100; ballRadius = 2; calculateCenter() }
        val sandSimulation = SandSimulation(settings)
        sandSimulation.setTarget(RhoTheta(0.0, 100.0),RhoTheta(0.0, 100.0) )
        var radius = (settings.tableDiameterMinusPadding) / 2.0 - settings.SHOULDER_WIDTH
        var angleInDegrees = 0.0
        for (iteration in 0..9999) {
            sandSimulation.update()
            if (sandSimulation.ballAtTarget(sandSimulation.ball)) {
                val angleInRadians = Math.toRadians(angleInDegrees)
                sandSimulation.setTarget(
                    RhoTheta(
                        settings.tableDiameterMinusPadding / 2.0 + sin(angleInRadians) * radius,
                        settings.tableDiameterMinusPadding / 2.0 + cos(angleInRadians) * radius
                    ),
                    RhoTheta(0.0, 100.0)
                )
                radius = ((settings.tableDiameterMinusPadding) / 2.0 - settings.SHOULDER_WIDTH) - (angleInDegrees / 360.0) * 10
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
}
