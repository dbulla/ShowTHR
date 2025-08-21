package com.nurflugel.showthr

import java.io.File

object Renamer {
    @JvmStatic
    fun main(args: Array<String>) {
        val dir = File("/Users/douglasbullard/dev/github/douglasBullard/sisyphus-table-pattern-maker/images4")

        val files = dir.listFiles()!!
        files
            .filter { it.name.startsWith("clock") }
            .sortedBy{ it.name}
            .mapIndexed { index, s -> index to s }
            .forEach { it  ->
                val paddedNumber = it.first.toString().padStart(4, '0')
                println("renaming ${it.second} to images_${paddedNumber}.png")
                it.second.renameTo(File(dir, "images_${paddedNumber}.png"))
            }
    }
}