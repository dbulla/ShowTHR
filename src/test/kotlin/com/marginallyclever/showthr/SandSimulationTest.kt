package com.marginallyclever.showthr

import com.marginallyclever.showthr.Settings.Companion.backgroundImageName
import com.marginallyclever.showthr.Settings.Companion.height
import com.marginallyclever.showthr.Settings.Companion.width
import org.junit.jupiter.api.Test
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.cos
import kotlin.math.sin

class SandSimulationTest {
    @Test
    @Throws(IOException::class)
    fun testSandSimulation() {
        val sandSimulation = SandSimulation()
        val image = sandSimulation.renderSandImage()
        // save the image to disk
        val file = File("sand_simulation.png")
        ImageIO.write(image, "png", file)
        println("Image saved to " + file.absolutePath)
    }

    @Test
    @Throws(IOException::class)
    fun testSandSimulationSpiral() {
        val settings=Settings()
        width=300
        height=300
        val sandSimulation = SandSimulation()
        sandSimulation.setTarget(100.0, 100.0)
        var d = (width - 40) / 2.0
        var a = 0.0
        for (i in 0..9999) {
            sandSimulation.update(0.5)
            if (sandSimulation.ballAtTarget()) {
                val r = Math.toRadians(a)
                sandSimulation.setTarget(
                    width / 2.0 + cos(r) * d,
                    height / 2.0 + sin(r) * d
                )
                d = ((width - 40) / 2.0) - (a / 360.0) * 10
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
        // table size
        val w = 1000
        val h = 1000
        val sandSimulation = SandSimulation()
      val showThr= ShowTHR
        showThr.processThrFile("src/test/resources/Vaporeon with Waves.thr", sandSimulation)

        val image = sandSimulation.renderSandImage()
        // save the image to disk
        val file = File("sand_simulation.png")
        ImageIO.write(image, "png", file)
        val aaa = null
        println("Image saved to " + file.absolutePath)
    }
}
