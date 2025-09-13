package com.nurflugel.showthr

/**
 * Class to represent a point in polar coordinates.
 *
 * For simplicity, rho is always in the Sisyphus range of -1 to +1 (hence the "normalized").  0 is at the center.  Negative values are used for the second ball if specified.
 */
data class NormalizedThetaRho(var theta: Double, var rho: Double)
