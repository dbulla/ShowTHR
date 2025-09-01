package com.marginallyclever.showthr

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
        settings = Settings().apply { tableDiameter=300 } // Assuming Settings has a parameterless constructor
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
    @Throws(IOException::class)
    fun testSandSimulationSpiral() {
        val sandSimulation = SandSimulation(settings)
        sandSimulation.setTarget(100.0, 100.0)
        var d = (settings.tableDiameter - 40) / 2.0
        var a = 0.0
        for (i in 0..9999) {
            sandSimulation.update()
            if (sandSimulation.ballAtTarget()) {
                val r = Math.toRadians(a)
                sandSimulation.setTarget(
                    settings.tableRadius + cos(r) * d,
                    settings.tableRadius + sin(r) * d
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
