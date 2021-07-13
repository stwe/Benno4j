/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.file;

import de.sg.benno.BennoRuntimeException;
import de.sg.benno.Util;
import de.sg.benno.chunk.Chunk;
import de.sg.ogl.resource.Texture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;

/**
 * Represents a BshFile.
 * The class loads a Bsh file.
 */
public class BshFile extends BinaryFile {

    /**
     * Represents the type of an Bsh image.
     */
    private enum BshType {
        NORMAL(1),
        NEW(13);

        public final int value;

        BshType(int value) {
            this.value = value;
        }

        public static BshType fromInt(int value) {
            switch (value) {
                case 1 : return NORMAL;
                case 13 : return NEW;
            }

            return null; // todo
        }
    }

    /**
     * A {@link BufferedImage} with additional information.
     */
    private static class BufferedBshImage {
        final BufferedImage image;
        final BshType type;
        final int offset;

        /**
         * Constructs a new {@link BufferedBshImage} object.
         *
         * @param image A {@link BufferedImage}
         * @param type A {@link BshType}
         * @param offset The offset of the Bsh image.
         */
        BufferedBshImage(BufferedImage image, BshType type, int offset) {
            this.image = image;
            this.type = type;
            this.offset = offset;
        }
    }

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * The number of expected {@link de.sg.benno.chunk.Chunk} objects in the file.
     */
    private static final int NUMBER_OF_CHUNKS = 1;

    /**
     * The Id of the {@link de.sg.benno.chunk.Chunk}.
     */
    private static final String CHUNK_ID = "BSH";

    /**
     * If the next byte is 0xFF the image has reached it’s end.
     */
    private static final int END_MARKER = 255;

    /**
     * If the next byte is 0xFE the current pixel line has reached it’s end.
     */
    private static final int END_OF_ROW = 254;

    /**
     * The directory in which the Png files are saved.
     */
    private static final String OUTPUT_DIR = "out";

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The color values from the <i>stadtfld.col</i> file.
     * These are loaded with {@link PaletteFile}.
     */
    private final int[] palette;

    /**
     * If the variable is true, all images are saved as a Png in {@link #OUTPUT_DIR}.
     */
    private final boolean saveAsPng;

    /**
     * The offsets to the Bsh images.
     */
    private final ArrayList<Integer> offsets = new ArrayList<>();

    /**
     * The possible invalid offsets to the Bsh images.
     */
    private final HashSet<Integer> possibleInvalidOffsets = new HashSet<>();

    /**
     * A list with {@link BshTexture} objects.
     */
    private final ArrayList<BshTexture> bshTextures = new ArrayList<>();

    /**
     * The texture min width in {@link #bshTextures}.
     */
    private int maxX = -999;

    /**
     * The texture min height in {@link #bshTextures}.
     */
    private int maxY = -999;

