/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.file;

import de.sg.benno.chunk.Chunk;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Common stuff for all {@link BinaryFile} objects.
 */
public abstract class BinaryFile implements BinaryFileInterface {

    /**
     * The {@link Path} to the file.
     */
    private final Path path;

    /**
     * Each file has one or many {@link Chunk} objects.
     */
    private final ArrayList<Chunk> chunks = new ArrayList<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Contructs a new {@link BinaryFile} object.
     *
     * @param path The {@link Path} to the file.
     * @throws IOException If an I/O error is thrown.
     */
    public BinaryFile(Path path) throws IOException {
        this.path = Objects.requireNonNull(path, "path must not be null");
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
        var inputStream = new FileInputStream(path.toFile());
        while (inputStream.available() > 0) {
            var chunk = new Chunk(inputStream);
            chunks.add(chunk);
        }
    }

    @Override
    public abstract void readDataFromChunks() throws Exception;
}
