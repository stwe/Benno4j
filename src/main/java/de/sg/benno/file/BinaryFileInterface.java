/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.file;

import java.io.IOException;

public interface BinaryFileInterface {
    void readChunksFromFile() throws IOException;
    void readDataFromChunks() throws IOException;
}
