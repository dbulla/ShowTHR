package com.marginallyclever.showthr

import com.marginallyclever.showthr.Utilities.Companion.setValueFromArg
import java.awt.Toolkit
import javax.imageio.ImageIO

class Settings {
    //    companion object {
    val MAX_SLOPE = 1.0 // Threshold for sand redistribution
    val REDISTRIBUTION_RATE = 0.5 // Amount of sand transferred per step
    val RELAX_MARGIN = 4.0 // must be at greater than 1.
    val PROGRESS_THRESHOLD = 4.0
    var shouldExpandSequences = true
    val NUMBER_OF_TURNS_TO_CLEAN = 200
    var useTwoBalls = false

    var ballRadius: Int = 5
    var initialSandDepth: Double = 2.0
    var isGenerateCleanBackdrop = false
    var isReversed = false
    var shouldQuitWhenDone = false
    var backgroundImageName = ""
    var imageSkipCount = 4
    lateinit var inputFilename: String
    lateinit var outputFilename: String
    var height = Toolkit.getDefaultToolkit().screenSize.height - 100
    var width = height // circular table
    var centerX: Int = width / 2
    var centerY: Int = height / 2
    lateinit var ext: String
    var useGreyBackground = false
    val redConversion = 255 / 255.0
    val greenConversion = 244 / 255.0
    val blueConversion = 200 / 255.0
    var isHeadless = false
    var maxRadius = width / 2 - 20


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
                    "-b"           -> backgroundImageName = setValueFromArg(++index, args)
                    "-useTwoBalls" -> useTwoBalls = true
                    "-c"           -> isGenerateCleanBackdrop = true
                    "-d"           -> initialSandDepth = setValueFromArg(++index, args).toDouble()
                    "-e"           -> shouldExpandSequences = setValueFromArg(++index, args).toBoolean()
                    "-headless"    -> isHeadless = true
                    "-i"           -> inputFilename = args[++index]
                    "-g"           -> useGreyBackground = true
                    "-skip"        -> imageSkipCount = setValueFromArg(++index, args).toInt()
                    "-o"           -> outputFilename = setValueFromArg(++index, args)
                    "-q"           -> shouldQuitWhenDone = true
                    "-r"           -> isReversed = true
                    "-s"           -> ballRadius = setValueFromArg(++index, args).toInt()
                    "-h"           -> height = setValueFromArg(++index, args).toInt()
                    "-w"           -> width = setValueFromArg(++index, args).toInt()
                    else           -> {
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
        if (isGenerateCleanBackdrop) {
            inputFilename = "clean.thr"  // should figure out a better way to noop this
            backgroundImageName = "clean_${width}x$height.png"
            outputFilename = backgroundImageName
            imageSkipCount = 1000
            shouldQuitWhenDone = true
        }
        else {
            outputFilename = inputFilename.replace(".thr", ".png") //JPEG doesn't work for me, only png...
            if (isReversed) outputFilename = outputFilename.replace(".png", "_reversed.png")
            if (backgroundImageName.trim().isEmpty()) backgroundImageName = "clean_${width}x${height}.png"
        }
        centerX = width / 2
        centerY = height / 2
        maxRadius = width / 2 - 20
        // default output name to input name and png
        ext = outputFilename.substringAfterLast('.')
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
        println("h - height = $height")
        println("w - width = $width")
        println("skip imageSkipCount = $imageSkipCount")
        println("d - initialDepth = $initialSandDepth")
        println("r - isReversed = $isReversed")
        println("o - outputFilename = $outputFilename")
        println("e - shouldExpandSequences = $shouldExpandSequences")
        println("q - shouldQuitWhenDone = $shouldQuitWhenDone")
        println("ext = $ext")

    }

    //    }
}