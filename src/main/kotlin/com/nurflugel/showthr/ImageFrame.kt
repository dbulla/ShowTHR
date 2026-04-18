package com.nurflugel.showthr

import java.awt.BorderLayout
import java.awt.BorderLayout.CENTER
import java.awt.Color
import java.awt.Dimension
import java.awt.Point
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane


/** Frame that displays the image(s) */
class ImageFrame(bufferedImage: BufferedImage, settings: Settings) : JFrame() {

    private val imagePanel = ImagePanel()
    private var isPaused = true

    init {
        if (!settings.isHeadless) {
            layout = BorderLayout()

            val mainPanel = JPanel(BorderLayout())
            val backgroundColor = bufferedImage.getRGB(0, 0)
            mainPanel.background = Color(backgroundColor)

            val scrollPane = JScrollPane(imagePanel)
            mainPanel.add(scrollPane, CENTER)
            add(mainPanel, CENTER)
            imagePanel.updateImage(bufferedImage)
            imagePanel.preferredSize = Dimension(settings.baseTableDiameter, settings.baseTableDiameter)
            imagePanel.size = imagePanel.preferredSize
            defaultCloseOperation = DISPOSE_ON_CLOSE
//            defaultCloseOperation = EXIT_ON_CLOSE
//            defaultCloseOperation = HIDE_ON_CLOSE
//            defaultCloseOperation = DO_NOTHING_ON_CLOSE
            pack()

            if (settings.frameLocation == null) { // Never saved, or else they used the -center flag
                // center the frame on the screen
                setLocationRelativeTo(null)
            }
            else {
                // get location from the last use
                location = settings.frameLocation!!
            }

            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_SPACE) {
                        isPaused = false
                    }
                }
            })

            addComponentListener(object : ComponentAdapter() {
                override fun componentMoved(e: ComponentEvent?) {
                    val newLocation: Point? = location
                    settings.frameLocation = newLocation
                    settings.saveSettings()
                }
            })


            isVisible = true
            if (settings.waitForSpaceBar) {
                while (isPaused) {
                    Thread.sleep(100)
                }
            }
        }
    }

    fun updateImage(bufferedImage: BufferedImage) {
        imagePanel.updateImage(bufferedImage)
    }

}