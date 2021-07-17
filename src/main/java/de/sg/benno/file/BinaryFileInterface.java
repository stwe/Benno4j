/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.file;

import java.io.IOException;

/**
 * Interface for loading BinaryFiles.
 */
public interface BinaryFileInterface {

    /**
     * Opens and reads the file. {@link de.sg.benno.chunk.Chunk} objects are created from the file content.
     *
     * @throws IOException If an I/O error is thrown.
     */
    void readChunksFromFile() throws IOException;

    /**
     * Reads the data from the {@link de.sg.benno.chunk.Chunk} objects.
     *
     * @throws Exception If an error is thrown.
     */
    void readDataFromChunks() throws Exception;
}