    /**
     * Shortcut to the {@link Chunk}.
     */
    private final Chunk chunk0;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link BshFile} object.
     *
     * @param path The {@link Path} to the Bsh file.
     * @param palette The color values from the <i>stadtfld.col</i> file.
     * @param saveAsPng If the variable is true, all images are saved as a Png in {@link #OUTPUT_DIR}.
     * @throws IOException If an I/O error is thrown.
     */
    BshFile(Path path, int[] palette, boolean saveAsPng) throws IOException {
        super(Objects.requireNonNull(path, "path must not be null"));

        LOGGER.debug("Creates BshFile object from file {}.", path);

        this.palette = Objects.requireNonNull(palette, "palette must not be null");
        this.saveAsPng = saveAsPng;

        if (getNumberOfChunks() != NUMBER_OF_CHUNKS) {
            throw new BennoRuntimeException("Invalid number of Chunks.");
        }

        if (!chunkIndexHasId(0, CHUNK_ID)) {
            throw new BennoRuntimeException("Invalid Chunk Id.");
        }

        chunk0 = getChunk(0);

        readDataFromChunks();
    }

    /**
     * Constructs a new {@link BshFile} object.
     *
     * @param path The {@link Path} to the Bsh file.
     * @param palette The color values from the <i>stadtfld.col</i> file.
     * @throws IOException If an I/O error is thrown.
     */
    BshFile(Path path, int[] palette) throws IOException {
        this(path, palette, false);
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #bshTextures}.
     *
     * @return {@link #bshTextures}
     */
    public ArrayList<BshTexture> getBshTextures() {
        return bshTextures;
    }

    /**
     * Get {@link #maxX}.
     *
     * @return {@link #maxX}
     */
    public int getMaxX() {
        return maxX;
    }

    /**
     * Get {@link #maxY}.
     *
     * @return {@link #maxY}
     */
    public int getMaxY() {
        return maxY;
    }

    //-------------------------------------------------
    // BinaryFileInterface
    //-------------------------------------------------

    @Override
    public void readDataFromChunks() throws IOException {
        LOGGER.debug("Start reading BSH data from Chunks...");

        readOffsets();
        validateOffsets();
        decodeTextures();
        createGlTextures();
        setMaxValues();

        LOGGER.debug("BSH data read successfully.");
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up {@link #bshTextures}.
     */
    public void cleanUp() {
        LOGGER.debug("Clean up {} OpenGL textures.", bshTextures.size());

        for (var bshTexture : bshTextures) {
            bshTexture.getTexture().cleanUp();
        }
    }

    //-------------------------------------------------
    // Offsets
    //-------------------------------------------------

    /**
     * Reads and saves all offsets of the Bsh images.
     */
    private void readOffsets() {
        // get offset of the first texture
        var texturesStartOffset = chunk0.getData().getInt();
        offsets.add(texturesStartOffset);

        // add remaining offsets
        for (var offset = chunk0.getData().getInt(); chunk0.getData().position() <= texturesStartOffset; offset = chunk0.getData().getInt()) {
            offsets.add(offset);
        }

        LOGGER.debug("Detected {} texture offsets.", offsets.size());
    }

    /**
     * Stores presumably invalid offsets.
     * The offset is probably invalid if the difference between two offsets is 20 bytes.
     */
    private void validateOffsets() {
        for (int i = 0; i < offsets.size(); i++) {
            if (i + 1 < offsets.size()) {
                var offset = offsets.get(i);
                var nextOffset = offsets.get(i + 1);
                if (nextOffset - offset == 20) {
                    possibleInvalidOffsets.add(offset);
                }
            }
        }

        if (!possibleInvalidOffsets.isEmpty()) {
            LOGGER.warn("Detected {} possible invalid texture offsets.", possibleInvalidOffsets.size());
        }
    }

    //-------------------------------------------------
    // Texture
    //-------------------------------------------------

    /**
     * Reads the pixel data from the {@link #chunk0} and uses it to create {@link BshTexture} objects.
     * The {@link BshTexture} objects are stored in {@link #bshTextures}.
     *
     * @throws IOException If an I/O error is thrown.
     */
    private void decodeTextures() throws IOException {
        for (var offset : offsets) {
            if (!possibleInvalidOffsets.contains(offset)) {
                chunk0.getData().position(offset);

                var textureHeader = readTextureHeader(offset);
                if (textureHeader.type == BshType.NEW) {
                    decodeTexture13(textureHeader);
                } else {
                    decodeTexture(textureHeader);
                }
            }
        }

        LOGGER.debug("A total of {} bsh textures were created.", bshTextures.size());
    }

    /**
     * Reads the pixel data from the {@link #chunk0} and uses it to create a {@link BshTexture} object.
     *
     * @param textureHeader {@link BufferedBshImage}
     * @throws IOException If an I/O error is thrown.
     */
    private void decodeTexture13(BufferedBshImage textureHeader) throws IOException {
        int x = 0;
        int y = 0;

        while (true) {
            int numAlpha = chunk0.getData().getInt();

            if (numAlpha == END_MARKER) {
                break;
            }

            if (numAlpha == END_OF_ROW) {
                x = 0;
                y++;
                continue;
            }

            for (int i = 0; i < numAlpha; i++) {
                textureHeader.image.setRGB(x, y, 0);
                x++;
            }

            int numPixels = chunk0.getData().getInt();
            for (int i = 0; i < numPixels; i++) {
                var b = chunk0.getData().get();
                var g = chunk0.getData().get();
                var r = chunk0.getData().get();
                var a = chunk0.getData().get();

                var color = Util.rgbToInt(Util.byteToInt(r), Util.byteToInt(g), Util.byteToInt(b));

                textureHeader.image.setRGB(x, y, color);
                x++;
            }
        }

        if (saveAsPng) {
            saveAsPng(textureHeader.image, textureHeader.offset);
        }

        var bshTexture = new BshTexture(textureHeader.image);
        bshTextures.add(bshTexture);
    }

    /**
     * Reads the pixel data from the {@link #chunk0} and uses it to create a {@link BshTexture} object.
     *
     * @param textureHeader {@link BufferedBshImage}
     * @throws IOException If an I/O error is thrown.
     */
    private void decodeTexture(BufferedBshImage textureHeader) throws IOException {
        int x = 0;
        int y = 0;

        while (true) {
            int numAlpha = Util.byteToInt(chunk0.getData().get());

            // if the next byte is 0xFF the image has reached it’s end
            if (numAlpha == END_MARKER) {
                break;
            }

            // if the next byte is 0xFE the current pixel line has reached it’s end
            if (numAlpha == END_OF_ROW) {
                x = 0;
                y++;
                continue;
            }

            // ... else it’s an Texture Chunk
            // number of pixels to skip / number of transparent pixels
            for (int i = 0; i < numAlpha; i++) {
                textureHeader.image.setRGB(x, y, 0);
                x++;
            }

            // number of pixels that follow
            int numPixels = Util.byteToInt(chunk0.getData().get());
            for (int i = 0; i < numPixels; i++) {
                // pixels to insert at current position
                var colorIndex = Util.byteToInt(chunk0.getData().get());
                var color = palette[colorIndex];
                textureHeader.image.setRGB(x, y, color);
                x++;
            }
        }

        if (saveAsPng) {
            saveAsPng(textureHeader.image, textureHeader.offset);
        }

        var bshTexture = new BshTexture(textureHeader.image);
        bshTextures.add(bshTexture);
    }

    /**
     * Makes the {@link BshTexture} objects available for OpenGL.
     */
    private void createGlTextures() {
        for (var bshTexture : bshTextures) {
            var dbb = (DataBufferInt) bshTexture.getBufferedImage().getRaster().getDataBuffer();

            var texture = new Texture();
            Texture.bind(texture.getId());
            Texture.useNoFilter();

            bshTexture.setTexture(texture);
            bshTexture.getTexture().setWidth(bshTexture.getBufferedImage().getWidth());
            bshTexture.getTexture().setHeight(bshTexture.getBufferedImage().getHeight());

            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA8,
                    bshTexture.getBufferedImage().getWidth(),
                    bshTexture.getBufferedImage().getHeight(),
                    0,
                    GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV,
                    dbb.getData()
            );

            Texture.unbind();
        }
    }

    /**
     * Reads width, height, type and length of a Bsh image from {@link #chunk0}.
     * A {@link BufferedBshImage} object is created from this.
     *
     * @param offset The offset in {@link #chunk0}.
     *
     * @return {@link BufferedBshImage}
     */
    private BufferedBshImage readTextureHeader(int offset) {
        var width = chunk0.getData().getInt();
        var height = chunk0.getData().getInt();

        if (width <= 0 || height <= 0) {
            throw new BennoRuntimeException("Invalid width or height.");
        }

        var type = chunk0.getData().getInt();
        var length = chunk0.getData().getInt();

        return new BufferedBshImage(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), BshType.fromInt(type), offset);
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Saves a {@link BufferedImage} as a Png.
     *
     * @param image {@link BufferedImage}
     * @param offset The offset in {@link #chunk0} used as file name.
     * @throws IOException If an I/O error is thrown.
     */
    private void saveAsPng(BufferedImage image, int offset) throws IOException {
        Files.createDirectories(Paths.get(OUTPUT_DIR));

        var bshFilename = getPath().getFileName().toString().toLowerCase();

        String filename = OUTPUT_DIR + "/" +
                bshFilename.substring(0, bshFilename.lastIndexOf(".")) + "_" +
                offset + ".png";

        File file = new File(filename);
        if (!file.exists()) {
            var result = file.createNewFile();
            if (!result) {
                throw new BennoRuntimeException("Unexpected error.");
            }
        }

        ImageIO.write(image, "PNG", file);
    }

    /**
     * Determines the maximum width and height of the images.
     */
    private void setMaxValues() {
        for (var bshTexture : bshTextures) {
            if (bshTexture.getBufferedImage().getWidth() > maxX) {
                maxX = bshTexture.getBufferedImage().getWidth();
            }

            if (bshTexture.getBufferedImage().getHeight() > maxY) {
                maxY = bshTexture.getBufferedImage().getHeight();
            }
        }
    }
}
