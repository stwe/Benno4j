/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.file;

import de.sg.benno.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.util.Objects;

// todo

public class ImageFile {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    private final BufferedImage image;
    private final byte[] pixels;
    private int width;
    private int height;
    private final boolean hasAlphaChannel;
    private int pixelLength;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link ImageFile} object.
     *
     * @param path The file path to the image.
     *
     * @throws IOException If an I/O error is thrown.
     */
    public ImageFile(String path) throws IOException {
        var source = Util.getFileFromResourceAsStream(path);
        image = ImageIO.read(Objects.requireNonNull(source, "source must not be null"));

        pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        width = image.getWidth();
        height = image.getHeight();
        hasAlphaChannel = image.getAlphaRaster() != null;
        pixelLength = 3;
        if (hasAlphaChannel) {
            pixelLength = 4;
        }
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #image}.
     *
     * @return {@link #image}
     */
    public BufferedImage getImage() {
        return image;
    }

    public int[] getArgb() {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
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
