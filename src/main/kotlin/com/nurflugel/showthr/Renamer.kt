package com.nurflugel.showthr

import com.nurflugel.showthr.Utilities.Companion.setValueFromArg
import java.io.File

/** This class will rename all the files in a directory to have a number prefix.
 *
 * Any file that starts with "clock" will be included.  Anything else will be skipped.  The index of the "clock*" file
 * in the sorted list of "clock*" files, and the images are copied to a new directory as "images_NNNN.png".
 *
 * If the target image already exists, it is skipped.
 */
object Renamer {
    @JvmStatic
    fun main(args: Array<String>) {

        lateinit var sourceDirectory: File
        lateinit var targetDirectory: File

        if (args.isEmpty()) {
            println("Problem parsing arguments ")
            return
        }
        try {
            var index = 0
            while (index < args.size) {
                when (args[index]) {
                    "-sourceDir" -> sourceDirectory = File(setValueFromArg(++index, args))
                    "-targetDir" -> targetDirectory = File(setValueFromArg(++index, args))
                    else         -> {
                        println("Unknown option " + args[index])
                        return
                    }
                }
                index++
            }
        } catch (e: Exception) {
            println("Problem parsing arguments ${e.message}")
            return
        }

        sourceDirectory.listFiles()!!
            .filter { it.name.startsWith("clock") }
            .sortedBy { it.name }
            .mapIndexed { index, s -> index to s } // make a list of pairs, consisting of the index number and the file
            .forEach {
                val paddedNumber = it.first.toString().padStart(6, '0')
                // figure out the new filename
                val newImageName = "images_${paddedNumber}.png"
                val renamedImageFile = File(targetDirectory, newImageName)

                when { // skip if the target file already exists
                    renamedImageFile.exists() -> println("Skipping $newImageName - file already exists")
                    else                      -> {
                        println("Renaming ${it.second} to $newImageName")
                        it.second.copyTo(renamedImageFile)
                    }
                }
            }
    }
}