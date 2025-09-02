package com.nurflugel.showthr

/** Wrapper for the number of frames */
class Counter {
    var count = 1

    fun increment(): Int {
        return count++
    }
}