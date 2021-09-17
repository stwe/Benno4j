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

import de.sg.benno.BennoRuntimeException;
import de.sg.benno.chunk.Chunk;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Common stuff for all {@link BinaryFile} objects.
 */
public abstract class BinaryFile implements BinaryFileInterface {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link Path} to the file.
     */
    private Path path = null;

    /**
     * An {@link InputStream}.
     */
    private InputStream inputStream = null;

    /**
     * Each file has one or many {@link Chunk} objects.
     */
    private final ArrayList<Chunk> chunks = new ArrayList<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Contructs a new {@link BinaryFile} object from {@link Path}.
     *
     * @param path The {@link Path} to the file.
     * @throws IOException If an I/O error is thrown.
     */
    public BinaryFile(Path path) throws IOException {
        this.path = Objects.requireNonNull(path, "path must not be null");

        LOGGER.debug("Reading Chunks from {}.", path);

        readChunksFromFile();
    }

    /**
     * Contructs a new {@link BinaryFile} object from {@link InputStream}.
     *
     * @param inputStream An {@link InputStream}.
     * @throws IOException If an I/O error is thrown.
     */
    public BinaryFile(InputStream inputStream) throws IOException {
        this.inputStream = Objects.requireNonNull(inputStream, "inputStream must not be null");

        LOGGER.debug("Reading Chunks from input stream.");

        readChunksFromFile();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #path}.
     *
     * @return {@link #path}
     */
    public Path getPath() {
        return path;
    }

    /**
     * Get {@link #chunks}.
     *
     * @return {@link #chunks}
     */
    public ArrayList<Chunk> getChunks() {
        return chunks;
    }

    /**
     * Get size (number of elements) of {@link #chunks}.
     *
     * @return int
     */
    public int getNumberOfChunks() {
        return chunks.size();
    }

    /**
     * Get {@link Chunk} by index.
     *
     * @param chunkIndex The given index.
     *
     * @return {@link Chunk}
     */
    public Chunk getChunk(int chunkIndex) {
        return chunks.get(chunkIndex);
    }

    /**
     * Checks whether a {@link Chunk} has the given id.
     *
     * @param chunkIndex The index of the {@link Chunk} in {@link #chunks}.
     * @param chunkId The Id.
     *
     * @return boolean
     */
    boolean chunkIndexHasId(int chunkIndex, String chunkId) {
        return getChunk(chunkIndex).getId().equals(chunkId);
    }

    //-------------------------------------------------
    // BinaryFileInterface
    //-------------------------------------------------

    @Override
    public void readChunksFromFile() throws IOException {
        if (inputStream == null) {
            if (path == null) {
                throw new BennoRuntimeException("No path given.");
            }

            inputStream = new FileInputStream(path.toFile());
        }

        while (inputStream.available() > 0) {
            var chunk = new Chunk(inputStream);
            chunks.add(chunk);
        }
    }

    @Override
    public abstract void readDataFromChunks() throws Exception;
}
