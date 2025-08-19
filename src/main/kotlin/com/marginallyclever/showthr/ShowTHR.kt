package com.marginallyclever.showthr

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.time.Duration
import java.time.Instant
import javax.imageio.ImageIO
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.system.exitProcess
import kotlin.text.trim
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


/**
 *
 * Simulate a sand table from a THR file and save the result as an image.
 *
 * `ShowTHR inputfile.thr outputfile [-w width] [-h height] [-b ball size] [-d sand depth]`
 *
 * The THR file is a text file that describes the motion of a ball across a table of sand.  The output file is an
 * image of the sand table after the ball has moved.
 *
 * The THR format is a text file with one command per line.  Each command is "theta rho", where theta is an angle in
 * radians and rho is a value from 0...1.  Lines that are blank or begin with # can be safely ignored.
 *
 * The simulation attempts to push some sand away from the ball and then
 */
object ShowTHR {

    private val settings = Settings()

    @JvmStatic
    fun main(args: Array<String>) {
        println("ShowTHR")
        if (parseInputs(args) && isOutputFileIsSupported()) {
            printSettings()

            // get start time
            val start = Instant.now()

            val sandSimulation = SandSimulation(settings)
            try {
                processThrFile(settings.inputFilename, sandSimulation)
            } catch (e: IOException) {
                println("Error reading file " + settings.inputFilename + ": " + e.message)
            }

            try { // save the image to disk
                val file = File(settings.outputFilename)
                ImageIO.write(sandSimulation.bufferedImage, settings.ext, file)
                println("Image saved to " + file.absolutePath)
            } catch (e: IOException) {
                println("Error saving file " + settings.outputFilename + ": " + e.message)
            }
            // get end time
            val end = Instant.now()
            println("Done!  Time taken: " + Duration.between(start, end).seconds + " s")
            if (settings.shouldQuitWhenDone) exitProcess(0)
        }
        else {
            showHelp()
        }
    }


    /**
     * Read the command line arguments and set the inputFilename, outputFilename, w, h, ballSize, and initialDepth.
     *
     * @param args the command line arguments
     * @return true if the arguments are valid
     */
    private fun parseInputs(args: Array<String>): Boolean {
        if (args.isEmpty()) {
            return false
        }
        try {
            var index = 0
            while (index < args.size) {
                when (args[index]) {
                    "-b"    -> settings.backgroundImageName = setValueFromArg(++index, args)
                    "-c"    -> settings.isGenerateCleanBackdrop = true
                    "-d"    -> settings.initialSandDepth = setValueFromArg(++index, args).toDouble()
                    "-e"    -> settings.shouldExpandSequences = setValueFromArg(++index, args).toBoolean()
                    "-i"    -> settings.inputFilename = args[++index]
                    "-skip" -> settings.imageSkipCount = setValueFromArg(++index, args).toInt()
                    "-o"    -> settings.outputFilename = setValueFromArg(++index, args)
                    "-q"    -> settings.shouldQuitWhenDone = true
                    "-r"    -> settings.isReversed = true
                    "-s"    -> settings.ballRadius = setValueFromArg(++index, args).toDouble()
                    "-h"    -> settings.height = setValueFromArg(++index, args).toInt()
                    "-w"    -> settings.width = setValueFromArg(++index, args).toInt()
                    else    -> {
                        println("Unknown option " + args[index])
                        return false
                    }
                }
                index++
            }
        } catch (e: Exception) {
            println("Problem parsing arguments ${e.message}")
            return false
        }
        //        }
        if (settings.isGenerateCleanBackdrop) {
            settings.inputFilename = "clean.thr"  // should figure out a better way to noop this
            settings.backgroundImageName = "clean_${settings.width}x$settings.height.png"
            settings.outputFilename = settings.backgroundImageName
            settings.imageSkipCount = 1000
            settings.shouldQuitWhenDone = true
        }
        else {
            settings.outputFilename = settings.inputFilename.replace(".thr", ".png") //JPEG doesn't work for me, only png...
            if (settings.isReversed) settings.outputFilename = settings.outputFilename.replace(".png", "_reversed.png")
            if (settings.backgroundImageName.trim().isEmpty()) settings.backgroundImageName = "clean_${settings.width}x${settings.height}.png"
        }

        // default output name to input name and png
        settings.ext = settings.outputFilename.substringAfterLast('.')
        return true
    }


