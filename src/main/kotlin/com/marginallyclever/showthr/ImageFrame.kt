package com.marginallyclever.showthr

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane

class ImageFrame(bufferedImage: BufferedImage, settings: Settings) : JFrame() {

    private val imagePanel = ImagePanel()

    init {
        layout = BorderLayout()

        val mainPanel = JPanel(BorderLayout())
        val backgroundColor = bufferedImage.getRGB(0, 0)

        mainPanel.background = Color(backgroundColor)


        val scrollPane = JScrollPane(imagePanel)
        mainPanel.add(scrollPane, BorderLayout.CENTER)
        add(mainPanel, BorderLayout.CENTER)
        imagePanel.updateImage(bufferedImage)
        imagePanel.preferredSize = Dimension(settings.tableDiameter, settings.tableDiameter)
        imagePanel.size=imagePanel.preferredSize
        defaultCloseOperation = EXIT_ON_CLOSE
        pack()
        // center the frame on the screen
        setLocationRelativeTo(null)
        isVisible = true
    }

    fun updateImage(bufferedImage: BufferedImage) {
        imagePanel.updateImage(bufferedImage)
    }

}