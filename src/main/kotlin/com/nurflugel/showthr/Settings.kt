package com.nurflugel.showthr

import com.nurflugel.showthr.Utilities.Companion.setValueFromArg
import java.awt.Toolkit
import javax.imageio.ImageIO

class Settings {
    companion object {
        const val MAX_SLOPE = 1.0 // Threshold for sand redistribution
        const val REDISTRIBUTION_RATE = 0.5 // Amount of sand transferred per step
        const val RELAX_MARGIN = 4.0 // must be at greater than 1.
        const val PROGRESS_THRESHOLD = 4.0
        const val NUMBER_OF_TURNS_TO_CLEAN = 200
        const val RED_CONVERSION = 255 / 255.0
        const val GREEN_CONVERSION = 244 / 255.0
        const val BLUE_CONVERSION = 200 / 255.0
    }

    var makeAnimation: Boolean = false
    var useTwoBalls = false

    var ballRadius: Int = 5
    var initialSandDepth: Double = 2.0
    var isGenerateCleanBackdrop = false
    var cleanBeforeRendering = false
    var isReversed = false
    var shouldQuitWhenDone = false
    var backgroundImageName = ""
    var imageSkipCount = 4
    var inputFilename: String? = null
    var outputFilename: String? = null
    var shouldExpandSequences = true
    var tableDiameter = 100
    var tableRadius = 0
    var centerX = 0
    var centerY = 0
    var proposedLength = 0.0
    lateinit var ext: String
    var useGreyBackground = false
    private val speed = 1.0 // Arbitrary speed value

    var isHeadless = false
    var maxRadius = tableRadius - 20
    val deltaTime = 0.2 // todo possibly add this to settings
    val batchTracks: MutableList<String> = mutableListOf()
    var batchTrackFile: String? = null

    /**
     * Read the command line arguments and set the inputFilename, outputFilename, w, h, ballSize, and initialDepth.
     *
     * @param args the command line arguments
     * @return true if the arguments are valid
     */
    fun parseInputs(args: Array<String>): Boolean {
        println("args= ${args.joinToString(",")}")
        try {
            if (getSettingsFromArgs(args)) {
                adjustSettings()
                calculateCenter()
                return true
            }
        } catch (e: Exception) {
            println("Problem parsing arguments ${e.message}")
        }
        return false
    }

    private fun adjustSettings() {
        if (!isHeadless && tableDiameter == 0) {
            tableDiameter = Toolkit.getDefaultToolkit().screenSize.height - 100
        }
        if (isGenerateCleanBackdrop) {
            inputFilename = "clean.thr"  // should figure out a better way to noop this
            backgroundImageName = "clean_${tableDiameter}x$tableDiameter.png"
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
            //            if (outputFilename == null) outputFilename = inputFilename?.replace(".thr", ".jpg") // this doesn't work for some reason, only png
            if (isReversed) outputFilename = outputFilename!!.replace(".png", "_reversed.png")
            if (useTwoBalls) outputFilename = outputFilename!!.replace(".png", "_2balls.png")
            if (backgroundImageName.trim().isEmpty()) backgroundImageName = "clean_${tableDiameter}x${tableDiameter}.png"
        }
        // default output name to input name and png
        ext = outputFilename!!.substringAfterLast('.')
    }

