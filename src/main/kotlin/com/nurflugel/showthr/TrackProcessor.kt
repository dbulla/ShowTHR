package com.nurflugel.showthr

import com.nurflugel.showthr.Settings.Companion.NUMBER_OF_TURNS_TO_CLEAN
import com.marginallyclever.showthr.ShowTHR.settings
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.system.exitProcess

/**
 * This class handles the task of parsing the .thr file and producing a list of pairs of theta and rho.
 */
class TrackProcessor() {

    /** Take the .thr file and parse it into a list of pairs of theta and rho */
    fun extractThetaRhoPairs(file: File): MutableList<ThetaRho> {
        val regex = "\\s+".toRegex()
        val trackLines: MutableList<String> = when {
            settings.isGenerateCleanBackdrop -> createCleaningTrack()
            else                             -> {
                BufferedReader(InputStreamReader(FileInputStream(file))).use { reader ->
                    val lineSequence = reader.lineSequence().toMutableList()
                    if (lineSequence.isEmpty()) exitProcess(0)
                    lineSequence
                }
            }
        }
        var sequence: List<ThetaRho> = parseSequence(trackLines, regex)
        if (settings.isReversed) sequence = sequence.reversed().toMutableList()
        val expandedSequence = expandSequence(sequence)
        println("initial size: ${sequence.size}, expandedSequence size = ${expandedSequence.size}")
        return expandedSequence
    }

    /**
     *  Go through the original file and clean it up, then produce a nice list, consisting of pairs of theta and rho.
     */
    fun parseSequence(lines: List<String>, regex: Regex): MutableList<ThetaRho> {
        val sequence: MutableList<ThetaRho> =

            lines.map { it.trim() }
                .filterNot { it.isEmpty() || it.startsWith("#") || it.startsWith("//") || it.startsWith("theta") }
                .map {
                    val parts = it.replace(regex, " ").split(" ")
                    try {
                        val theta = parts[0].toDouble()
                        val rho = parts[1].toDouble()
                        ThetaRho(theta, rho)
                    } catch (e: Exception) {
                        println("Error parsing sequence: ${e.message}: theta=${parts[0]}, rho=${parts[1]}")
                        throw e
                    }
                }
                .toMutableList()

        return sequence
    }


    // if desired, add a "clean" before the main track
    fun createCleaningTrack(): MutableList<String> {
        //        if (true) {
        //            val targetTheta = sequence[0].first
        //            val targetRho = sequence[0].second
        //            val initialRho = when (targetRho) {
        //                0.0  -> 1.0
        //                else -> 0.0
        //            }
        //            val initialTheta = targetTheta - 200.0 * PI
        //            val newSequence = mutableListOf<Pair<Double, Double>>()
        //            newSequence.add(Pair(initialTheta, initialRho))
        //            newSequence.addAll(sequence)
        //            return newSequence
        //        }
        //        else
        //            return sequence
        val cleaningTrack = mutableListOf<String>()
        cleaningTrack.add("0.0 0.0")
        cleaningTrack.add("${NUMBER_OF_TURNS_TO_CLEAN * PI} 1.0")
        cleaningTrack.add("${(NUMBER_OF_TURNS_TO_CLEAN + 2) * PI} 1.0") // get a nice clean edge

        return cleaningTrack
    }

    /**
     * The problem is that the app will draw straight lines in x,y space between two points - and when you only have a change in theta, it draws a straight line instead of
     * the curve that it should be.  So, for any case where theta changes but rho does not, we need to expand the sequence with many intermediate points to fake the curve.
     */
    fun expandSequence(sequence: List<ThetaRho>): MutableList<ThetaRho> {
        if (settings.shouldExpandSequences) {
            val newSequence = mutableListOf<ThetaRho>()
            for (i in 0..<sequence.size - 1) {
                val (theta1, rho1) = sequence[i]
                val (theta2, rho2) = sequence[i + 1]
                val deltaRho = rho2 - rho1
                val deltaTheta = theta2 - theta1
                //                val areBothRhosNotZero = rho1 != 0.0 || rho2 != 0.0 // this doesn't really save that much time unless we only have 1 ball
                //                if (settings.useTwoBalls || areBothRhosNotZero) { // if rhos are zero, skip expanding - unless we have two balls
                val maxRhoDiff = .01
                val maxThetaDiff = .05
                if ((abs(deltaRho) > maxRhoDiff || abs(deltaTheta) > maxThetaDiff) || (rho1 < .0001 && rho2 < .0001)) {
                    val thetaDiff = theta2 - theta1
                    val rhoDiff = rho2 - rho1
                    val numPointsTheta = max(1, abs(thetaDiff / maxThetaDiff).toInt())
                    val numPointsRho = max(1, abs(rhoDiff / maxRhoDiff).toInt())
                    // which is greater?  Use that to subdivide the segment.
                    val numPoints=max(numPointsRho,numPointsTheta)
                    if (numPoints == 1) { // special case to prevent division by zero below
                        newSequence.add(ThetaRho(theta1, rho1))
                    }
                    else {
                        val deltaRho = rhoDiff / (numPoints - 1)
                        val deltaTheta = thetaDiff / (numPoints - 1)

                        (0..<numPoints).forEach { j ->
                            val newTheta = theta1 + deltaTheta * j
                            val newRho = rho1 + deltaRho * j
                            newSequence.add(ThetaRho(newTheta, newRho))
                        }
                    }
                }
                //                }
                else newSequence.add(ThetaRho(theta1, rho1))
            }
            if (sequence.isNotEmpty()) newSequence.add(sequence.last())
            return newSequence
        }
        else return sequence.toMutableList()
    }


}