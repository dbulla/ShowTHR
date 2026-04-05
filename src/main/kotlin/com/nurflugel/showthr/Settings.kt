package com.nurflugel.showthr

import com.nurflugel.showthr.Utilities.Companion.setValueFromArg
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
    var useTwoBalls = false

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
    var tableDiameterWithPadding = 0
//    var centerX: Int = 0
//    var centerY: Int = 0
    lateinit var ext: String
    var useGreyBackground = false
    val redConversion = 255 / 255.0
    val greenConversion = 244 / 255.0
    val blueConversion = 200 / 255.0
    var isHeadless = false
    var tableRadius = baseTableDiameter / 2

    //    var maxRadius = tableRadius + SHOULDER_WIDTH
    val deltaTime = 0.2
    val batchTracks: MutableList<String> = mutableListOf()

    fun calculateCenter() {
//        centerX = tableDiameterWithPadding / 2
//        centerY = centerX
        tableRadius = baseTableDiameter / 2 // todo auto remove shoulder width so the image can be exactly the size specified
        //        maxRadius = tableRadius + SHOULDER_WIDTH
        // add the extra padding to the base table diameter
        tableDiameterWithPadding = baseTableDiameter + 2 * SHOULDER_WIDTH

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
                    // todo review these and standardize
                    "-b"             -> backgroundImageName = setValueFromArg(++index, args)
                    "-useTwoBalls"   -> useTwoBalls = true
                    "-c"             -> isGenerateCleanBackdrop = true
                    "-d"             -> initialSandDepth = setValueFromArg(++index, args).toDouble()
                    "-e"             -> shouldExpandSequences = setValueFromArg(++index, args).toBoolean()
                    "-headless"      -> isHeadless = true
                    "-i"             -> inputFilename = args[++index]
                    "-g"             -> useGreyBackground = true
                    "-skip"          -> imageSkipCount = setValueFromArg(++index, args).toInt()
                    "-o"             -> outputFilename = setValueFromArg(++index, args)
                    "-q"             -> shouldQuitWhenDone = true
                    "-reversed"      -> isReversed = true
                    "-s"             -> ballRadius = setValueFromArg(++index, args).toInt()
                    "-tableDiameter" -> baseTableDiameter = setValueFromArg(++index, args).toInt()
                    "-batchTracks"   -> batchTracks.addAll(setValueFromArg(++index, args).split(","))
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
            inputFilename = "clean.thr"  // should figure out a better way to noop this
            backgroundImageName = "clean_${tableDiameterWithPadding}x$tableDiameterWithPadding.png"
            outputFilename = backgroundImageName
            imageSkipCount = 1000
            shouldQuitWhenDone = true
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
            if (useTwoBalls) outputFilename = outputFilename!!.replace(".png", "_2balls.png")
            if (backgroundImageName.trim().isEmpty()) backgroundImageName = "clean_${tableDiameterWithPadding}x${tableDiameterWithPadding}.png"
        }

        // default output name to input name and png
        ext = outputFilename!!.substringAfterLast('.')
        return true
    }


    // verify the file extension is supported by ImageIO
    fun isOutputFileIsSupported(): Boolean {
        if (!ImageIO.getImageWritersByFormatName(ext).hasNext()) {
            println("Unsupported file format $ext")
            return false
        }
        return true
    }

    // print the settings
    fun printSettings() {
        println("inputFilename = $inputFilename")
        println("b - backgroundImageName = $backgroundImageName")
        println("useTwoBalls  = $useTwoBalls")
        println("s - ballSize = $ballRadius")
        println("tableDiameter = $baseTableDiameter")
        println("skip imageSkipCount = $imageSkipCount")
        println("d - initialDepth = $initialSandDepth")
        println("r - isReversed = $isReversed")
        println("o - outputFilename = $outputFilename")
        println("e - expandSequences = $shouldExpandSequences")
        println("q - shouldQuitWhenDone = $shouldQuitWhenDone")
        println("ext = $ext")

    }
}