    /**
     * Read a THR file and simulate the sand displacement.
     *
     * @param filename THR file to read
     * @throws IOException if the file cannot be read
     */
    @OptIn(ExperimentalTime::class)
    @Throws(IOException::class)
    fun processThrFile(filename: String, sandSimulation: SandSimulation) {
        val file = File(filename)

        val centerX = settings.width / 2
        val centerY = settings.height / 2
        val maxRadius = settings.width / 2 - 20

        var firstLine = true
        val shortFilename = file.name
        val stringBuilder = StringBuilder()

        val regex = "\\s+".toRegex()
        var previousPercentage = 0.0
        val startTime = Clock.System.now()

        val trackLines: MutableList<String> = when {
            settings.isGenerateCleanBackdrop -> createCleaningTrack()
            else                             -> {
                BufferedReader(InputStreamReader(FileInputStream(file))).use { reader ->
                    val lineSequence = reader.lineSequence().toMutableList()
                    if (lineSequence.isEmpty()) exitProcess(0)
                    lineSequence
                }
            }
        }
        var sequence: List<Pair<Double, Double>> = parseSequence(trackLines, regex)
        if (settings.isReversed) sequence = sequence.reversed().toMutableList()
        val expandedSequence = expandSequence(sequence)
        println("initial size: ${sequence.size}, expandedSequence size = ${expandedSequence.size}")
        val numLines = expandedSequence.size

        // set the ball position to the first point in the sequence, instead of 0 - we might start at the outside (1) instead of the inside (0)
        val firstTheta = expandedSequence.first().first
        val firstRho = expandedSequence.first().second
        sandSimulation.setTarget(centerX + sin(firstTheta) * firstRho, centerY - cos(firstTheta) * firstRho)

        expandedSequence.forEachIndexed { index, it ->
            val theta = it.first
            val rho = it.second * maxRadius

            val y = centerY - cos(theta) * rho
            val x = centerX + sin(theta) * rho

            if (firstLine) { // set the ball position to the first point in the sequence, instead of 0 - we might start at the outside (1) instead of the inside (0)
                sandSimulation.setBallPosition(x, y)
                firstLine = false
            }

            sandSimulation.setTarget(x, y)
            var count = 0
            while (!sandSimulation.ballAtTarget()) {
                sandSimulation.update(0.2)
                count++
            }
            if (index % settings.imageSkipCount == 0) {
                sandSimulation.renderSandImage()
            }
            previousPercentage = outputStatus(stringBuilder, shortFilename, index, numLines, previousPercentage, startTime)
        }
    }

    private fun parseSequence(lines: List<String>, regex: Regex): MutableList<Pair<Double, Double>> {
        val sequence: MutableList<Pair<Double, Double>> =

            lines.map { it.trim() }
                .filterNot { it.isEmpty() || it.startsWith("#") || it.startsWith("//")  || it.startsWith("theta") }
                .map {
                    val parts = it.replace(regex, " ").split(" ")
                    try {
                        val theta = parts[0].toDouble()
                        val rho = parts[1].toDouble()
                        Pair(theta, rho)
                    } catch (e: Exception) {
                        println("Error parsing sequence: ${e.message}: theta=${parts[0]}, rho=${parts[1]}")
                        throw e
                    }
                }
                .toMutableList()

        return sequence
    }

    // if desired, add a "clean" before the main track
    fun createCleaningTrack(): MutableList<String> {
        //        if (true) {
        //            val targetTheta = sequence[0].first
        //            val targetRho = sequence[0].second
        //            val initialRho = when (targetRho) {
        //                0.0  -> 1.0
        //                else -> 0.0
        //            }
        //            val initialTheta = targetTheta - 200.0 * PI
        //            val newSequence = mutableListOf<Pair<Double, Double>>()
        //            newSequence.add(Pair(initialTheta, initialRho))
        //            newSequence.addAll(sequence)
        //            return newSequence
        //        }
        //        else
        //            return sequence
        val cleaningTrack = mutableListOf<String>()
        cleaningTrack.add("0.0 0.0")
        cleaningTrack.add("${settings.NUMBER_OF_TURNS_TO_CLEAN * PI} 1.0")
        cleaningTrack.add("${(settings.NUMBER_OF_TURNS_TO_CLEAN + 2) * PI} 1.0")

        return cleaningTrack
    }

