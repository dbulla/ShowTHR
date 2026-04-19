package com.nurflugel.showthr

import com.nurflugel.showthr.Utilities.Companion.getBall2RhoTheta
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
import kotlin.math.max
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

    @JvmStatic
    fun main(args: Array<String>) {
        val settings = Settings()
        println("ShowTHR")

        if (settings.parseInputs(args) && settings.isOutputFileIsSupported()) {
            settings.printSettings()

            // get start time
            val start = Instant.now()

            val sandSimulation = SandSimulation(settings)
            sandSimulation.initialize()
            settings.batchTracks.forEach {
                try {
                    processThrFile(it, sandSimulation, settings)
                } catch (e: IOException) {
                    println("Error reading file " + settings.inputFilename + ": " + e.message)
                }
                if(settings.saveImage) {
                    try { // save the image to disk
                        val file = File(settings.outputFilename!!)
                        ImageIO.write(sandSimulation.bufferedImage, settings.fileExtension, file)
                        println("Image saved to " + file.absolutePath)
                    } catch (e: IOException) {
                        println("Error saving file " + settings.outputFilename + ": " + e.message)
                    }
                }
                // Make the new background the image that was just generated
                // instead, keep sand grid from previous rendering
                // commenting out, as if we just don't re-initialize the sand grid, it'll keep the previous sand
                //  settings.backgroundImageName = settings.outputFilename!!
            }

            // get end time
            val end = Instant.now()
            println("Done!  Time taken: " + Duration.between(start, end).seconds + " s")
            if (settings.shouldQuitWhenDone && settings.quitOnClose) {
                exitProcess(0)
            }
        }
        else {
            showHelp()
        }
    }

    /**
     * Read a THR file and simulate the sand displacement.
     *
     * @param filename THR file to read
     * @throws IOException if the file cannot be read
     */
    @OptIn(ExperimentalTime::class)
    @Throws(IOException::class)
    fun processThrFile(filename: String, sandSimulation: SandSimulation, settings: Settings) {
        val stringBuilder = StringBuilder()

        var previousPercentage = 0.0
        val startTime = Clock.System.now()

        val expandedSequence = extractRhoThetaPairs(filename, settings)
        if (expandedSequence.isEmpty()) return
        val numLines = expandedSequence.size

        // set the ball position to the first point in the sequence, instead of 0 - we might start at the outside (1) instead of the inside (0)
        val firstTheta = expandedSequence.first().first
        val firstRho = expandedSequence.first().second
        val firstRhoTheta = RhoTheta(firstRho, firstTheta)
        val ball2RhoTheta = getBall2RhoTheta(firstRhoTheta)
        sandSimulation.setTarget(firstRhoTheta, ball2RhoTheta)

        expandedSequence.forEachIndexed { index, it ->
            val rhoTheta = RhoTheta(it.second, it.first)
            sandSimulation.moveToNextRhoTheta(index, rhoTheta)
            previousPercentage = outputStatus(stringBuilder, filename, index, numLines, previousPercentage, startTime, settings)
        }
        sandSimulation.renderSandImage()
    }

    private fun extractRhoThetaPairs(filename: String, settings: Settings): MutableList<Pair<Double, Double>> {
        val regex = "\\s+".toRegex()
        val trackLines: MutableList<String> = when {
            settings.isGenerateCleanBackdrop -> createCleaningTrack(settings)
            else                             -> {
                BufferedReader(InputStreamReader(FileInputStream(File(filename)))).use { reader ->
                    val lineSequence = reader.lineSequence().toMutableList()
                    if (lineSequence.isEmpty()) TODO("need to show a dialog for this error") // todo throw an error
                    lineSequence
                }
            }
        }
        var sequence: List<Pair<Double, Double>> = parseSequence(trackLines, regex)
        if (settings.isReversed) sequence = sequence.reversed().toMutableList()
        val expandedSequence = expandSequence(sequence, settings)
        println("initial size: ${sequence.size}, expandedSequence size = ${expandedSequence.size}")
        return expandedSequence
    }

    /**
     *  Go through the original file and clean it up, then produce a nice list, consisting of pairs of thetas and rhos.
     */
    private fun parseSequence(lines: List<String>, regex: Regex): MutableList<Pair<Double, Double>> {
        val sequence: MutableList<Pair<Double, Double>> =
            lines.map { it.trim() }
                .filterNot { it.isEmpty() || it.startsWith("#") || it.startsWith("//") || it.startsWith("theta") }
                .map {
                    val parts = it.replace(regex, " ").split(" ")
                    try {
                        val theta = parts[0].toDouble() - PI/2.0 // adding 90 degrees so images appear upright
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
    fun createCleaningTrack(settings: Settings): MutableList<String> {
        val cleaningTrack = mutableListOf<String>()
        cleaningTrack.add("0.0 0.0")
        val turnsInRadians = settings.NUMBER_OF_TURNS_TO_CLEAN * PI
        cleaningTrack.add("$turnsInRadians 1.0")
        // do another 2 PI turns to get a nice clean edge
        cleaningTrack.add("${turnsInRadians + 4 * PI} 1.0")
        cleaningTrack.add("${turnsInRadians + 8 * PI} 1.0")

        return cleaningTrack
    }

    /**
     * The problem is that the app will draw straight lines in x,y space between two points - and when you only have a change in theta, it draws a straight line instead of
     * the curve that it should be.  So, for any case where theta changes but rho does not, we need to expand the sequence with many intermediate points to fake the curve.
     */
    fun expandSequence(sequence: List<Pair<Double, Double>>, settings: Settings): MutableList<Pair<Double, Double>> {
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
                    if (numPoints == 1) { // special case to prevent division by zero below
                        newSequence.add(Pair(theta1, rho1))
                    }
                    else {
                        val deltaRho = rhoDiff / (numPoints - 1)
                        val deltaTheta = thetaDiff / (numPoints - 1)

                        (0..<numPoints).forEach { j ->
                            val newTheta = theta1 + deltaTheta * j
                            val newRho = rho1 + deltaRho * j
                            newSequence.add(Pair(newTheta, newRho))
                        }
                    }
                }
                else newSequence.add(Pair(theta1, rho1))
            }
            if (sequence.isNotEmpty()) newSequence.add(sequence.last())
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
    @OptIn(ExperimentalTime::class)
    private fun outputStatus(
        stringBuilder: StringBuilder,
        shortFilename: String,
        index: Int,
        numLines: Int,
        previousPercentageThreshold: Double,
        startTime: kotlin.time.Instant,
        settings: Settings,
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
            
Usage: ./gradlew run --args="-i inputFile.thr [options]"   
  
  or, build the jar with 'gradlew shadowJar' and run with 
  
  'java -jar build/libs/showthr-all.jar -i inputFile.thr [options]'


Optional flags with arguments:
    
    -o              outputFilename        If present, the output file will be written to this file.
    -background     backgroundImageName   Use the supplied image as the background image.  Will be blank if it doesn't exist.  Uses "clean.png" if not supplied.
    -depth          initialDepth          Initial depth of the sand.  Default is 2.  Ignored if you have a background image.
    -deltaTime      deltaTime             Determines how fine the time slice is - the smaller the number, the slower (but smoother) the animation.  Default is 2.
    -expand         ExpandSequences       If true (default), will preprocess the .thr file to deal with polar->x,y conversion issues.
    -skip           imageSkipCount        How many lines are skipped before the image is refreshed - 1 is slowest, higher is faster (but jerkier).
    -ballRadius     ballSize              Sets the ball size.  Default is 5.
    -tableDiameter  table size            Sets the diameter of the sand table.  Default is the screen height.
    -batchTracks    batch track list      List of file names to process - each will draw on top of the previous one.

Optional flags without arguments:
    -clean            If present, will generate a "clean_SIZE.png" image to be used as a background image.
    -hideBall1        If present, the first ball will not be drawn.
    -quit             If present, the program will quit after it has finished running.  Else, it will stop with the image displayed (default).
    -reversed         If present, the .thr file will be read in reversed order.
    -tantalus         Tantalus mode - draw with two balls.
    -grey             Use a grey background instead of a "clean" track background. 
    -headless         Generate the image w/o any GUI.
    -hideBall1        Use two balls, but don't show the first ball.
    -wait             Wait for the space bar to be pressed before starting the animation.  Useful for screen recording.


    
Output formats supported: " + ${ImageIO.getWriterFormatNames().contentToString()}
    
                            """.trimIndent()
        )
    }
}
