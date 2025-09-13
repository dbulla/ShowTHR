package com.nurflugel.showthr

/** Wrapper for the number of frames - note that it defaults to 1 if not set */
class Counter(var count: Int = 1) {
    //    var count = 1

    fun increment(): Int {
        return count++
    }

    override fun toString(): String {
        return count.toString()
    }
}