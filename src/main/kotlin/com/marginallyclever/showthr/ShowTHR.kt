package com.marginallyclever.showthr

import com.nurflugel.showthr.Counter
import com.nurflugel.showthr.Settings
import com.nurflugel.showthr.Settings.Companion.PROGRESS_THRESHOLD
import com.nurflugel.showthr.ThetaRho
import com.nurflugel.showthr.TrackProcessor
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.time.Duration
import java.time.Instant
import java.util.Locale
import javax.imageio.ImageIO
import kotlin.system.exitProcess
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
    val settings = Settings()
    val trackProcessor = TrackProcessor()

    @JvmStatic
    fun main(args: Array<String>) {
        println("ShowTHR")

        if (settings.parseInputs(args) && settings.isOutputFileIsSupported()) {
            settings.printSettings()

            // get start time
            val start = Instant.now()
            if (settings.makeAnimation) {
                val animationDir = File("animationImages")
                animationDir.deleteRecursively() // clear out any existing images
                animationDir.mkdirs()
            }
            val sandSimulation = SandSimulation(settings)
            //            if (settings.batchTracks.isEmpty() && settings.inputFilename != null) {
            //                settings.batchTracks.add(settings.inputFilename!!)
            //            }
            val counter = Counter() // need this so image files from successive tracks don't overwrite each other
            settings.batchTracks.forEach {
                try {
                    //                    val oldBallsSetting = settings.useTwoBalls
                    //                    val oldReversedSetting = settings.isReversed
                    //                    if (it == "clean.thr") {
                    //                        settings.useTwoBalls = true
                    //                        settings.isReversed = true
                    //                    }
                    processThrFile(it, sandSimulation, counter)
                    //                    settings.useTwoBalls = oldBallsSetting
                    //                    settings.isReversed = oldReversedSetting
                } catch (e: IOException) {
                    println("Error reading file " + settings.inputFilename + ": " + e.message)
                }
                sandSimulation.writeImage()
                // Make the new background the image that was just generated
                settings.backgroundImageName = settings.outputFilename!!
            }

            // get end time
            val end = Instant.now()
            println("Image generation done!  Time taken: " + Duration.between(start, end).seconds + " s")
            if (settings.makeAnimation) {
                fireUpFFMPEG()
            }
            if (settings.shouldQuitWhenDone) exitProcess(0)
        }
        else {
            showHelp()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun fireUpFFMPEG() {
        val imageDir = "animationImages"
        val mpgFilename = "ani_multi_${settings.tableDiameter}.mp4"
        checkForFFMPEG()
        val startTime = Clock.System.now()
        val mpgFile = File(mpgFilename)
        if (mpgFile.exists()) mpgFile.delete()
        val commandList = getCommandList(imageDir, mpgFilename)
        val process = ProcessBuilder(commandList).start() // grab the error stream and print that to stdout so we can see any errors
        val bufferedReader = BufferedReader(InputStreamReader(process.errorStream))
        var line: String? = bufferedReader.readLine()
        while (line != null) {
            println(line)
            line = bufferedReader.readLine()
        }
        val duration = Clock.System.now().minus(startTime)
        println("Done creating video, took $duration")
        ProcessBuilder(mutableListOf("open", mpgFilename)).start()
    }

    private fun checkForFFMPEG() {
        val process = ProcessBuilder(mutableListOf("ffmpeg", "-version")).start()
        val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String? = bufferedReader.readLine()
        while (line != null) {
            println(line)
            if (line.lowercase().contains("ffmpeg version")) {
                println("FFMPEG detected")
                return
            }
            line = bufferedReader.readLine()
        }
        println("FFMPEG NOT detected, exiting")
        exitProcess(0)
    }

    private val os: String = System.getProperty("os.name")
    private val isOsX: Boolean = System.getProperty("os.name").lowercase().startsWith("mac os")
    private val isWindows: Boolean = os.lowercase(Locale.getDefault()).startsWith("windows")


    private fun getCommandList(imageDir: String, mpgFilename: String): List<String> {

        //        val command="ffmpeg -f image2 -s 1500x1500 -i animationImages/image_%6d.png -vcodec libx264 -crf 25 -n -pix_fmt yuv420p  ani_1500_multi.mp4"
        //        if (isWindows) {
        //            val quote = if (isWindows) "\""
        //            else ""
        //            val dot = quote + dotExecutablePath + quote
        //            val output = " -o $quote$outputFilePath$quote"
        //            return mutableListOf(dot, "-T$outputFormat", "$quote$dotFilePath$quote$output")
        //        }
        //        else {

        return mutableListOf(
            "ffmpeg", "-f", "image2", "-s", "${settings.tableDiameter}x${settings.tableDiameter}", "-i", "$imageDir/image_%6d.png", "-vcodec", "libx264", "-crf", "25", "-n", "-pix_fmt", "yuv420p",
            mpgFilename
        )
        //        }
    }

    /**
     * Read a THR file and simulate the sand displacement.
     *
     * @param filename THR file to read
     * @throws IOException if the file cannot be read
     */
    @OptIn(ExperimentalTime::class)
    @Throws(IOException::class)
    fun processThrFile(filename: String, sandSimulation: SandSimulation, counter: Counter) {
        val file = File(filename)
        val shortFilename = file.name
        val stringBuilder = StringBuilder()

        var previousPercentage = 0.0
        val startTime = Clock.System.now()

        val thetaRhos = trackProcessor.extractThetaRhoPairs(file)
        if (thetaRhos.isEmpty()) return
        val numLines = thetaRhos.size

        // set the ball position to the first point in the sequence, instead of 0 - we might start at the outside (1) instead of the inside (0)
        sandSimulation.setTarget(thetaRhos.first())
        thetaRhos.forEachIndexed { index, it ->
            //            println("Processing line $index of $numLines")
            previousPercentage = moveToNextThetaRho(it, sandSimulation, index, previousPercentage, stringBuilder, shortFilename, numLines, startTime, counter)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun moveToNextThetaRho(
        thetaRho: ThetaRho,
        sandSimulation: SandSimulation,
        index: Int,
        previousPercentage: Double,
        stringBuilder: StringBuilder,
        shortFilename: String,
        numLines: Int,
        startTime: kotlin.time.Instant,
        counter: Counter,
    ): Double {

        if (index == 0) { // set the ball position to the first point in the sequence, instead of 0 - we might start at the outside (1) instead of the inside (0)
            sandSimulation.setInitialBallPosition(thetaRho)
        }

        sandSimulation.setTarget(thetaRho)
        while (!sandSimulation.ballAtTarget()) {
            sandSimulation.update()
        }
        // Only output images every N lines, or at the very end
        if (index % settings.imageSkipCount == 0 || index == numLines - 1) {
            val image = sandSimulation.renderSandImage()
            if (settings.makeAnimation) {
                writeImageWithCounterName(counter, sandSimulation, image)
            }
        }
        val newPreviousPercentage = outputStatus(stringBuilder, shortFilename, index, numLines, previousPercentage, startTime)
        return newPreviousPercentage
    }

    private fun writeImageWithCounterName(counter: Counter, sandSimulation: SandSimulation, image: BufferedImage) {
        val indexString = String.format("%06d", counter.increment())
        val file = File("animationImages/image_$indexString.png")
        val parentFile = file.parentFile
        parentFile.mkdirs() // make the parent dir if it doesn't exist'
        sandSimulation.writeImage(file, image)
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
            previousPercentageThreshold == 0.0                                    -> true
            percentageComplete > previousPercentageThreshold + PROGRESS_THRESHOLD -> true
            else                                                                  -> false
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
            return previousPercentageThreshold + PROGRESS_THRESHOLD
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
    -w width                    Set the image width.  Default is screen width.
    -skip imageSkipCount        How many lines are skipped before the image is refreshed - 1 is slowest, higher is faster (but jerkier)
    -o outputFilename           If present, the output file will be written to this file
    -q                          No args, if present, the program will quit after it has finished running.  Else, it will stop with the image displayed (default)
    -r                          No args, if present, the .thr file will be read in reversed order.
    -s ballSize                 Sets the ball size.  Default is 5.
    
Output formats supported: " + ${ImageIO.getWriterFormatNames().contentToString()}
    
                            """.trimIndent()
        )
    }
}