    /**
     * The problem is that the app will draw straight lines in x,y space between two points - and when you only have a change in theta, it draws a straight line instead of
     * the curve that it should be.  So, for any case where theta changes but rho does not, we need to expand the sequence with many intermediate points to fake the curve.
     */
    private fun expandSequence(sequence: List<Pair<Double, Double>>): MutableList<Pair<Double, Double>> {
        if (settings.shouldExpandSequences) {
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

    private fun setValueFromArg(index: Int, args: Array<String>): String {
        if (index < args.size) {
            return args[index].trim { it <= ' ' }
        }
        else {
            println("Missing value for ${args[index - 1]}")
            throw IllegalArgumentException("Missing value for ${args[index - 1]}")
        }
    }

    /**
     * Outputs the current status of the simulation to the console, including information about the file being processed
     * and relevant computation details.
     *
     * Only when countByTens is 0 or is exceeded should this print.
     */
    @OptIn(ExperimentalTime::class)
    private fun outputStatus(
        stringBuilder: StringBuilder,
        shortFilename: String,
        index: Int,
        numLines: Int,
        previousPercentageThreshold: Double,
        startTime: kotlin.time.Instant,
    ): Double {
        val percentageComplete = 100.0 * index / numLines
        val shouldPrint = when {
            previousPercentageThreshold == 0.0                                             -> true
            percentageComplete > previousPercentageThreshold + settings.PROGRESS_THRESHOLD -> true
            else                                                                           -> false
        }

        if (shouldPrint) {
            val percent = String.format("%.0f%% ", percentageComplete)

            val duration = Clock.System.now().minus(startTime)
            val durationMs = duration.inWholeMilliseconds
            val timeRemaining = when {
                index > 0 -> {
                    val timeRemainingMs = (numLines * durationMs / index) - durationMs
                    Duration.ofMillis(timeRemainingMs).toString()
                }

                else      -> "?"
            }
            stringBuilder.append("$shortFilename    $percent    Duration: $duration    timeRemaining: $timeRemaining")
            //                .append(String.format("  %.2f    %.2f"))
            val dots = stringBuilder.toString()

            if (dots.isNotEmpty()) println(dots)
            stringBuilder.clear()
            return previousPercentageThreshold + settings.PROGRESS_THRESHOLD
        }
        else
            return previousPercentageThreshold
    }

    private fun showHelp() {
        print(
            """
            
Usage: ShowTHR inputfile.thr [options]
Optional:
    -b backgroundImageName      Use the supplied image as the background image.  Will be blank if it doesn't exist.  Uses "clean.png" if not supplied.
    -c                          No args, if present will generate a "clean.png" image to be used as a background image.
    -d initialDepth             Initial depth of the sand.  Default is 2.  Ignored if you have a background image.
    -e shouldExpandSequences    If true (default), will preprocess the .thr file to deal with polar->x,y conversion issues
    -h height                   Set the image height.  Default is screen height.
    -h width                    Set the image width.  Default is screen width.
    -skip imageSkipCount        How many lines are skipped before the image is refreshed - 1 is slowest, higher is faster (but jerkier)
    -o outputFilename           If present, the output file will be written to this file
    -q                          No args, if present, the program will quit after it has finished running.  Else, it will stop with the image displayed (default)
    -r                          No args, if present, the .thr file will be read in reversed order.
    -s ballSize                 Sets the ball size.  Default is 5.
    
Output formats supported: " + ${ImageIO.getWriterFormatNames().contentToString()}
    
                            """.trimIndent()
        )
    }


    // verify the file extension is supported by ImageIO
    private fun isOutputFileIsSupported(): Boolean {
        if (!ImageIO.getImageWritersByFormatName(settings.ext).hasNext()) {
            println("Unsupported file format $settings.ext")
            return false
        }
        return true
    }

    // print the settings
    private fun printSettings() {
        println("inputFilename = ${settings.inputFilename}")
        println("b - backgroundImageName = ${settings.backgroundImageName}")
        println("s - ballSize = ${settings.ballRadius}")
        println("h - height = ${settings.height}")
        println("w - width = ${settings.width}")
        println("skip imageSkipCount = ${settings.imageSkipCount}")
        println("d - initialDepth = ${settings.initialSandDepth}")
        println("r - isReversed = ${settings.isReversed}")
        println("o - outputFilename = ${settings.outputFilename}")
        println("e - shouldExpandSequences = ${settings.shouldExpandSequences}")
        println("q - shouldQuitWhenDone = ${settings.shouldQuitWhenDone}")
        println("ext = ${settings.ext}")

    }
}
