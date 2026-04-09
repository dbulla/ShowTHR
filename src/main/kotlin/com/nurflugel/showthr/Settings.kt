package com.nurflugel.showthr

import com.nurflugel.showthr.Utilities.Companion.getValueFromArg
import java.awt.Toolkit
import javax.imageio.ImageIO

@Suppress("PropertyName")
class Settings {

    //    companion object {
    val MAX_SLOPE = 1.0 // Threshold for sand redistribution
    val REDISTRIBUTION_RATE = 0.5 // Amount of sand transferred per step
    val RELAX_MARGIN = 4.0 // must be at greater than 1.
    val PROGRESS_THRESHOLD = 4.0
    var shouldExpandSequences = true
    val NUMBER_OF_TURNS_TO_CLEAN = 200
    var isTantalus = false

    /**
     * At the perimeter of the table, the ball can push the sand past the 1.0 rho level - we want to see
     * that (it looks odd if it's clipped) - so the table is actually a little larger than the rho of 1.0 - by SHOULDER_WIDTH pixels.
     *
     * Note that this makes all the computations nasty - what if we made the actual table width what the user entered PLUS the SHOULDER_WIDTH?
     */
    val SHOULDER_WIDTH = 20

    var ballRadius: Int = 5
    var initialSandDepth: Double = 2.0
    var isGenerateCleanBackdrop = false
    var isReversed = false
    var shouldQuitWhenDone = false
    var backgroundImageName = ""
    var imageSkipCount = 4
    var inputFilename: String? = null
    var outputFilename: String? = null
    var baseTableDiameter = Toolkit.getDefaultToolkit().screenSize.height - 100
    var tableDiameterMinusPadding = baseTableDiameter - SHOULDER_WIDTH * 2
    var hideBallOne: Boolean = false
    lateinit var fileExtension: String
    var useGreyBackground = false
    val redConversion   = 1.2 * 255 / 255.0
    val greenConversion = 1.2 * 244 / 255.0
    val blueConversion  = 1.2 * 200 / 255.0
    var isHeadless = false
    var tableRadius = tableDiameterMinusPadding / 2

    var deltaTime = 2 / 10.0  // good starting point
    val batchTracks: MutableList<String> = mutableListOf()

    fun calculateCenter() {
        tableDiameterMinusPadding = baseTableDiameter - SHOULDER_WIDTH * 2
        tableRadius = tableDiameterMinusPadding / 2
    }

    /**
     * Read the command line arguments and set the inputFilename, outputFilename, w, h, ballSize, and initialDepth.
     *
     * @param args the command line arguments
     * @return true if the arguments are valid
     */
    fun parseInputs(args: Array<String>): Boolean {
        if (args.isEmpty()) {
            println("Problem parsing arguments")
            return false
        }
        try {
            var index = 0
            while (index < args.size) {
                when (args[index]) {
                    "-i"             -> inputFilename = args[++index]
                    "-o"             -> outputFilename = getValueFromArg(++index, args)
                    "-grey"          -> useGreyBackground = true
                    "-background"    -> backgroundImageName = getValueFromArg(++index, args)
                    "-tantalus"      -> isTantalus = true
                    "-clean"         -> {
                        isGenerateCleanBackdrop = true
                        isHeadless = true
                    }

                    "-depth"         -> initialSandDepth = getValueFromArg(++index, args).toDouble()
                    "-expand"        -> shouldExpandSequences = getValueFromArg(++index, args).toBoolean()
                    "-headless"      -> isHeadless = true
                    "-hideBall1"     -> {
                        hideBallOne = true
                        isTantalus = true
                    }

                    "-skip"          -> imageSkipCount = getValueFromArg(++index, args).toInt()
                    "-quit"          -> shouldQuitWhenDone = true
                    "-reversed"      -> isReversed = true
                    "-ballRadius"    -> ballRadius = getValueFromArg(++index, args).toInt()
                    "-tableDiameter" -> baseTableDiameter = getValueFromArg(++index, args).toInt()
                    "-batchTracks"   -> batchTracks.addAll(getValueFromArg(++index, args).split(","))
                    "-deltaTime"     -> deltaTime = getValueFromArg(++index, args).toDouble() / 10.0
                    else             -> {
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
        calculateCenter()
        if (isGenerateCleanBackdrop) {
            inputFilename = "dibble.thr"  // Isn't used, should figure out a better way to noop this
            outputFilename = "clean_${baseTableDiameter}.png"
            imageSkipCount = 1000
            shouldQuitWhenDone = true
            batchTracks.add(inputFilename!!)
        }
        else {
            if (batchTracks.isEmpty() && inputFilename != null) {
                batchTracks.add(inputFilename!!)
            }
            else {
                inputFilename = batchTracks.first()
            }
            if (outputFilename == null) outputFilename = inputFilename?.replace(".thr", ".png")
            //            outputFilename = inputFilename.replace(".thr", ".png") //JPEG doesn't work for me, only png...
            if (isReversed) outputFilename = outputFilename!!.replace(".png", "_reversed.png")
            if (isTantalus) outputFilename = outputFilename!!.replace(".png", "_tantalus.png")
            if (backgroundImageName.trim().isEmpty()) backgroundImageName = "clean_${baseTableDiameter}.png"
        }

        // default output name to input name and png
        fileExtension = outputFilename!!.substringAfterLast('.')
        return true
    }


    // verify the file extension is supported by ImageIO
    fun isOutputFileIsSupported(): Boolean {
        if (!ImageIO.getImageWritersByFormatName(fileExtension).hasNext()) {
            println("Unsupported file format $fileExtension")
            return false
        }
        return true
    }

    // print the settings
    fun printSettings() {
        println("inputFilename = $inputFilename")
        println("outputFilename = $outputFilename")
        println("backgroundImageName = $backgroundImageName")
        println("tantalus  = $isTantalus")
        println("deltaTime  = $deltaTime")
        println("ballRadius = $ballRadius")
        println("tableDiameter = $baseTableDiameter")
        println("skip = $imageSkipCount")
        println("depth = $initialSandDepth")
        println("reversed  = $isReversed")
        println("expand  = $shouldExpandSequences")
        println("useGreyBackground = $useGreyBackground")
        println("quit  = $shouldQuitWhenDone")
        println("hideBallOne = $hideBallOne")
        println("ext = $fileExtension")
        println("isHeadless = $isHeadless")

    }
}