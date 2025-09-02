package com.marginallyclever.showthr

import com.nurflugel.showthr.Settings.Companion.MAX_SLOPE
import com.nurflugel.showthr.Settings.Companion.REDISTRIBUTION_RATE
import com.nurflugel.showthr.Settings.Companion.BLUE_CONVERSION
import com.nurflugel.showthr.Settings.Companion.GREEN_CONVERSION
import com.nurflugel.showthr.Settings.Companion.RED_CONVERSION
import com.nurflugel.showthr.Utilities.Companion.calculateX
import com.nurflugel.showthr.Utilities.Companion.calculateY
import com.nurflugel.showthr.Utilities.Companion.getBall2Theta
import com.nurflugel.showthr.Utilities.Companion.getBall2ThetaRho
import com.nurflugel.showthr.ImageFrame
import com.nurflugel.showthr.Settings
import com.nurflugel.showthr.ThetaRho
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sqrt

/**
 * A simulation of loose sand on a table and being displaced by a ball.
 *
 * Note that although the ball is tracked with polar coordinates (rho, theta), the table and resulting image are rendered with Cartesian coordinates.  By
 * converting late like this, the 2nd ball tracking is trivial, else it'd have to be converted to x,y, and back again.
 *
 */
class SandSimulation(val settings: Settings) {

    private val sandGrid = Array(settings.tableDiameter) { DoubleArray(settings.tableDiameter) } // 2D array for sand density
    private val ball = Ball("Ball_1", settings.ballRadius, settings)
    private val ball2 = Ball("Ball_2", settings.ballRadius - 1, settings) // optional second ball, slightly smaller than the first
    private lateinit var startPosition: ThetaRho
    private lateinit var startPosition2: ThetaRho
    private var imageFrame: ImageFrame? = null
    private var bufferedImage: BufferedImage

    init {
        ball.setPosition(ThetaRho(0.0, 0.0))
        if (settings.useTwoBalls) {
            ball2.setPosition(ThetaRho(getBall2Theta(ball.position.theta), 1.0))
        }

        // Initialize sand grid to uniform density
        initializeSandGrid(settings.initialSandDepth)
        val backgroundImageFile = File(settings.backgroundImageName)
        val isBackgroundImagePresent = backgroundImageFile.exists()
        bufferedImage = when {
            isBackgroundImagePresent -> readInCleanedImage(backgroundImageFile)
            else                     -> BufferedImage(settings.tableDiameter, settings.tableDiameter, TYPE_INT_ARGB)
        }
        if (!settings.isHeadless) imageFrame = ImageFrame(bufferedImage, settings)
    }

    private fun initializeSandGrid(initialSandDepth: Double) {
        (0..<settings.tableDiameter).forEach { i ->
            (0..<settings.tableDiameter).forEach { j ->
                sandGrid[i][j] = initialSandDepth // some sand in every square
            }
        }
    }

    /**
     *
     *  Read in a pre-generated image of a "clean" cycle as a starting point for the sand.

     *  Just for reference...
     *  int red = (rgb>>16)&0x0ff;
     *  int green=(rgb>>8) &0x0ff;
     *  int blue= (rgb)    &0x0ff;
     */
    private fun readInCleanedImage(cleanFile: File): BufferedImage {
        val backgroundImage = ImageIO.read(cleanFile)
        // set the sand height to the image
        (0..<settings.tableDiameter).forEach { i ->
            (0..<settings.tableDiameter).forEach { j ->
                val color = Color(backgroundImage.getRGB(i, j))
                val newLevel = (color.red + color.green + color.blue).toDouble() / 3 / 30 // 30 seems to work...
                sandGrid[i][j] = newLevel
            }
        }
        return backgroundImage
    }

    fun setTarget(thetaRho: ThetaRho) {
        ball.setTarget(thetaRho)
        startPosition = ball.position
        if (settings.useTwoBalls) {
            ball2.setTarget(getBall2ThetaRho(thetaRho))
            startPosition2 = ball2.position // we need this for the relaxation step
        }
    }

    fun setInitialBallPosition(thetaRho: ThetaRho) {
        ball.setPosition(thetaRho)
        ball.setTarget(thetaRho)
        if (settings.useTwoBalls) {
            ball2.setPosition(getBall2ThetaRho(thetaRho))
            ball2.setTarget(getBall2ThetaRho(thetaRho))
        }
    }

    fun update() {
        ball.updatePosition(settings.deltaTime)
        if (settings.useTwoBalls) {
            ball2.updatePosition(settings.deltaTime)
            //            println("Ball1: rho=${ball.getRho()}, Ball2: rho=${ball2.getRho()}")
        }
        makeBallPushSand() // push the sand up
        relaxSand() // let the sand settle
    }

    private fun makeBallPushSand() {
        makeBallPushSand(ball)
        if (settings.useTwoBalls) makeBallPushSand(ball2)
    }

