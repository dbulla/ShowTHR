package com.marginallyclever.showthr

import java.awt.BorderLayout
import java.awt.Color.DARK_GRAY
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane

class ImageFrame(bufferedImage: BufferedImage, settings: Settings) : JFrame() {

    private val imagePanel = ImagePanel()

    init {
        layout = BorderLayout()

        val mainPanel = JPanel(GridBagLayout())
        mainPanel.background = DARK_GRAY

        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
//        gbc.weightx = 0.0 // Don't let it expand horizontally
//        gbc.weighty = 0.0 // Don't let it expand vertically
        gbc.anchor = GridBagConstraints.CENTER

        val scrollPane = JScrollPane(imagePanel)
        scrollPane.preferredSize = Dimension(settings.width, settings.height)

        mainPanel.add(imagePanel, gbc)
//                mainPanel.add(scrollPane, gbc) // todo - figure out why the image turns to a postage stamp when the frame is resized smaller
        add(mainPanel, BorderLayout.CENTER)
//        add(scrollPane, BorderLayout.CENTER)
        imagePanel.updateImage(bufferedImage)
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