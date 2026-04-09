package com.marginallyclever.showthr

import com.nurflugel.showthr.ImageFrame
import com.nurflugel.showthr.RhoTheta
import com.nurflugel.showthr.Settings
import com.nurflugel.showthr.Utilities.Companion.calculateCornerXY
import com.nurflugel.showthr.Utilities.Companion.calculateDistance
import com.nurflugel.showthr.Utilities.Companion.getBall2RhoTheta
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * A simulation of loose sand on a table and being displaced by a ball.
 */
class SandSimulation(val settings: Settings) {
    // note that we size the table larger than specified - todo - subtract padding from image size
    private val sandGrid = Array(settings.baseTableDiameter) { DoubleArray(settings.baseTableDiameter) } // 2D array for sand density
    val ball = Ball("Ball_1", settings.ballRadius, settings)
    val ball2 = Ball("Ball_2", settings.ballRadius - 1, settings) // optional second ball
    private var imageFrame: ImageFrame? = null
    var bufferedImage: BufferedImage
    val ballRelaxedMargin = (ball.radius * settings.RELAX_MARGIN).toInt()
    val ball2RelaxedMargin = (ball2.radius * settings.RELAX_MARGIN).toInt()

    init {
        val rhoTheta = RhoTheta(0.0, 0.0)
        ball.setPositionRhoTheta(rhoTheta)
        if (settings.isTantalus) {
            val rhoTheta2 = getBall2RhoTheta(rhoTheta)
            ball2.setPositionRhoTheta(rhoTheta2)
        }

        initializeSandGrid(settings.initialSandDepth)
        val backgroundImageFile = File(settings.backgroundImageName)
        val isBackgroundImagePresent = backgroundImageFile.exists()
        bufferedImage = when {
            isBackgroundImagePresent -> readInCleanedImage(backgroundImageFile)
            else                     -> BufferedImage(settings.baseTableDiameter, settings.baseTableDiameter, TYPE_INT_ARGB)
        }
        if (!settings.isHeadless) imageFrame = ImageFrame(bufferedImage, settings)
    }

    /** Initialize sand grid to uniform density */
    private fun initializeSandGrid(initialSandDepth: Double) {
        (0..<settings.baseTableDiameter).forEach { i ->
            (0..<settings.baseTableDiameter).forEach { j ->
                sandGrid[i][j] = initialSandDepth // some sand in every square
            }
        }
    }

    /**
     *    Read in a pre-generated image of a "clean" cycle as a starting point for the sand.
     *
     *    For reference...
     *    int red = (rgb>>16)&0x0ff;
     *    int green=(rgb>>8) &0x0ff;
     *    int blue= (rgb)    &0x0ff;
     */
    private fun readInCleanedImage(cleanFile: File): BufferedImage {
        val backgroundImage = ImageIO.read(cleanFile)
        // set the sand height to the image
        (0..<settings.baseTableDiameter).forEach { i ->
            (0..<settings.baseTableDiameter).forEach { j ->
                //                println("i = $i, j = $j")
                val rgb = backgroundImage.getRGB(i, j)
                val red: Int = (rgb and 0xff0000) shr 16
                val newLevel = red.toDouble() / 30 // 30 seems to work...
                sandGrid[i][j] = newLevel
            }
        }
        return backgroundImage
    }

    fun setTarget(rhoTheta: RhoTheta) {
        ball.setTargetRhoTheta(rhoTheta)
        if (settings.isTantalus) {
            val ball2RhoTheta = getBall2RhoTheta(rhoTheta)
            ball2.setTargetRhoTheta(ball2RhoTheta)
        }
    }

    fun setInitialBallPosition(rhoTheta: RhoTheta) {
        ball.setPositionRhoTheta(rhoTheta)
        ball.setTargetRhoTheta(rhoTheta)
        if (settings.isTantalus) {
            val ball2RhoTheta = getBall2RhoTheta(rhoTheta)
            ball2.setPositionRhoTheta(ball2RhoTheta)
            ball2.setTargetRhoTheta(ball2RhoTheta)
        }
    }


