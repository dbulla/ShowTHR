package com.marginallyclever.showthr

import com.marginallyclever.showthr.Settings.Companion.centerX
import com.marginallyclever.showthr.Settings.Companion.centerY
import com.marginallyclever.showthr.Settings.Companion.maxRadius
import com.marginallyclever.showthr.Settings.Companion.width
import kotlin.math.cos
import kotlin.math.sin

class Utilities {
    companion object    {

        fun calculateY(theta: Double, rho: Double): Double {
            val newY = centerY - cos(theta) * rho * maxRadius
            return newY
        }

        fun calculateX(theta: Double, rho: Double): Double {
            val newX = centerX + sin(theta) * rho * maxRadius
            return newX
        }
    }
}