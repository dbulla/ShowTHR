package com.nurflugel.showthr.utilities

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
        println("Renamer - $args")
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

        println("Renaming files in ${sourceDirectory.absolutePath} to ${targetDirectory.absolutePath}")

        // We want to sort the files based on the number embedded in the file name, so we can copy them in order.
        // Why not just let the OS sort it?  Because OS X does really wierd sorting on file names like this, and cannot
        // be trusted to do it right.  So, sort them ourselves.
        val sortedFiles = sourceDirectory
            .listFiles()!!
            .filter { it.name.startsWith("clock") }
            .sortedBy { // the file hame has a number in it (like 0.8609375), strip that out of the name and sort on that
                it.name.substringAfterLast("_")
                    .substringBefore(".png")
                    .toDouble()
            }
        // Now, go through the sorted list and copy the files to the new directory, renaming them as we go along.
        sortedFiles
            .forEachIndexed { index, sourceFile ->
                val paddedNumber = index.toString().padStart(6, '0')
                // figure out the new filename
                val newImageName = "images_${paddedNumber}.png"
                val renamedImageFile = File(targetDirectory, newImageName)

                when { // skip if the target file already exists
                    renamedImageFile.exists() -> println("Skipping $newImageName - file already exists")
                    else                      -> {
                        println("Copying/Renaming ${sourceFile.name} to $newImageName")
                        sourceFile.copyTo(renamedImageFile)
                    }
                }
            }
    }
}