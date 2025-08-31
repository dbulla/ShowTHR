package com.marginallyclever.showthr

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Utilities {
    companion object {

        fun calculateY(theta: Double, rho: Double, settings: Settings): Double {
            val newY = settings.centerY - cos(theta) * rho * settings.maxRadius
            // todo why no y offset?
            return newY
        }

        fun calculateX(theta: Double, rho: Double, settings: Settings): Double {
            val centerX = settings.centerX
            val sin = sin(theta)
            val newXoffset = sin * rho * settings.maxRadius
            val newX = centerX + newXoffset
            return newX
        }

        fun setValueFromArg(index: Int, args: Array<String>): String {
            if (index < args.size) {
                return args[index].trim { it <= ' ' }
            }
            else {
                println("Missing value for ${args[index - 1]}")
                throw IllegalArgumentException("Missing value for ${args[index - 1]}")
            }
        }

        // returns a normalized rho (0..1)
        fun calculateRho(x: Int, y: Int, settings: Settings): Double {
            val actualX=x-settings.centerX
            val actualY=y-settings.centerY
            val rho = sqrt((actualX * actualX + actualY * actualY) .toDouble())
            val normalizedRho = rho / settings.tableDiameter
            return normalizedRho
        }

        fun calculateTheta(x: Int, y: Int, settings: Settings): Double {
            val actualX=x-settings.centerX
            val actualY=y-settings.centerY
            val theta = atan2(actualY.toDouble(), actualX.toDouble()) * 180 / Math.PI
            return theta
        }

    }
}