package de.sg.benno.file;

import de.sg.benno.chunk.Chunk;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

public abstract class BinaryFile implements BinaryFileInterface {

    private final Path path;
    private final ArrayList<Chunk> chunks = new ArrayList<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public BinaryFile(Path path) throws IOException {
        this.path = Objects.requireNonNull(path, "path must not be null");
        readChunksFromFile();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    public Path getPath() {
        return path;
    }

    public ArrayList<Chunk> getChunks() {
        return chunks;
    }

    public int getNumberOfChunks() {
        return chunks.size();
    }

    public Chunk getChunk(int chunkIndex) {
        return chunks.get(chunkIndex);
    }

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
    public abstract void readDataFromChunks() throws IOException;
}
