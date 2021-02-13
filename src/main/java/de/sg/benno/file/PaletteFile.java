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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;

public class PaletteFile extends BinaryFile {

    private static final int NUMBER_OF_COLORS = 256;
    private static final int NUMBER_OF_CHUNKS = 1;
    private static final String CHUNK_ID = "COL";

    private final int[] palette = new int[NUMBER_OF_COLORS];

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    PaletteFile(Path path) throws IOException {
        super(Objects.requireNonNull(path, "path must not be null"));

        LOGGER.debug("Creates PaletteFile object from file {}.", path);

        if (getNumberOfChunks() != NUMBER_OF_CHUNKS) {
            throw new BennoRuntimeException("Invalid number of Chunks.");
        }

        if (!chunkIndexHasId(0, CHUNK_ID)) {
            throw new BennoRuntimeException("Invalid Chunk Id.");
        }

        readDataFromChunks();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    public int[] getPalette() {
        return palette;
    }

    //-------------------------------------------------
    // BinaryFileInterface
    //-------------------------------------------------

    @Override
    public void readDataFromChunks() {
        LOGGER.debug("Start reading Palette data from Chunks...");

        var chunk0 = getChunk(0);

        for (int i = 0; i < palette.length; i++) {
            int red = Util.byteToInt(chunk0.getData().get());
            int green = Util.byteToInt(chunk0.getData().get());
            int blue = Util.byteToInt(chunk0.getData().get());

            chunk0.getData().get(); // skip next byte

            palette[i] = Util.rgbToInt(red, green, blue);
        }

        LOGGER.debug("Palette data read successfully.");
    }
}
