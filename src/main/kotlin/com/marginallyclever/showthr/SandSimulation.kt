package com.marginallyclever.showthr

import com.marginallyclever.showthr.Settings.Companion.MAX_SLOPE
import com.marginallyclever.showthr.Settings.Companion.REDISTRIBUTION_RATE
import com.marginallyclever.showthr.Settings.Companion.RELAX_MARGIN
import com.marginallyclever.showthr.Settings.Companion.backgroundImageName
import com.marginallyclever.showthr.Settings.Companion.ballRadius
import com.marginallyclever.showthr.Settings.Companion.blueConversion
import com.marginallyclever.showthr.Settings.Companion.greenConversion
import com.marginallyclever.showthr.Settings.Companion.height
import com.marginallyclever.showthr.Settings.Companion.initialSandDepth
import com.marginallyclever.showthr.Settings.Companion.isHeadless
import com.marginallyclever.showthr.Settings.Companion.redConversion
import com.marginallyclever.showthr.Settings.Companion.useGreyBackground
import com.marginallyclever.showthr.Settings.Companion.width
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.vecmath.Vector2d
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sqrt


/**
 * A simulation of loose sand on a table and being displaced by a ball.
 */
//class SandSimulation(val tableWidth: Int, val tableHeight: Int, ballRadius: Double, initialSandDepth: Double, backgroundImageName: String) {
class SandSimulation() {

    private val sandGrid = Array(width) { DoubleArray(height) } // 2D array for sand density
    private val ball = Ball(ballRadius)
    private lateinit var startPosition: Vector2d
    private var imageFrame: ImageFrame? = null

    var bufferedImage: BufferedImage

    init {

        ball.position = (Vector2d(width / 2.0, height / 2.0))

        // Initialize sand grid to uniform density
        initializeSandGrid(initialSandDepth)
        val backgroundImageFile = File(backgroundImageName)
        val isBackgroundImagePresent = backgroundImageFile.exists()
        bufferedImage = when {
            isBackgroundImagePresent -> readInCleanedImage(backgroundImageFile)
            else                     -> BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        }
        if (!isHeadless) imageFrame = ImageFrame(bufferedImage)
    }

    private fun initializeSandGrid(initialSandDepth: Double) {
        (0..<width).forEach { i ->
            (0..<height).forEach { j ->
                sandGrid[i][j] = initialSandDepth // some sand in every square
            }
        }
    }

    /*
        int red = (rgb>>16)&0x0ff;
        int green=(rgb>>8) &0x0ff;
        int blue= (rgb)    &0x0ff;
     */
    private fun readInCleanedImage(cleanFile: File): BufferedImage {
        val backgroundImage = ImageIO.read(cleanFile)
        // set the sand height to the image
        (0..<width).forEach { i ->
            (0..<height).forEach { j ->
                val rgb = backgroundImage.getRGB(i, j)
                val red: Int = (rgb and 0xff0000) shr 16
                val newLevel = red.toDouble() / 30 // 30 seems to work...
                sandGrid[i][j] = newLevel
            }
        }
        return backgroundImage
    }

    fun setTarget(x: Double, y: Double) {
        ball.setTarget(x, y)
        startPosition = ball.position
    }

    fun setBallPosition(x: Double, y: Double) {
        ball.position.x = x
        ball.position.y = y
    }

    fun ballAtTarget(): Boolean {
        return ball.atTarget
    }

    fun update(deltaTime: Double) {
        ball.updatePosition(deltaTime)
        makeBallPushSand()
        relaxSand() // redistribute the sand
    }

    private fun makeBallPushSand() {
        // Iterate over the area affected by the ball's radius
        val ballX = ball.position.x.toInt()
        val ballY = ball.position.y.toInt()
        val radius = ball.radius.toInt()

        for (i in ballX - radius..ballX + radius) {
            for (j in ballY - radius..ballY + radius) {
                if (i in 0..<width && j >= 0 && j < height) {
                    val dx = i - ballX
                    val dy = j - ballY
                    if (insideTable(i + dx, j + dy)) {
                        // Distance from the ball's center
                        val distance = sqrt((dx * dx + dy * dy).toDouble())
                        if (distance <= radius) {
                            // Displace sand based on distance
                            // if the amount of sand here (A) is greater than the height of the ball at this point (B)
                            // displace A-B away from the center of the ball.
                            var sandHere = sandGrid[i][j]
                            val heightOfBall = max(0.0, 1 - cos(max(0.0, 1 - distance / radius)))
                            if (sandHere >= heightOfBall) {
                                var toMove = sandHere - heightOfBall
                                sandHere -= toMove
                                if (sandHere < 0) {
                                    toMove -= sandHere
                                    sandHere = 0.0
                                }
                                sandGrid[i + dx][j + dy] += toMove
                                sandGrid[i][j] = sandHere
                            }
                        }
                    }
                }
            }
        }
    }

