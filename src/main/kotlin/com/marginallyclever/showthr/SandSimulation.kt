package com.marginallyclever.showthr

import com.nurflugel.showthr.NormalizedThetaRho
import com.nurflugel.showthr.Settings
import com.nurflugel.showthr.Settings.Companion.MAX_SLOPE
import com.nurflugel.showthr.Settings.Companion.REDISTRIBUTION_RATE
import com.nurflugel.showthr.Utilities.Companion.calculateSandX
import com.nurflugel.showthr.Utilities.Companion.calculateSandY
import com.nurflugel.showthr.Utilities.Companion.getBall2ThetaRho
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

    internal val sandGrid: Array<DoubleArray> = Array(settings.tableDiameter) { DoubleArray(settings.tableDiameter) } // 2D array for sand density
    private val ball1 = Ball("Ball_1", settings.ballRadius, settings)
    private val ball2 = Ball("Ball_2", settings.ballRadius - 1, settings) // optional second ball, slightly smaller than the first

    init {
//        ball1.setPosition(NormalizedThetaRho(0.0, 0.0))
//        if (settings.useTwoBalls) {
//            ball2.setPosition(getBall2ThetaRho(ball1.currentPosition))
//        }

        // Initialize sand grid to uniform density
        initializeSandGrid(settings.initialSandDepth)
    }

    private fun initializeSandGrid(initialSandDepth: Double) {
        (0..<settings.tableDiameter).forEach { i ->
            (0..<settings.tableDiameter).forEach { j ->
                sandGrid[i][j] = initialSandDepth // some sand in every square
            }
        }
    }

    fun setTargets(normalizedThetaRho: NormalizedThetaRho) {
        ball1.setTarget(normalizedThetaRho)
        if (settings.useTwoBalls) {
            ball2.setTarget(getBall2ThetaRho(normalizedThetaRho))
        }
    }

    fun setInitialBallPositions(normalizedThetaRho: NormalizedThetaRho) {
        ball1.setInitialPosition(normalizedThetaRho)
        if (settings.useTwoBalls) {
            ball2.setInitialPosition(getBall2ThetaRho(normalizedThetaRho))
        }
    }

    fun updateBalls() {
        ball1.updatePosition()
        if (settings.useTwoBalls) ball2.updatePosition()
        // the next two lines are the bottleneck in the app's performance.
        makeBallPushSand(ball1) // push the sand up
        if (settings.useTwoBalls) makeBallPushSand(ball2) // push the sand up
        relaxSand(ball1) // let the sand settle
        if (settings.useTwoBalls) relaxSand(ball2) // let the sand settle
        //        println("Ball1: ${ball.position}")
    }


    /**
     * This method simulates the ball pushing the sand out of its personal space.
     */
    private fun makeBallPushSand(ball: Ball) {
        // Iterate over the area affected by the ball's radius
        val ballX = calculateSandX(ball.currentPosition, settings).toInt()
        val ballY = calculateSandY(ball.currentPosition, settings).toInt()
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
                            // heightOfBall is the distance from the surface of the ball to the top of the table at the given distance from the center of the ball
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

    /**
     * Make the sand naturally collapse into a more stable shape.
     */
    private fun relaxSand(ball: Ball) {
        var startX = calculateSandX(ball.startPosition, settings).toInt()
        var startY = calculateSandY(ball.startPosition, settings).toInt()
        var endX = calculateSandY(ball.currentPosition, settings).toInt()
        var endY = calculateSandY(ball.currentPosition, settings).toInt()

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

    fun ballsAtTarget(): Boolean {
        return when {
            settings.useTwoBalls -> ball1.atTarget && ball2.atTarget
            else                 -> ball1.atTarget
        }
    }

    fun calculateMaxAndAverageSandHeights(): Double {
        val max = sandGrid.maxOf {
            it.maxOrNull()
            ?: 0.0
        }
        var sum = 0.0
        //        if (settings.isHeadless) {
        for (i in 0..<settings.tableDiameter) {
            sum += sandGrid[i].average()
        }
        println("max = $max, average=${sum / settings.tableDiameter}")
        //        }
        return max
    }


    private fun isInsideTable(x: Int, y: Int): Boolean {
        return when {
            x < 0                      -> false
            y < 0                      -> false
            x > settings.tableDiameter -> false
            y > settings.tableDiameter -> false
            else                       -> true
            //            else -> {
            //                val isXInside = x in 0..<settings.tableDiameter
            //                val isYInside = y in 0..<settings.tableDiameter
            //                return isXInside && isYInside
            //            }
        }
    }

}

