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

/**
 * Represents an ImageFile.
 */
public class ImageFile {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * A {@link BufferedImage}.
     */
    private final BufferedImage image;

    /**
     * The pixels array.
     */
    private final byte[] pixels;

    /**
     * The width of the image.
     */
    private final int width;

    /**
     * The height of the image.
     */
    private final int height;

    /**
     * True if the image has an alpha channel.
     */
    private final boolean hasAlphaChannel;

    /**
     * The number of pixel channels.
     */
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

    //-------------------------------------------------
    // Pixel
    //-------------------------------------------------

    /**
     * Returns an array of pixels.
     *
     * @return int[]
     */
    public int[] getArgb() {
        return image.getRGB(0, 0, width, height, null, 0, width);
    }

    /**
     * A fast getRGB implementation.
     *
     * @param x The x position.
     * @param y The y position.
     *
     * @return short[]
     */
    public short[] getFastRGB(int x, int y) {
        var pos = (y * pixelLength * width) + (x * pixelLength);
        var rgb = new short[4];

        if (hasAlphaChannel) {
            rgb[3] = (short) (pixels[pos++] & 0xFF); // Alpha
        }

        rgb[2] = (short) (pixels[pos++] & 0xFF); // Blue
        rgb[1] = (short) (pixels[pos++] & 0xFF); // Green
        rgb[0] = (short) (pixels[pos] & 0xFF); // Red

        return rgb;
    }
}
