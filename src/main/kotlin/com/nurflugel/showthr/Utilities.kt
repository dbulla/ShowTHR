package com.nurflugel.showthr

import java.lang.Math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Utilities {
    companion object {

        /** X and Y are now zeroed in the center of the table */
        fun calculateY(rhoTheta: RhoTheta, settings: Settings): Double {
            val sin = sin(rhoTheta.theta)
            val normalizedRho = sin * rhoTheta.rho
            val deltaY = normalizedRho * settings.tableRadius
            return deltaY
        }

        /** X and Y are now zeroed in the center of the table */
        fun calculateX(rhoTheta: RhoTheta, settings: Settings): Double {
            val cos = cos(rhoTheta.theta)
            val newXoffset = cos * rhoTheta.rho * settings.tableRadius
            return newXoffset
        }

        fun getValueFromArg(index: Int, args: Array<String>): String {
            if (index < args.size) {
                return args[index].trim { it <= ' ' }
            }
            else {
                println("Missing value for ${args[index - 1]}")
                throw IllegalArgumentException("Missing value for ${args[index - 1]}")
            }
        }

        /**
         * rho is now zeroed in the center of the table.
         *
         * @param settings - passed in here so we can do unit tests.
         */
        fun calculateRho(x: Double, y: Double, settings: Settings): Double {
            val rho = sqrt((x * x + y * y))
            val normalizedRho = rho / settings.tableRadius
            return normalizedRho
        }

        fun calculateThetaInDegrees(x: Double, y: Double): Double {
            val theta = calculateTheta(x, y) * 180 / PI
            return theta
        }

        fun calculateTheta(x: Double, y: Double): Double {
            val theta = atan2(y, x)
            return theta
        }

        fun getBall2RhoTheta(rhoTheta: RhoTheta): RhoTheta {
            val rho = 1.0 - rhoTheta.rho
            val theta = rhoTheta.theta + PI
            return RhoTheta(rho, theta)
        }

        /**
         * Take an X, Y grid with 0,0 in the center of the grid, and transfer into a coordinate system where 0,0 is in the upper-left corner, so the image can work
         * Since the table is round, the translation works for both x or y.
         */
        fun calculateCornerXY(centerXy: Double, settings: Settings): Int {
            return centerXy.toInt() + settings.tableRadius + settings.SHOULDER_WIDTH
        }

    }
}