/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.file;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageFile {

    private final BufferedImage image;

    public int width;
    public int height;
    private final boolean hasAlphaChannel;
    private int pixelLength;
    private final byte[] pixels;

    public ImageFile(String path) throws IOException {
        var source = this.getClass().getResourceAsStream(path);
        if (source == null) {
            throw new FileNotFoundException("Image " + path + " not found.");
        }

        image = ImageIO.read(source);

        pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        width = image.getWidth();
        height = image.getHeight();
        hasAlphaChannel = image.getAlphaRaster() != null;
        pixelLength = 3;
        if (hasAlphaChannel) {
            pixelLength = 4;
        }
    }

    public BufferedImage getImage() {
        return image;
    }

    public short[] getRGB(int x, int y) {
        int pos = (y * pixelLength * width) + (x * pixelLength);
        short[] rgb = new short[4];

        if (hasAlphaChannel) {
            rgb[3] = (short) (pixels[pos++] & 0xFF); // Alpha
        }

        rgb[2] = (short) (pixels[pos++] & 0xFF); // Blue
        rgb[1] = (short) (pixels[pos++] & 0xFF); // Green
        rgb[0] = (short) (pixels[pos++] & 0xFF); // Red

        return rgb;
    }
}
