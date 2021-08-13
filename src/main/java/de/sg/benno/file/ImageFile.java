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

import static de.sg.ogl.Log.LOGGER;

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
     * @param image A {@link BufferedImage}
     */
    public ImageFile(BufferedImage image) {
        LOGGER.debug("Creates ImageFile object.");

        this.image = Objects.requireNonNull(image, "image must not be null");

        pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        width = image.getWidth();
        height = image.getHeight();
        hasAlphaChannel = image.getAlphaRaster() != null;
        pixelLength = 3;
        if (hasAlphaChannel) {
            pixelLength = 4;
        }
    }

    /**
     * Constructs a new {@link ImageFile} object.
     *
     * @param path The file path to the image.
     *
     * @throws IOException If an I/O error is thrown.
     */
    public ImageFile(String path) throws IOException {
        this(ImageIO.read(
                Objects.requireNonNull(Util.getFileFromResourceAsStream(path), "source must not be null"))
        );
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

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Resizing an {@link BufferedImage}.
     *
     * @param originalImage The original {@link BufferedImage}.
     * @param targetWidth The target width.
     * @param targetHeight The target height.
     *
     * @return The resized {@link BufferedImage}
     */
    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        var resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
        var graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();

        return resizedImage;
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the ImageFile.");

        image.getGraphics().dispose();
    }
}
