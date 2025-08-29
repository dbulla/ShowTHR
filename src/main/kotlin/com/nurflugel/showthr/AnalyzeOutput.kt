package com.nurflugel.showthr

import org.apache.commons.io.FileUtils
import java.io.File

/**
 * Read in the "output.txt" file and find any times the internet wasn't available.
 *
 * rsync -a douglasbullard@192.168.1.46:/home/douglasbullard/pingStuff/ .
 */
object AnalyzeOutput {
    @JvmStatic
    fun main(args: Array<String>) {
        val dirName=args[0]
        val lines = FileUtils.readLines(File("$dirName/output.txt"), Charsets.UTF_8)

        for ((index, line) in lines.withIndex()) {
            if(index==0) println("100% packet loss at: ")
            if (line.contains("100% packet loss")) {
                var text = lines[index - 2]
                text=text.substringBefore(".")
                println("    $text")
            }
        }
    }
}