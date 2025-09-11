package com.nurflugel.showthr

import com.nurflugel.showthr.Settings.Companion.BLUE_CONVERSION
import com.nurflugel.showthr.Settings.Companion.GREEN_CONVERSION
import com.nurflugel.showthr.Settings.Companion.RED_CONVERSION
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.max

class ImageRenderer(private val settings: Settings, val sandGrid: Array<DoubleArray>) {

    private var bufferedImage: BufferedImage
    private var imageFrame: ImageFrame? = null

    init {
        val backgroundImageFile = File(settings.backgroundImageName)
        val isBackgroundImagePresent = backgroundImageFile.exists()
        bufferedImage = when {
            isBackgroundImagePresent -> readInCleanedImage(backgroundImageFile)
            else                     -> BufferedImage(settings.tableDiameter, settings.tableDiameter, TYPE_INT_ARGB)
        }
        if (!settings.isHeadless) imageFrame = ImageFrame(bufferedImage, settings)
    }

    /**
     * Render the sand density as a grayscale image.  The darkest pixels have the least sand.
     *
     * @return the image
     */
    fun renderSandImage(settings: Settings): BufferedImage {
        val max = sandGrid.maxOf { it.maxOrNull() ?: 0.0 }
//        println("Max sand: $max")
        for (i in 0..<settings.tableDiameter) {
            for (j in 0..<settings.tableDiameter) {
//                val gray = (sandGrid[i][j] * 255.0 / max).toInt() // Convert density to grayscale - original calc
                val gray = minOf(255, (sandGrid[i][j] * 30).toInt()) // Simplified calculation
                bufferedImage.setRGB(i, j, encode32bit(gray))
            }
        }
        if (!settings.isHeadless) imageFrame?.updateImage(bufferedImage)
        return bufferedImage
    }

    fun writeImage() {
        val file = File(settings.outputFilename!!)
        writeImage(file)
        println("Image saved to " + file.absolutePath)
    }

    /**
     *
     *  Read in a pre-generated image of a "clean" cycle as a starting point for the sand.

     *  Just for reference...
     *  int red = (rgb>>16)&0x0ff;
     *  int green=(rgb>>8) &0x0ff;
     *  int blue= (rgb)    &0x0ff;
     */
    private fun readInCleanedImage(cleanFile: File): BufferedImage {
        val backgroundImage = ImageIO.read(cleanFile)
        // set the sand height to the image
        (0..<settings.tableDiameter).forEach { i ->
            (0..<settings.tableDiameter).forEach { j ->
                val color = Color(backgroundImage.getRGB(i, j))
                val newLevel = (color.red + color.green + color.blue).toDouble() / 3 / 30 // 30 seems to work...
                sandGrid[i][j] = newLevel
            }
        }
        return backgroundImage
    }


    /**
     * Encodes an 8-bit greyscale value into a 32-bit ARGB color value.
     * The alpha channel is set to full opacity (0xFF), and the same greyscale value is applied to the red, green, and blue channels.
     *
     * @param greyscale the 8-bit greyscale value to encode, expected to be in the range [0, 255].
     * @return the corresponding 32-bit ARGB color value.
     */
    private fun encode32bit(greyscale: Int): Int {
        val red: Int = (greyscale * RED_CONVERSION).toInt()
        val green: Int = (greyscale * GREEN_CONVERSION).toInt()
        val blue: Int = (greyscale * BLUE_CONVERSION).toInt()
        val resultRgb = when {
            settings.useGreyBackground -> Color(greyscale, greyscale, greyscale).rgb
            else                       -> Color(red, green, blue).rgb
        }
        return resultRgb
    }


    fun writeImage(file: File) {
        try { // save the image to disk
            ImageIO.write(bufferedImage, settings.ext, file)

        } catch (e: IOException) {
            println("Error saving file " + settings.outputFilename + ": " + e.message)
        }

    }
}