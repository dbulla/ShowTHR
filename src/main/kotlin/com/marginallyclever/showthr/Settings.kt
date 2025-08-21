package com.marginallyclever.showthr

import java.awt.Toolkit
import javax.imageio.ImageIO

class Settings {
    companion object {
        const val MAX_SLOPE = 1.0 // Threshold for sand redistribution
        const val REDISTRIBUTION_RATE = 0.5 // Amount of sand transferred per step
        const val RELAX_MARGIN = 4.0 // must be at greater than 1.
        const val PROGRESS_THRESHOLD = 4.0
        var shouldExpandSequences = true
        const val NUMBER_OF_TURNS_TO_CLEAN = 200

        var ballRadius: Double = 5.0
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
        lateinit var ext: String
        var useGreyBackground = false
        const val redConversion = 255 / 255.0
        const val greenConversion = 244 / 255.0
        const val blueConversion = 200 / 255.0
        var isHeadless = false


        /**
         * Read the command line arguments and set the inputFilename, outputFilename, w, h, ballSize, and initialDepth.
         *
         * @param args the command line arguments
         * @return true if the arguments are valid
         */
        fun parseInputs(args: Array<String>): Boolean {
            if (args.isEmpty()) {
                return false
            }
            try {
                var index = 0
                while (index < args.size) {
                    when (args[index]) {
                        "-b"          -> backgroundImageName = setValueFromArg(++index, args)
                        "-c"          -> isGenerateCleanBackdrop = true
                        "-d"          -> initialSandDepth = setValueFromArg(++index, args).toDouble()
                        "-e"          -> shouldExpandSequences = setValueFromArg(++index, args).toBoolean()
                        "-headless" -> isHeadless = true
                        "-i"          -> inputFilename = args[++index]
                        "-g"          -> useGreyBackground = true
                        "-skip"       -> imageSkipCount = setValueFromArg(++index, args).toInt()
                        "-o"          -> outputFilename = setValueFromArg(++index, args)
                        "-q"          -> shouldQuitWhenDone = true
                        "-r"          -> isReversed = true
                        "-s"          -> ballRadius = setValueFromArg(++index, args).toDouble()
                        "-h"          -> height = setValueFromArg(++index, args).toInt()
                        "-w"          -> width = setValueFromArg(++index, args).toInt()
                        else          -> {
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

            // default output name to input name and png
            ext = outputFilename.substringAfterLast('.')
            return true
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

    }
}