package de.sg.benno.file;

import java.io.IOException;

public interface BinaryFileInterface {
    void readChunksFromFile() throws IOException;
    void readDataFromChunks() throws IOException;
}
