package com.marginallyclever.showthr

import java.awt.Toolkit

class Settings {
    val MAX_SLOPE = 1.0 // Threshold for sand redistribution
    val REDISTRIBUTION_RATE = 0.5 // Amount of sand transferred per step
    val RELAX_MARGIN = 4.0 // must be at greater than 1.
    val PROGRESS_THRESHOLD = 4.0
    var shouldExpandSequences = true
    val NUMBER_OF_TURNS_TO_CLEAN = 200


    var ballRadius: Double = 5.0
    var initialSandDepth: Double = 2.0
    var isGenerateCleanBackdrop = false
    var isReversed = false
    var shouldQuitWhenDone = false
    var backgroundImageName = ""
    var imageSkipCount = 4
    lateinit var inputFilename: String
    lateinit var outputFilename: String
    var height = Toolkit.getDefaultToolkit().screenSize.height
    var width = height // circular table
    lateinit var ext: String
}