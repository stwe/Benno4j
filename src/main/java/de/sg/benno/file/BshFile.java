/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021, stwe <https://github.com/stwe/Benno4j>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.sg.benno.file;

import de.sg.benno.BennoConfig;
import de.sg.benno.BennoRuntimeException;
import de.sg.benno.util.Util;
import de.sg.benno.chunk.Chunk;
import de.sg.benno.renderer.Zoom;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents a BshFile.
 * The class loads a Bsh file.
 */
public class BshFile extends BinaryFile {

    //-------------------------------------------------
    // Types
    //-------------------------------------------------

    /**
     * Represents the type of Bsh image.
     */
    private enum BshType {
        /**
         * Custom Benno4j type used for placeholder images.
         */
        PLACEHOLDER(0),

        /**
         * A common type used by all game types.
         */
        NORMAL(1),

        /**
         * Used by the new History Edition.
         */
        NEW(13);

        private final int typeValue;
        private static final HashMap<Integer, BshType> map = new HashMap<>();

        BshType(int typeValue) {
            this.typeValue = typeValue;
        }

        static {
            for (var bshType : BshType.values()) {
                map.put(bshType.typeValue, bshType);
            }
        }

        /**
         * Get {@link BshType} from int.
         *
         * @param typeValue the value of the type.
         * @return {@link BshType}
         */
        public static BshType valueOf(int typeValue) {
            return map.get(typeValue);
        }

        /**
         * Get int from {@link BshType}.
         *
         * @return the value of the type.
         */
        public int getTypeValue() {
            return typeValue;
        }
    }

    /**
     * Stores info about a placeholder offset.
     */
    private static class Placeholder {
        private final int offset;
        private int width;
        private int height;

        public Placeholder(int offset) {
            this.offset = offset;
        }

        public int getOffset() {
            return offset;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    /**
     * A {@link BufferedImage} with additional information.
     */
    private static class BufferedBshImage {
        final BufferedImage image;
        final BshType type;
        final int offset;
        final int gfxIndex;

        /**
         * Constructs a new {@link BufferedBshImage} object.
         *
         * @param image A {@link BufferedImage}
         * @param type A {@link BshType}
         * @param offset The offset of the Bsh image.
         * @param gfxIndex The gfx index of Bsh image.
         */
        BufferedBshImage(BufferedImage image, BshType type, int offset, int gfxIndex) {
            this.image = Objects.requireNonNull(image, "image must not be null");
            this.type = Objects.requireNonNull(type, "type must not be null");
            this.offset = offset;
            this.gfxIndex = gfxIndex;
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

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The color values from the <i>stadtfld.col</i> file.
     * These are loaded with {@link PaletteFile}.
     */
    private final int[] palette;

    /**
     * If the variable is true, all images are saved as a Png.
     */
    private final boolean saveAsPng;

    /**
     * The offsets to the Bsh images.
     */
    private final ArrayList<Integer> offsets = new ArrayList<>();

    /**
     * To store all placeholder offsets.
     */
    private final HashMap<Integer, Placeholder> placeholders = new HashMap<>();

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
     * Shortcut to the first {@link Chunk}.
     */
    private final Chunk chunk0;

    /**
     * Store the {@link Zoom} if this a zoomable file.
     * To generate a valid and readable output path for png files.
     */
    private final Zoom zoom;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link BshFile} object.
     *
     * @param path The {@link Path} to the Bsh file.
     * @param palette The color values from the <i>stadtfld.col</i> file.
     * @param zoom The {@link Zoom} of the (zoomable) Bsh file.
     * @param saveAsPng If the variable is true, all images are saved as a Png.
     * @throws IOException If an I/O error is thrown.
     */
    public BshFile(Path path, int[] palette, Zoom zoom, boolean saveAsPng) throws IOException {
        super(Objects.requireNonNull(path, "path must not be null"));

        LOGGER.debug("Creates BshFile object from file {}.", path);

        this.zoom = zoom;
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
     * @param saveAsPng If the variable is true, all images are saved as a Png.
     * @throws IOException If an I/O error is thrown.
     */
    public BshFile(Path path, int[] palette, boolean saveAsPng) throws IOException {
        this(path, palette, null, saveAsPng);
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
        LOGGER.debug("Clean up {} bsh textures.", bshTextures.size());

        for (var bshTexture : bshTextures) {
            bshTexture.cleanUp();
        }
    }

    //-------------------------------------------------
    // Offsets
    //-------------------------------------------------

    /**
     * Reads and saves all offsets of the Bsh images.
     * Stores in {@link #offsets}.
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
     * Stores {@link #placeholders}.
     * The offset is a placeholder offset if the difference between two offsets is 20 bytes.
     */
    private void validateOffsets() {
        for (int i = 0; i < offsets.size(); i++) {
            if (i + 1 < offsets.size()) {
                var offset = offsets.get(i);
                var nextOffset = offsets.get(i + 1);
                if (nextOffset - offset == 20) {
                    var placeholder = new Placeholder(offset);
                    placeholders.put(offset, placeholder);
                }
            }
        }

        if (!placeholders.isEmpty()) {
            LOGGER.debug("Detected {} placeholder offsets.", placeholders.size());
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
        var gfxIndex = 0;
        for (var offset : offsets) {
            if (!placeholders.containsKey(offset)) {
                var bufferedBshImage = createBufferedBshImage(offset, gfxIndex);
                if (bufferedBshImage.type == BshType.NEW) {
                    decodeTexture13(bufferedBshImage);
                } else {
                    decodeTexture(bufferedBshImage);
                }
            } else {
                var bufferedBshImage = createPlaceholderBufferedBshImage(
                        placeholders.get(offset),
                        gfxIndex
                );
                bshTextures.add(new BshTexture(bufferedBshImage.image));
                if (saveAsPng) {
                    saveAsPng(bufferedBshImage);
                }
            }

            gfxIndex++;
        }

        if (bshTextures.isEmpty() || (bshTextures.size() != gfxIndex)) {
            throw new BennoRuntimeException("Unexpected error.");
        }

        LOGGER.debug("A total of {} bsh textures were created.", bshTextures.size());
    }

    /**
     * Reads the pixel data from the {@link #chunk0} and uses it to create a {@link BshTexture} object.
     *
     * @param bufferedBshImage {@link BufferedBshImage}
     * @throws IOException If an I/O error is thrown.
     */
    private void decodeTexture13(BufferedBshImage bufferedBshImage) throws IOException {
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
                bufferedBshImage.image.setRGB(x, y, 0);
                x++;
            }

            int numPixels = chunk0.getData().getInt();
            for (int i = 0; i < numPixels; i++) {
                var b = chunk0.getData().get();
                var g = chunk0.getData().get();
                var r = chunk0.getData().get();
                var a = chunk0.getData().get();

                var color = Util.rgbToInt(Util.byteToInt(r), Util.byteToInt(g), Util.byteToInt(b));

                bufferedBshImage.image.setRGB(x, y, color);
                x++;
            }
        }

        bshTextures.add(new BshTexture(bufferedBshImage.image));
        if (saveAsPng) {
            saveAsPng(bufferedBshImage);
        }
    }

    /**
     * Reads the pixel data from the {@link #chunk0} and uses it to create a {@link BshTexture} object.
     *
     * @param bufferedBshImage {@link BufferedBshImage}
     * @throws IOException If an I/O error is thrown.
     */
    private void decodeTexture(BufferedBshImage bufferedBshImage) throws IOException {
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
                bufferedBshImage.image.setRGB(x, y, 0);
                x++;
            }

            // number of pixels that follow
            int numPixels = Util.byteToInt(chunk0.getData().get());
            for (int i = 0; i < numPixels; i++) {
                // pixels to insert at current position
                var colorIndex = Util.byteToInt(chunk0.getData().get());
                var color = palette[colorIndex];
                bufferedBshImage.image.setRGB(x, y, color);
                x++;
            }
        }

        bshTextures.add(new BshTexture(bufferedBshImage.image));
        if (saveAsPng) {
            saveAsPng(bufferedBshImage);
        }
    }