    fun update() {
        val rhoTheta = ball.updatePosition()
        // push the sand up
        if (!settings.hideBallOne) {
            makeBallPushSand(ball)
            // let the sand settle
            relaxSand(ball, ballRelaxedMargin)
        }
        if (settings.isTantalus) {
            // force ball 2 to mirror ball 1 - BUT - when main ball rho is small and delta theta is large, we get large jumps between ball 2's positions - so we must split
            //            these large jumps into smaller ones.
            val newBall2RhoTheta = getBall2RhoTheta(rhoTheta)
            if (ball.positionRhoTheta.rho < 50) {
                val currentBall2RhoTheta = ball2.positionRhoTheta

                if (calculateDistance(rhoTheta, newBall2RhoTheta, settings) > 2) {
                    val deltaRho = newBall2RhoTheta.rho - currentBall2RhoTheta.rho
                    val deltaTheta = newBall2RhoTheta.theta - currentBall2RhoTheta.theta
                    val numSteps = max(1, abs(deltaTheta / .01).toInt())
                    val deltaThetaPerStep = deltaTheta / numSteps
                    var ball2TransitionalRhoTheta = currentBall2RhoTheta
                    for (i in 0 until numSteps) {
                        ball2TransitionalRhoTheta = RhoTheta(ball2TransitionalRhoTheta.rho + deltaRho, ball2TransitionalRhoTheta.theta + deltaThetaPerStep)
                        ball2.setPositionRhoTheta(ball2TransitionalRhoTheta)
                        makeBallPushSand(ball2)
                        relaxSand(ball2, ball2RelaxedMargin)
                    }
                }
            }
            else {
                ball2.setPositionRhoTheta(newBall2RhoTheta)
                makeBallPushSand(ball2)
                relaxSand(ball2, ball2RelaxedMargin)
            }
        }
    }

    fun moveToNextRhoTheta(
        index: Int,
        rhoTheta: RhoTheta,
    ) {
        // set the ball position to the first point in the sequence, instead of 0 - we might start at the outside (1) instead of the inside (0)
        if (index == 0) {
            setInitialBallPosition(rhoTheta)
        }
        setTarget(rhoTheta)
        var count = 0
        while (!ballAtTarget(ball)) {
            update()
            count++
        }
        if (index % settings.imageSkipCount == 0) {
            renderSandImage()
        }
    }

    /**
     * This method simulates the ball pushing the sand out of its personal space.
     */
    private fun makeBallPushSand(ball: Ball) {
        // Iterate over the area affected by the ball's radius
        val ballX = calculateCornerXY(ball.positionXy.x, settings)
        val ballY = calculateCornerXY(ball.positionXy.y, settings)
        val radius = ball.radius
        for (i in ballX - radius..ballX + radius) {
            for (j in ballY - radius..ballY + radius) {
                if (i in 0..<settings.baseTableDiameter && j >= 0 && j < settings.baseTableDiameter) {
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

    /** This method checks if a given (x,y) coordinate is inside the table - (0, 0) is the top-left corner of the table (sand array, not ball coordinates) */
    private fun isInsideTable(x: Int, y: Int): Boolean {
        val isXInside = x in 0..<settings.baseTableDiameter
        val isYInside = y in 0..<settings.baseTableDiameter
        return isXInside && isYInside
    }

    private fun relaxSand(ball: Ball, ballRelaxedMargin: Int) {
        var startX = calculateCornerXY(ball.startPosition.x, settings)
        var startY = calculateCornerXY(ball.startPosition.y, settings)
        var endX = calculateCornerXY(ball.positionXy.x, settings)
        var endY = calculateCornerXY(ball.positionXy.y, settings)

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
        if (endX >= settings.baseTableDiameter) endX = settings.baseTableDiameter - 1
        if (endY >= settings.baseTableDiameter) endY = settings.baseTableDiameter - 1

        var settled: Boolean
        do {
            settled = true
            val lowerNeighbors = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)

            for (y in startY..<endY - 1) {
                for (x in startX..<endX - 1) {
                    val sandHeightHere = sandGrid[x][y]
                    val sandHeightHereMinusSlope = sandHeightHere - settings.MAX_SLOPE
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
                        val d = settings.REDISTRIBUTION_RATE * 2.0 / neighborIndex

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

        for (x in 0..<settings.baseTableDiameter) {
            for (y in 0..<settings.baseTableDiameter) {
                val gray = minOf(255, (sandGrid[x][y] * 30).toInt()) // Simplified calculation
                bufferedImage.setRGB(x, y, encode32bit(gray))
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
        var newGreyscale = greyscale
        newGreyscale = newGreyscale and 0xff
        val red: Int = min((newGreyscale * settings.redConversion).toInt(), 255)
        val green: Int = min((newGreyscale * settings.greenConversion).toInt(), 255)
        val blue: Int = min((newGreyscale * settings.blueConversion).toInt(), 255)
        val resultRgb = when {
            settings.useGreyBackground -> Color(newGreyscale, newGreyscale, newGreyscale).rgb
            else                       -> Color(red, green, blue).rgb
        }
        return resultRgb
    }

    /** used for testing */
    fun ballAtTarget(ball: Ball): Boolean {
        return ball.atTarget
    }
}

