package com.marginallyclever.showthr

import java.awt.Dimension
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.swing.JPanel


class ImagePanel : JPanel() {
    private lateinit var image: BufferedImage

    fun updateImage(image: BufferedImage) {
        this.image = image
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g) // Call superclass method to clear the background
        g.drawImage(image, 0, 0, this) // Draw the image at (0,0)
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(image.width, image.height)
    }

}