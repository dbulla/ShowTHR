package com.nurflugel.showthr

/**
 * Class to represent a point in polar coordinates.
 *
 * For simplicity, rho is always in the Sisyphus range of 0-1 (hence the "normalized")
  */
data class NormalizedThetaRho(var theta: Double, var rho: Double)
