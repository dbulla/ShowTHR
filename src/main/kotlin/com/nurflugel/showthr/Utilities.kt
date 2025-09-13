package com.nurflugel.showthr

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Utilities {
    companion object {

        /** Returns the Y value in sand coordinates for a normalized rho */
        fun calculateSandY(normalizedThetaRho: NormalizedThetaRho, settings: Settings): Double {
            val cos = cos(normalizedThetaRho.theta)
            val newYOffset = cos * normalizedThetaRho.rho * settings.maxRadius
            val newY = settings.centerY + newYOffset
            return newY
        }

        /** Returns the X value in sand coordinates for a normalized rho */
        fun calculateSandX(normalizedThetaRho: NormalizedThetaRho, settings: Settings): Double {
            val sin = sin(normalizedThetaRho.theta)
            val newXOffset = sin * normalizedThetaRho.rho * settings.maxRadius
            val newX = settings.centerX + newXOffset
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

//        /**
//         *  From https://en.wikipedia.org/wiki/Polar_coordinate_system
//         *
//         * Note that this gives straight magnitude - rho is in table sand pixels, NOT the 0-1 rho that describes
//         */
//        fun calculateThetaWithSandRho(x: Double, y: Double, settings: Settings): SandThetaRho {
//
//            val cos0 = cos(0.0)
//            val cosPi2 = cos(PI / 2)
//            val cosPi = cos(PI)
//            val cos32Pi = cos(3 * PI / 2)
//            val cos2Pi = cos(2 * PI)
//            val aa = acos(cos0)
//            val ab = acos(cosPi2)
//            val ac = acos(cosPi)
//            val ad = acos(cos32Pi)
//            val ae = acos(cosPi)
//
//
//            val polarX = x - settings.centerX
//            val polarY = y - settings.centerY
//
//            val rho = sqrt((polarX * polarX + polarY * polarY))
//            var theta = calculateThetaFromPolarXY(polarX, polarY, rho)
//
//            return SandThetaRho(theta, rho)
//        }

//        fun calculateThetaFromPolarXY(polarX: Double, polarY: Double, rho: Double): Double {
//            var theta = when {
//                polarY >= 0 && rho > 0 -> acos(polarX / rho) // range of +/- 0-PI
//                polarY < 0 && rho > 0  -> -acos(polarX / rho)
//                rho == 0.0             -> 0.0 // theta is undefined for rho=0
//                else                   -> 0.0
//            }
//            //            if (polarX < 0) theta += PI  // total hack!!!
//            if (theta < 0) theta += 2 * PI // Trying to avoid negative thetas
//            //            val theta = when {
//            //                actualX > 0 && actualY > 0 -> atan(actualY / actualX) // Quadrant I
//            //                actualX < 0 && actualY > 0 -> atan(actualY / actualX) + PI // Quadrant II
//            //                actualX < 0 && actualY < 0 -> atan(actualY / actualX) + PI // Quadrant III
//            //                actualX > 0 && actualY < 0 -> atan(actualY / actualX) + 2 * PI // Quadrant IV
//            //                else                        -> 0.0
//            //            }
//            return theta
//        }
//
//        fun calculateAdjustedRho(sandThetaRho: SandThetaRho, settings: Settings): Double {
//            val newRho = sandThetaRho.rho * settings.maxRadius / settings.tableRadius
//            return newRho
//        }
//
//        /** Takes the table sand pixel rho and converts it to a 0-1 rho value, leaving the theta value alone.
//         *
//         * Note that the sand rho is "adjusted" - i.e., it never exceeds maxRadius instead of tableRadius.
//         * */
//        fun calculateNormalizedThetaRho(sandThetaRho: SandThetaRho, settings: Settings): NormalizedThetaRho {
//            val newRho = sandThetaRho.rho / settings.maxRadius
//            return NormalizedThetaRho(sandThetaRho.theta, newRho)
//        }
//
//        /** Takes the 0-1 rho and converts it to a table pixel rho value, leaving the theta value alone. */
//        fun calculateMaximalizedThetaRho(normalizedThetaRho: NormalizedThetaRho, settings: Settings): NormalizedThetaRho {
//            // this can be used to convert the "normal" 0-1 rho into something that won't exceed the max radius
//            val normalizingFactor = settings.maxRadius / (settings.tableDiameter / 2.0)
//            val newRho = normalizedThetaRho.rho * normalizingFactor // now will never exceed max radius
//            return NormalizedThetaRho(normalizedThetaRho.theta, newRho)
//        }

        fun getBall2Rho(rho: Double): Double = rho - 1.0
        fun getBall2Theta(theta: Double): Double = theta

        //        fun getBall2Rho(rho: Double): Double = 1.0 - rho
        //        fun getBall2Theta(theta: Double): Double = theta + PI
        fun getBall2ThetaRho(normalizedThetaRho: NormalizedThetaRho): NormalizedThetaRho {
            val newTheta = getBall2Theta(normalizedThetaRho.theta)
            val newRho = getBall2Rho(normalizedThetaRho.rho)
            val ball2ThetaRho = NormalizedThetaRho(newTheta, newRho)
//            println("oldThetRhoa=$normalizedThetaRho, newThetaRho=$ball2ThetaRho")
            return ball2ThetaRho
        }

        /**
         * Calculates the distance between two points in theta, rho coordinate space.
         *   d = √(r₁² + r₂² - 2r₁r₂ cos(θ₂ - θ₁))
         */
        fun calculateDistanceRhoTheta(position1: NormalizedThetaRho, position2: NormalizedThetaRho): Double {
            val distance = sqrt(
                position1.rho * position1.rho
                + position2.rho * position2.rho
                - 2 * position1.rho * position2.rho * cos(position2.theta - position1.theta)
            )
            return distance
        }
    }
}