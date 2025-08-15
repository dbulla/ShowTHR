package com.marginallyclever.showthr

import java.awt.image.BufferedImage
import java.io.*
import javax.vecmath.Vector2d
import kotlin.math.*


/**
 * A simulation of loose sand on a table and being displaced by a ball.
 */
@Suppress("PrivatePropertyName")
class SandSimulation(private val tableWidth: Int, private val tableHeight: Int, ballRadius: Double, initialSandDepth: Double) {

    private val MAX_SLOPE = 1.0 // Threshold for sand redistribution
    private val REDISTRIBUTION_RATE = 0.5 // Amount of sand transferred per step
    private val RELAX_MARGIN = 4.0 // must be at greater than 1.
    private val PROGRESS_THRESHOLD = 2.0
    private val EXPAND_SEQUENCES = true

    private val sandGrid = Array(tableWidth) { DoubleArray(tableHeight) } // 2D array for sand density
    private val ball = Ball(ballRadius)
    private lateinit var startPosition: Vector2d

    init {
        ball.position = (Vector2d(tableWidth / 2.0, tableHeight / 2.0))

        // Initialize sand grid to uniform density
        (0..<tableWidth).forEach { i ->
            (0..<tableHeight).forEach { j ->
                sandGrid[i][j] = initialSandDepth // some sand in every square
            }
        }
    }

