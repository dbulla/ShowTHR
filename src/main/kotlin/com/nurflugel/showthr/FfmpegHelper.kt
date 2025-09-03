package com.nurflugel.showthr

import com.marginallyclever.showthr.ShowTHR.settings
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Locale
import kotlin.system.exitProcess
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class FfmpegHelper {

    @OptIn(ExperimentalTime::class)
    fun fireUpFFMPEG() {
        val imageDir = "animationImages"
        val mpgFilename = "ani_multi_${settings.tableDiameter}.mp4"
        checkForFFMPEG()
        val startTime = Clock.System.now()
        val mpgFile = File(mpgFilename)
        if (mpgFile.exists()) mpgFile.delete()
        val commandList = getCommandList(imageDir, mpgFilename)
        val process = ProcessBuilder(commandList).start() // grab the error stream and print that to stdout so we can see any errors
        val bufferedReader = BufferedReader(InputStreamReader(process.errorStream))
        var line: String? = bufferedReader.readLine()
        while (line != null) {
            println(line)
            line = bufferedReader.readLine()
        }
        val duration = Clock.System.now().minus(startTime)
        println("Done creating video, took $duration")
        ProcessBuilder(mutableListOf("open", mpgFilename)).start()
    }

    private fun checkForFFMPEG() {
        val process = ProcessBuilder(mutableListOf("ffmpeg", "-version")).start()
        val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String? = bufferedReader.readLine()
        while (line != null) {
            println(line)
            if (line.lowercase().contains("ffmpeg version")) {
                println("FFMPEG detected")
                return
            }
            line = bufferedReader.readLine()
        }
        println("FFMPEG NOT detected, exiting")
        exitProcess(0)
    }

    private val os: String = System.getProperty("os.name")
    private val isOsX: Boolean = System.getProperty("os.name").lowercase().startsWith("mac os")
    private val isWindows: Boolean = os.lowercase(Locale.getDefault()).startsWith("windows")


    private fun getCommandList(imageDir: String, mpgFilename: String): List<String> {

        //        val command="ffmpeg -f image2 -s 1500x1500 -i animationImages/image_%6d.png -vcodec libx264 -crf 25 -n -pix_fmt yuv420p  ani_1500_multi.mp4"
        //        if (isWindows) {
        //            val quote = if (isWindows) "\""
        //            else ""
        //            val dot = quote + dotExecutablePath + quote
        //            val output = " -o $quote$outputFilePath$quote"
        //            return mutableListOf(dot, "-T$outputFormat", "$quote$dotFilePath$quote$output")
        //        }
        //        else {

        return mutableListOf(
            "ffmpeg",
            "-f", "image2",
            "-s", "${settings.tableDiameter}x${settings.tableDiameter}",
            "-i", "$imageDir/image_%6d.png",
            "-vcodec", "libx264",
            "-crf", "25",
            "-n",
            "-pix_fmt", "yuv420p",
            mpgFilename
        )
        //        }
    }
}