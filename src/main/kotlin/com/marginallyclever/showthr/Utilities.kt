package com.marginallyclever.showthr

import java.lang.Math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Utilities {
    companion object {

        //        fun calculateY(theta: Double, rho: Double, settings: Settings): Double {
        //            val centerY = settings.centerY
        //            val cos = cos(theta)
        //            val newYOffset = cos * rho * settings.maxRadius
        //            val newY = centerY + newYOffset
        //            return newY
        //        }
        fun calculateY(thetaRho: ThetaRho, settings: Settings): Double {
            val centerY = settings.centerY
            val cos = cos(thetaRho.theta)
            val newYOffset = cos * thetaRho.rho * settings.maxRadius
            val newY = centerY + newYOffset
            return newY
        }

        //        fun calculateX(theta: Double, rho: Double, settings: Settings): Double {
        //            val centerX = settings.centerX
        //            val sin = sin(theta)
        //            val newXOffset = sin * rho * settings.maxRadius
        //            val newX = centerX + newXOffset
        //            return newX
        //        }
        fun calculateX(thetaRho: ThetaRho, settings: Settings): Double {
            val centerX = settings.centerX
            val sin = sin(thetaRho.theta)
            val newXOffset = sin * thetaRho.rho * settings.maxRadius
            val newX = centerX + newXOffset
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
            val actualX = x - settings.centerX
            val actualY = y - settings.centerY
            val rho = sqrt((actualX * actualX + actualY * actualY).toDouble())
            val normalizedRho = rho / settings.tableDiameter
            return normalizedRho
        }

        fun calculateTheta(x: Int, y: Int, settings: Settings): Double {
            val actualX = x - settings.centerX
            val actualY = y - settings.centerY
            val theta = atan2(actualY.toDouble(), actualX.toDouble()) * 180 / Math.PI
            return theta
        }

        fun getBall2Rho(rho: Double): Double = 1.0 - rho
        fun getBall2Theta(theta: Double): Double = theta + PI
        fun getBall2ThetaRho(thetaRho: ThetaRho): ThetaRho {
            val newTheta = getBall2Theta(thetaRho.theta)
            val newRho = getBall2Rho(thetaRho.rho)
            return ThetaRho(newTheta, newRho)
        }

        /** Calculates the distance between two points in theta, rho coordinate space. */
        fun calculateDistanceRhoTheta(position1: ThetaRho, position2: ThetaRho): Double {
            //            d = √(r₁² + r₂² - 2r₁r₂ cos(θ₂ - θ₁))
            val distance = sqrt((position1.rho * position1.rho + position2.rho * position2.rho - 2 * position1.rho * position2.rho * cos(position2.theta - position1.theta)))
            return distance
        }
    }
}