    private fun insideTable(x: Int, y: Int): Boolean {
        //        return (x >= 0 && x < width) && (y >= 0 && y < height)
        return (x in 0..<width) && (y in 0..<height)
    }

    /**
     * Make the sand naturally collapse into a more stable shape.
     */
    @Suppress("DuplicatedCode")
    private fun relaxSand() {
        val ballRelaxedMargin = (ball.radius * RELAX_MARGIN).toInt()
        var startX = startPosition.x.toInt()
        var startY = startPosition.y.toInt()
        var endX = ball.position.x.toInt()
        var endY = ball.position.y.toInt()

        if (startX > endX) {
            val temp = startX
            startX = endX
            endX = temp
        }

        startX -= ballRelaxedMargin
        endX += ballRelaxedMargin

        if (startY > endY) {
            val temp = startY
            startY = endY
            endY = temp
        }
        startY -= ballRelaxedMargin
        endY += ballRelaxedMargin

        if (startX < 0) startX = 0
        if (startY < 0) startY = 0
        if (endX >= width) endX = width - 1
        if (endY >= height) endY = height - 1

        var settled: Boolean
        do {
            settled = true
            val lowerNeighbors = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)

            for (y in startY..<endY - 1) {
                for (x in startX..<endX - 1) {
                    val here = sandGrid[x][y]
                    val hereMinusSlope = here - MAX_SLOPE
                    var c = 0

                    // Check up, down, left, right neighbors
                    if (insideTable(x - 1, y) && sandGrid[x - 1][y] < hereMinusSlope) {
                        lowerNeighbors[c++] = x - 1
                        lowerNeighbors[c++] = y
                    }
                    if (insideTable(x + 1, y) && sandGrid[x + 1][y] < hereMinusSlope) {
                        lowerNeighbors[c++] = x + 1
                        lowerNeighbors[c++] = y
                    }
                    if (insideTable(x, y - 1) && sandGrid[x][y - 1] < hereMinusSlope) {
                        lowerNeighbors[c++] = x
                        lowerNeighbors[c++] = y - 1
                    }
                    if (insideTable(x, y + 1) && sandGrid[x][y + 1] < hereMinusSlope) {
                        lowerNeighbors[c++] = x
                        lowerNeighbors[c++] = y + 1
                    }

                    if (c != 0) {
                        settled = false
                        val d = REDISTRIBUTION_RATE * 2.0 / c

                        var i = 0
                        while (i < c) {
                            val x2 = lowerNeighbors[i]
                            val y2 = lowerNeighbors[i + 1]
                            val heightDiff = sandGrid[x][y] - sandGrid[x2][y2]
                            val transferAmount = heightDiff * d
                            sandGrid[x2][y2] += transferAmount
                            sandGrid[x][y] -= transferAmount
                            i += 2
                        }
                    }
                }
            }
        } while (!settled)
    }

    /**
     * Render the sand density as a grayscale image.  The darkest pixels have the least sand.
     *
     * @return the image
     */
    fun renderSandImage(): BufferedImage {
        var max = 8.5 // setting max dynamically makes the animation flicker - setting it to a constant 8.5 seems acceptable.
        //        for (i in 0..<tableWidth) {
        //            for (j in 0..<tableHeight) {
        //                max = max(max, sandGrid[i][j])
        //            }
        //        }
        //        println("max = ${max}")

        for (i in 0..<width) {
            for (j in 0..<height) {
                var gray = (sandGrid[i][j] * 255.0 / max).toInt() // Convert density to grayscale
                if (gray > 255) gray = 255
                bufferedImage.setRGB(i, j, encode32bit(gray))
            }
        }
        if(!isHeadless) imageFrame?.updateImage(bufferedImage)
        return bufferedImage
    }

    /**
     * Encodes an 8-bit greyscale value into a 32-bit ARGB color value.
     * The alpha channel is set to full opacity (0xFF), and the same greyscale value is applied to the red, green, and blue channels.
     *
     * @param greyscale the 8-bit greyscale value to encode, expected to be in the range [0, 255].
     * @return the corresponding 32-bit ARGB color value.
     */
    private fun encode32bit(greyscale: Int): Int {
        var newGreyscale = greyscale
        newGreyscale = newGreyscale and 0xff
        val red: Int = (newGreyscale * redConversion).toInt()
        val green: Int = (newGreyscale * greenConversion).toInt()
        val blue: Int = (newGreyscale * blueConversion).toInt()
        val resultRgb = when {
            useGreyBackground -> Color(newGreyscale, newGreyscale, newGreyscale).rgb
            else              -> Color(red, green, blue).rgb
        }
        return resultRgb
    }
}

