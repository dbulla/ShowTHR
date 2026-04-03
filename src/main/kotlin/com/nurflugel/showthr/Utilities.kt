package com.nurflugel.showthr

import com.marginallyclever.showthr.Ball
import java.lang.Math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Utilities {
    companion object {

        fun calculateY(rhoTheta: RhoTheta, settings: Settings): Double {
            val centerY = settings.centerY
            val sin = sin(rhoTheta.theta)
            val normalizedRho = sin * rhoTheta.rho
            val deltaY = normalizedRho * settings.maxRadius
            val newY = centerY + deltaY
            return newY
        }

        fun calculateX(rhoTheta: RhoTheta, settings: Settings): Double {
            val centerX = settings.centerX
            val cos = cos(rhoTheta.theta)
            val newXoffset = cos * rhoTheta.rho * settings.maxRadius
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
        fun calculateRho(x: Double, y: Double, settings: Settings): Double {
            val actualX = x - settings.centerX
            val actualY = y - settings.centerY
            val rho = sqrt((actualX * actualX + actualY * actualY))
            //            val maxRadius = settings.maxRadius
            val maxRadius = settings.tableDiameter / 2
            val normalizedRho = rho / maxRadius // <== this is where the evil happens... should be a "1", got 0.48
            return normalizedRho
        }

        fun calculateTheta(x: Double, y: Double, settings: Settings): Double {
            val actualX = x - settings.centerX
            val actualY = y - settings.centerY
            val theta = atan2(actualY, actualX) * 180 / PI
            return theta
        }

        fun getBall2RhoTheta(rhoTheta: RhoTheta): RhoTheta {
            val rho = 1.0 - rhoTheta.rho
            val theta = rhoTheta.theta + PI
            return RhoTheta(rho, theta)
        }

        fun getArmLength(ball1: Ball, ball2: Ball): Double {
            val ball1Rho = ball1.getRho()
            val ball2Rho = ball2.getRho()
            val armLength = ball1Rho + ball2Rho
            println(" Ball1: rho=$ball1Rho, Ball2: rho=$ball2Rho   Total of $armLength ")
            return armLength
        }


    }
}