    @Suppress("SpellCheckingInspection")
    private fun getSettingsFromArgs(args: Array<String>): Boolean {
        if (args.isEmpty()) {
            println("Problem parsing arguments - no arguments")
            printHelp()
            return false
        }
        var index = 0
        while (index < args.size) {
            val argument = args[index].lowercase()
            when (argument) { // use lowercase, so if they get it wrong, it still works.
                // mandatory arguments
                "-i"                 -> inputFilename = args[++index]
                // Optional arguments with args and default values
                "-background"        -> backgroundImageName = setValueFromArg(++index, args)
                "-ballradius"        -> ballRadius = setValueFromArg(++index, args).toInt()
                "-batchtrackfile"    -> batchTrackFile = setValueFromArg(++index, args)
                "-batchtracks"       -> {
                    val fileNames = setValueFromArg(++index, args)
                        .split(",")
                        .map { it.trim() }
                        .map { if (it.endsWith(".thr")) it else "$it.thr" }
                    batchTracks.addAll(fileNames)
                }

                "-depth"             -> initialSandDepth = setValueFromArg(++index, args).toDouble()
                "-o"                 -> outputFilename = setValueFromArg(++index, args)
                "-skip"              -> imageSkipCount = setValueFromArg(++index, args).toInt()
                "-tablesize"         -> tableDiameter = setValueFromArg(++index, args).toInt()

                // Optional no-value arguments
                "-2balls"            -> useTwoBalls = true
                "-doclean"           -> cleanBeforeRendering = false
                "-expandtracks"      -> shouldExpandSequences = setValueFromArg(++index, args).toBoolean()
                "-gray"              -> useGreyBackground = true
                "-headless"          -> isHeadless = true
                "-makeanimation"     -> makeAnimation = true
                "-makecleanbackdrop" -> isGenerateCleanBackdrop = true
                "-q"                 -> shouldQuitWhenDone = true
                "-reversed"          -> isReversed = true
                else                 -> {
                    println("Unknown option $argument")
                    printHelp()
                    return false
                }
            }
            index++
        }
        return true
    }

    // print the settings
    fun printSettings() {
        println("       -i inputFilename =      $inputFilename")
        println("       -background =           $backgroundImageName")
        println("       -ballRadius =           $ballRadius ")
        println("       -batchTrackFile =       $batchTrackFile")
        println("       -batchTracks =          $batchTracks")
        println("       -depth =                $initialSandDepth")
        println("       -o =                    $outputFilename")
        println("       -skip =                 $imageSkipCount")
        println("       -tableSize =            $tableDiameter")
        println("       -2balls =               $useTwoBalls")
        println("       -doClean =              $cleanBeforeRendering")
        println("       -expandTracks  =        $shouldExpandSequences")
        println("       -gray =                 $useGreyBackground")
        println("       -headless =             $isHeadless ")
        println("       -makeCleanBackdrop =    $isGenerateCleanBackdrop")
        println("       -makeAnimation =        $makeAnimation")
        println("       -q  =                   $shouldQuitWhenDone")
        println("       -reversed =             $isReversed")
    }

    fun printHelp() {
        println("ShowTHR Options")
        println("   Mandatory arguments requiring values:")
        println("       -i inputFilename ")

        println("   Optional arguments requiring values:")
        println("       -background     background_image_name       use the image as the starting background")
        println("       -ballRadius     ball_radius                 radius of the ball in pixels")
        println("       -batchTrackFile file_name                   read the tracks from a batch file like 'tracks.txt'.  These files can have have attributes like 2Balls, etc.")
        println("       -batchTracks    file1.thr,file2.thr...      A comma separated list of .thr files to process.  Overridden by -batchTrackFile")
        println("       -depth          initialDepth                initial depth of the sand in pixels (only used if no background image is supplied)")
        println("       -o              outputFilename              Optional - if not supplied, the inputFilename will be used with a .png extension")
        println("       -skip           imageSkipCount              number of track line images to skip while rendering (more makes the GUI preview play faster")
        println("       -tableSize                                  diameter of the table in pixels.  If not supplied, the screen size will be used")
        println("   Optional no-value arguments:")
        println("       -2balls             2 balls can be better than 1")
        println("       -doClean            If present, clean the sand before rendering")
        println("       -expandTracks       Expand sequences of points to improve rendering quality")
        println("       -gray               Use a grey background instead of the image background")
        println("       -headless           No GUI, just render the image - this works on devices with no display")
        println("       -makeAnimation      Make animation frames of the images - you can run ffmpeg to create a video.  Dir is 'animationImages'")
        println("       -makeCleanBackdrop  Make a backdrop image of clean sand for use in future renderings")
        println("       -q                  Quit after rendering the image")
        println("       -reversed           Render the tracks in reverse order")
    }


    fun calculateCenter() {
        tableRadius = tableDiameter / 2
        centerX = tableRadius
        centerY = centerX
        maxRadius = tableRadius - 20
        proposedLength = 2 * speed * deltaTime / (maxRadius * 2.0 )
    }

    // verify the file extension is supported by ImageIO
    fun isOutputFileIsSupported(): Boolean {
        if (!ImageIO.getImageWritersByFormatName(ext).hasNext()) {
            println("Unsupported file format $ext")
            return false
        }
        return true
    }

}