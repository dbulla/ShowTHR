package com.marginallyclever.showthr

import java.awt.Toolkit
import java.io.File
import java.io.IOException
import java.time.Duration
import java.time.Instant
import javax.imageio.ImageIO

/**
 *
 * Simulate a sand table from a THR file and save the result as an image.
 *
 * `ShowTHR inputfile.thr outputfile [-w width] [-h height] [-b ball size] [-d sand depth]`
 *
 * The THR file is a text file that describes the motion of a ball across a table of sand.  The output file is an
 * image of the sand table after the ball has moved.
 *
 * THR format is a text file with one command per line.  Each command is "theta rho", where theta is an angle in
 * radians and rho is a value from 0...1.  lines that are blank or begin with # can be safely ignored.
 *
 * The simulation attempts to push some sand away from the ball and then
 */
object ShowTHR {
    private lateinit var inputFilename: String
    private lateinit var outputFilename: String
    private var height = Toolkit.getDefaultToolkit().screenSize.height
    private var width = height // circular table
    private var ballSize: Double = 5.0
    private var initialDepth: Double = 2.0
    private lateinit var ext: String

    @JvmStatic
    fun main(args: Array<String>) {
        println("ShowTHR")

        if (readInputs(args) || outputFileNotSupported()) {
            showHelp()
            return
        }

        printSettings()

        // get start time
        val start = Instant.now()

        val sandSimulation = SandSimulation(width, height, ballSize, initialDepth)
        try {
            sandSimulation.processFile(inputFilename)
        } catch (e: IOException) {
            println("Error reading file " + inputFilename + ": " + e.message)
        }

        val image = sandSimulation.renderSandImage()
        try {
            // save the image to disk
            val file = File(outputFilename)
            ImageIO.write(image, ext, file)
            println("Image saved to " + file.absolutePath)
        } catch (e: IOException) {
            println("Error saving file " + outputFilename + ": " + e.message)
        }
        // get end time
        val end = Instant.now()
        println("Done!  Time taken: " + Duration.between(start, end).seconds + " s")
    }

    // verify the file extension is supported by ImageIO
    private fun outputFileNotSupported(): Boolean {
        if (!ImageIO.getImageWritersByFormatName(ext).hasNext()) {
            println("Unsupported file format $ext")
            return true
        }
        return false
    }

    // print the settings
    private fun printSettings() {
        println("inputFilename=$inputFilename")
        println("outputFilename=$outputFilename")
        println("w=$width")
        println("h=$height")
        println("ballSize=$ballSize")
        println("initialDepth=$initialDepth")
    }

    /**
     * Read the command line arguments and set the inputFilename, outputFilename, w, h, ballSize, and initialDepth.
     *
     * @param args the command line arguments
     * @return true if the arguments are invalid
     */
    private fun readInputs(args: Array<String>): Boolean {
        if (args.isEmpty()) {
            return true
        }
        inputFilename = args[0]

        // default output name to input name and png
        outputFilename = inputFilename.replace("thr", "png")
//        outputFilename = inputFilename.replace("thr", "JPEG")// only png seems to work...

        var index = 1
        while (index < args.size) {
            when (args[index]) {
                "-o" -> {
                    index++
                    if (index == args.size) {
                        println("Missing value for -o")
                        return true
                    }
                    outputFilename = args[index].trim { it <= ' ' }
                }

                "-w" -> {
                    index++
                    if (index == args.size) {
                        println("Missing value for -w")
                        return true
                    }
                    width = args[index].trim { it <= ' ' }.toInt()
                }

                "-h" -> {
                    index++
                    if (index == args.size) {
                        println("Missing value for -h")
                        return true
                    }
                    height = args[index].trim { it <= ' ' }.toInt()
                }

                "-b" -> {
                    index++
                    if (index == args.size) {
                        println("Missing value for -b")
                        return true
                    }
                    ballSize = args[index].trim { it <= ' ' }.toDouble()
                }

                "-d" -> {
                    index++
                    if (index == args.size) {
                        println("Missing value for -d")
                        return true
                    }
                    initialDepth = args[index].trim { it <= ' ' }.toDouble()
                }

                else -> {
                    println("Unknown option " + args[index])
                    return true
                }
            }
            index++
        }
        ext = outputFilename.substring(outputFilename.lastIndexOf('.') + 1)
        return false
    }

    private fun showHelp() {
        println("ShowTHR inputfile.thr outputfile.png [-w width] [-h height] [-b ball size] [-d initial depth]")
        println("default width=300, height=300, ball size=5, initial depth=2")
        println("output formats supported: " + ImageIO.getWriterFormatNames().contentToString())
    }
}