    fun setTarget(x: Double, y: Double) {
        ball.setTarget(x, y)
        startPosition = ball.position
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
                if (i in 0..<tableWidth && j >= 0 && j < tableHeight) {
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
//        return (x in 0..<tableWidth) && (y in 0..<tableHeight)
        return (x >= 0 && x < tableWidth) && (y >= 0 && y < tableHeight)


//        return (x in 0..<tableWidth) && (y in 0..<tableHeight)
    }

    /**
     * Make the sand naturally collapse into a more stable shape.
     */
    @Suppress("DuplicatedCode")
    private fun relaxSand() {

        var startX = startPosition.x.toInt()
        var endX = ball.position.x.toInt()
        if (startX > endX) {
            val temp = startX
            startX = endX
            endX = temp
        }
        startX -= (ball.radius * RELAX_MARGIN).toInt()
        endX += (ball.radius * RELAX_MARGIN).toInt()

        var startY = startPosition.y.toInt()
        var endY = ball.position.y.toInt()
        if (startY > endY) {
            val temp = startY
            startY = endY
            endY = temp
        }
        startY -= (ball.radius * RELAX_MARGIN).toInt()
        endY += (ball.radius * RELAX_MARGIN).toInt()

        if (startX < 0) startX = 0
        if (startY < 0) startY = 0
        if (endX >= tableWidth) endX = tableWidth - 1
        if (endY >= tableHeight) endY = tableHeight - 1

        var settled: Boolean
        do {
            settled = true
            val lowerNeighbors = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)

            for (y in startY..<endY - 1) {
                for (x in startX..<endX - 1) {
                    val here = sandGrid[x][y]
                    var c = 0

                    // Check up, down, left, right neighbors
                    if (insideTable(x - 1, y) && sandGrid[x - 1][y] < here - MAX_SLOPE) {
                        lowerNeighbors[c++] = x - 1
                        lowerNeighbors[c++] = y
                    }
                    if (insideTable(x + 1, y) && sandGrid[x + 1][y] < here - MAX_SLOPE) {
                        lowerNeighbors[c++] = x + 1
                        lowerNeighbors[c++] = y
                    }
                    if (insideTable(x, y - 1) && sandGrid[x][y - 1] < here - MAX_SLOPE) {
                        lowerNeighbors[c++] = x
                        lowerNeighbors[c++] = y - 1
                    }
                    if (insideTable(x, y + 1) && sandGrid[x][y + 1] < here - MAX_SLOPE) {
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
        val image = BufferedImage(tableWidth, tableHeight, BufferedImage.TYPE_INT_ARGB)

        var max = 0.0
        for (i in 0..<tableWidth) {
            for (j in 0..<tableHeight) {
                max = max(max, sandGrid[i][j])
            }
        }
        println("max = $max")

        for (i in 0..<tableWidth) {
            for (j in 0..<tableHeight) {
                val gray = (sandGrid[i][j] * 255.0 / max).toInt() // Convert density to grayscale
                image.setRGB(i, j, encode32bit(gray))
            }
        }
        return image
    }

    /**
     * @param greyscale 0-255
     * @return RGB fully opaque
     */
    private fun encode32bit(greyscale: Int): Int {
        var newGreyscale = greyscale
        newGreyscale = newGreyscale and 0xff
        return (0xff shl 24) or (newGreyscale shl 16) or (newGreyscale shl 8) or newGreyscale
    }

    /**
     * Read a THR file and simulate the sand displacement.
     *
     * @param filename THR file to read
     * @throws IOException if the file cannot be read
     */
    @Throws(IOException::class)
    fun processFileNew(filename: String) {
        val file = File(filename)


        val centerX = tableWidth / 2
        val centerY = tableHeight / 2
        val maxRadius = tableWidth / 2 - 20

        var firstLine = true
        val shortFilename = file.name
        val stringBuilder = StringBuilder()

        val regex = "\\s+".toRegex()
        var previousPercentage = 0.0
        BufferedReader(InputStreamReader(FileInputStream(file))).use { reader ->

            //            val sequence = reader.lineSequence()
            //                .mapIndexedNotNull { index, line ->
            //                    line.trim() // filter out any comments or blank lines
            //                        .takeIf { it.isNotEmpty() && !it.startsWith("#") && !it.startsWith("//") }?.let {
            //                            index to it
            //                        }
            //                }.toList()
            val sequence = reader.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") && !it.startsWith("//") }
                .map {
                    val parts = it.replace(regex, " ").split(" ")
                    val theta = parts[0].toDouble()
                    val rho = parts[1].toDouble()
                    Pair(theta, rho)
                }
                .toList()

            val expandedSequence = expandSequence(sequence)
            //            File("temp.txt").delete()
            //            val br = BufferedWriter(FileWriter("temp.txt", false))
            //            for (pair in expandedSequence) {
            //                br.write("${pair.first}    ${pair.second}" + System.lineSeparator())
            //            }
            //            br.close()

            println("initial size: ${sequence.size}, expandedSequence size = ${expandedSequence.size}")
            val numLines = expandedSequence.size
            val firstTheta = expandedSequence.first().first
            val firstRho = expandedSequence.first().second
            setTarget(centerX + sin(firstTheta) * firstRho, centerY - cos(firstTheta) * firstRho)
            expandedSequence.forEachIndexed { index, it ->
                val theta = it.first
                val rho = it.second * maxRadius

                val y = centerY - cos(theta) * rho
                val x = centerX + sin(theta) * rho

                if (firstLine) {
                    ball.position.x = x
                    ball.position.y = y
                    firstLine = false
                }

                setTarget(x, y)
                var count = 0
                while (!ballAtTarget()) {
                    update(0.2)
                    count++
                }
                previousPercentage = outputStatus(stringBuilder, shortFilename, index, numLines, theta, rho, previousPercentage)
            }
        }
    }

    /**
     * The problem is that the app will draw straight lines in x,y space between two points - and when you only have a change in theta, it draws a straight line instead of
     * the curve that it should be.  So, for any case where theta changes but rho does not, we need to expand the sequence with many intermediate points to fake the curve.
     */
    private fun expandSequence(sequence: List<Pair<Double, Double>>): MutableList<Pair<Double, Double>> {
        if (EXPAND_SEQUENCES) {
            val newSequence = mutableListOf<Pair<Double, Double>>()
            for (i in 0..<sequence.size - 1) {
                val (theta1, rho1) = sequence[i]
                val (theta2, rho2) = sequence[i + 1]
                val deltaRho = abs(rho1 - rho2)
                val deltaTheta = abs(theta1 - theta2)
                if ((deltaRho > .01 || deltaTheta > 0.1) || (rho1 < .0001 && rho2 < .0001)) {
                    val thetaDiff = theta2 - theta1
                    val rhoDiff = rho2 - rho1
                    val numPoints = max(1, abs(thetaDiff / .01).toInt())
                    if (numPoints == 1) { // check for divide by zero here!
                        newSequence.add(Pair(theta1, rho1))
                    }
                    else {
                        val deltaRho = rhoDiff / (numPoints - 1) // divide by zero here!
                        val deltaTheta = thetaDiff / (numPoints - 1) // divide by zero here!

                        (0..<numPoints).forEach { j ->
                            val newTheta = theta1 + deltaTheta * j
                            val newRho = rho1 + deltaRho * j
                            newSequence.add(Pair(newTheta, newRho))
                        }
                    }
                }
                else newSequence.add(Pair(theta1, rho1))
            }

            return newSequence
        }
        else return sequence.toMutableList()
    }

    /**
     * Outputs the current status of the simulation to the console, including information about the file being processed
     * and relevant computation details.
     *
     * Only when countByTens is 0 or is exceeded should this print.
     */

    private fun outputStatus(
        stringBuilder: StringBuilder,
        shortFilename: String,
        index: Int,
        numLines: Int,
        theta: Double,
        rho: Double,
        previousPercentageThreshold: Double,
    ): Double {
        val percentageComplete = 100.0 * index / numLines
        val shouldPrint = when {
            previousPercentageThreshold == 0.0                                    -> true
            percentageComplete > previousPercentageThreshold + PROGRESS_THRESHOLD -> true
            else                                                                  -> false
        }

        if (shouldPrint) {
            val percent = String.format("%.0f%% ", percentageComplete)

            stringBuilder.append(shortFilename)
                .append("    ")
                .append(percent)
//                .append(String.format("  %.2f    %.2f"))
            val dots = stringBuilder.toString()
            if (dots.isNotEmpty()) println(dots)
            stringBuilder.clear()
            return previousPercentageThreshold + PROGRESS_THRESHOLD
        }
        else
            return previousPercentageThreshold
    }

}