    /**
     * This method simulates the ball pushing the sand out of its personal space.
     */
    private fun makeBallPushSand(ball: Ball) {
        // Iterate over the area affected by the ball's radius
        val ballX = calculateX(ball.position, settings).toInt()
        val ballY = calculateY(ball.position, settings).toInt()
        val radius = ball.radius
        for (i in ballX - radius..ballX + radius) {
            for (j in ballY - radius..ballY + radius) {
                if (i in 0..<settings.tableDiameter && j >= 0 && j < settings.tableDiameter) {
                    val dx = i - ballX
                    val dy = j - ballY
                    if (isInsideTable(i + dx, j + dy)) {
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

    private fun isInsideTable(x: Int, y: Int): Boolean {
        val isXInside = x in 0..<settings.tableDiameter
        val isYInside = y in 0..<settings.tableDiameter
        return isXInside && isYInside
    }

    /**
     * Make the sand naturally collapse into a more stable shape.
     */
    @Suppress("DuplicatedCode")
    private fun relaxSand() {
        relaxSand(startPosition, ball)
        if (settings.useTwoBalls) relaxSand(startPosition2, ball2)
    }

    private fun relaxSand(startPosition: ThetaRho, ball: Ball) {
        var startX = calculateX(startPosition, settings).toInt()
        var startY = calculateY(startPosition, settings).toInt()
        var endX = calculateY(ball.position, settings).toInt()
        var endY = calculateY(ball.position, settings).toInt()

        if (startX > endX) {
            val temp = startX
            startX = endX
            endX = temp
        }

        startX -= ball.ballRelaxedMargin
        endX += ball.ballRelaxedMargin

        if (startY > endY) {
            val temp = startY
            startY = endY
            endY = temp
        }
        startY -= ball.ballRelaxedMargin
        endY += ball.ballRelaxedMargin

        if (startX < 0) startX = 0
        if (startY < 0) startY = 0
        if (endX >= settings.tableDiameter) endX = settings.tableDiameter - 1
        if (endY >= settings.tableDiameter) endY = settings.tableDiameter - 1

        var settled: Boolean
        do {
            settled = true
            val lowerNeighbors = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)

            for (y in startY..<endY - 1) {
                for (x in startX..<endX - 1) {
                    val sandHeightHere = sandGrid[x][y]
                    val sandHeightHereMinusSlope = sandHeightHere - MAX_SLOPE
                    var neighborIndex = 0

                    // Check up, down, left, right neighbors
                    if (isInsideTable(x - 1, y) && sandGrid[x - 1][y] < sandHeightHereMinusSlope) {
                        lowerNeighbors[neighborIndex++] = x - 1
                        lowerNeighbors[neighborIndex++] = y
                    }
                    if (isInsideTable(x + 1, y) && sandGrid[x + 1][y] < sandHeightHereMinusSlope) {
                        lowerNeighbors[neighborIndex++] = x + 1
                        lowerNeighbors[neighborIndex++] = y
                    }
                    if (isInsideTable(x, y - 1) && sandGrid[x][y - 1] < sandHeightHereMinusSlope) {
                        lowerNeighbors[neighborIndex++] = x
                        lowerNeighbors[neighborIndex++] = y - 1
                    }
                    if (isInsideTable(x, y + 1) && sandGrid[x][y + 1] < sandHeightHereMinusSlope) {
                        lowerNeighbors[neighborIndex++] = x
                        lowerNeighbors[neighborIndex++] = y + 1
                    }

                    if (neighborIndex != 0) {
                        settled = false
                        val d = REDISTRIBUTION_RATE * 2.0 / neighborIndex

                        var i = 0
                        while (i < neighborIndex) {
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
        if (settings.isHeadless) {
            for (i in 0..<settings.tableDiameter) {
                for (j in 0..<settings.tableDiameter) {
                    max = max(max, sandGrid[i][j])
                }
            }
            //            println("max = ${max}")
        }
        for (i in 0..<settings.tableDiameter) {
            for (j in 0..<settings.tableDiameter) {
                val gray = minOf(255, (sandGrid[i][j] * 30).toInt()) // Simplified calculation
                bufferedImage.setRGB(i, j, encode32bit(gray))
            }
        }
        if (!settings.isHeadless) imageFrame?.updateImage(bufferedImage)
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
        val red: Int = (greyscale * RED_CONVERSION).toInt()
        val green: Int = (greyscale * GREEN_CONVERSION).toInt()
        val blue: Int = (greyscale * BLUE_CONVERSION).toInt()
        val resultRgb = when {
            settings.useGreyBackground -> Color(greyscale, greyscale, greyscale).rgb
            else                       -> Color(red, green, blue).rgb
        }
        return resultRgb
    }


    fun ballAtTarget(): Boolean {
        return ball.atTarget
    }

    fun writeImage() {
        val file = File(settings.outputFilename!!)
        writeImage(file, bufferedImage)
        println("Image saved to " + file.absolutePath)
    }

    fun writeImage(file: File, image: BufferedImage) {
        try { // save the image to disk
            ImageIO.write(image, settings.ext, file)

        } catch (e: IOException) {
            println("Error saving file " + settings.outputFilename + ": " + e.message)
        }

    }
}