    /**
     * Reads width, height, type and length of a Bsh image from {@link #chunk0}.
     * A {@link BufferedBshImage} with an empty {@link BufferedImage} is created from this.
     *
     * @param offset The offset in {@link #chunk0}.
     * @param gfxIndex The gfx index.
     *
     * @return {@link BufferedBshImage}
     */
    private BufferedBshImage createBufferedBshImage(int offset, int gfxIndex) {
        Objects.requireNonNull(chunk0, "chunk0 must not be null");

        // new position
        chunk0.getData().position(offset);

        // read width and height
        var width = chunk0.getData().getInt();
        var height = chunk0.getData().getInt();

        // check values
        if (width <= 0 || height <= 0) {
            throw new BennoRuntimeException("Invalid width or height.");
        }

        // read type and length
        var type = chunk0.getData().getInt();
        var length = chunk0.getData().getInt();

        // create a width x height pixel image with support for transparency
        // now we are ready to set pixels on BufferedImage and eventually save it to disk in a standard image format such as PNG
        return new BufferedBshImage(
                new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB),
                BshType.valueOf(type),
                offset,
                gfxIndex
        );
    }

    /**
     * Creates an {@link BufferedBshImage} with an empty {@link BufferedImage}.
     *
     * @param placeholder The {@link Placeholder} object to change.
     * @param gfxIndex The gfx index.
     *
     * @return {@link BufferedBshImage}
     */
    private BufferedBshImage createPlaceholderBufferedBshImage(Placeholder placeholder, int gfxIndex) {
        Objects.requireNonNull(chunk0, "chunk0 must not be null");

        // new position
        chunk0.getData().position(placeholder.offset);

        // read width and height
        placeholder.width = chunk0.getData().getInt();
        placeholder.height = chunk0.getData().getInt();

        // check values
        if (placeholder.width <= 0 || placeholder.height <= 0) {
            throw new BennoRuntimeException("Invalid width or height.");
        }

        // create a width x height pixel image with support for transparency
        return new BufferedBshImage(
                new BufferedImage(placeholder.width, placeholder.height, BufferedImage.TYPE_INT_ARGB),
                BshType.PLACEHOLDER,
                placeholder.offset,
                gfxIndex
        );
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Saves the {@link BufferedImage} from {@link BufferedBshImage} as a Png.
     *
     * @param bufferedBshImage {@link BufferedBshImage}
     * @throws IOException If an I/O error is thrown.
     */
    private void saveAsPng(BufferedBshImage bufferedBshImage) throws IOException {
        var bshFilename = getPath().getFileName().toString().toLowerCase();
        var preName = bshFilename.substring(0, bshFilename.lastIndexOf("."));
        var outDir = BennoConfig.PNG_OUT_PATH;

        if (zoom != null) {
            outDir += zoom + "/" + preName;
        } else {
            outDir += preName;
        }

        Files.createDirectories(Paths.get(outDir));

        String filename = outDir + "/" +
                preName + "_" +
                bufferedBshImage.gfxIndex + ".png";

        File file = new File(filename);
        if (!file.exists()) {
            var result = file.createNewFile();
            if (!result) {
                throw new BennoRuntimeException("Unexpected error.");
            }
        }

        ImageIO.write(bufferedBshImage.image, "PNG", file);
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
