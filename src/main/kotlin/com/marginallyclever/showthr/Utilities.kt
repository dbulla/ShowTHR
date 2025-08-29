package com.marginallyclever.showthr

import kotlin.math.cos
import kotlin.math.sin

class Utilities {
    companion object {

        fun calculateY(theta: Double, rho: Double, settings: Settings): Double {
            val newY = settings.centerY - cos(theta) * rho * settings.maxRadius
            return newY
        }

        fun calculateX(theta: Double, rho: Double, settings: Settings): Double {
            val newX = settings.centerX + sin(theta) * rho * settings.maxRadius
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

    }
}