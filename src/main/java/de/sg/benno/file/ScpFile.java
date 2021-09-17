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

import de.sg.benno.chunk.Island5;
import de.sg.benno.chunk.Ship4;
import de.sg.benno.chunk.WorldData;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents a ScpFile.
 */
public class ScpFile extends BinaryFile implements WorldData {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------


    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link ScpFile} object.
     *
     * @param path The {@link Path} to the SCP file.
     * @throws IOException If an I/O error is thrown.
     */
    public ScpFile(Path path) throws IOException {
        super(Objects.requireNonNull(path, "path must not be null"));

        LOGGER.debug("Creates ScpFile object from file {}.", path);

        readDataFromChunks();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------



    //-------------------------------------------------
    // BinaryFileInterface
    //-------------------------------------------------

    @Override
    public void readDataFromChunks() {
        /*
        LOGGER.debug("Start reading scp data from Chunks...");

        for (var chunk : getChunks()) {
            if (chunk.getId().equals("INSEL3")) {

            }

            if (chunk.getId().equals("INSEL4")) {

            }

            if (chunk.getId().equals("INSEL5")) {

            }

            if (chunk.getId().equals("INSELHAUS")) {

            }
        }

        LOGGER.debug("Scp data read successfully.");
        */
    }

    //-------------------------------------------------
    // WorldDataInterface
    //-------------------------------------------------

    @Override
    public ArrayList<Island5> getIsland5List() {
        return null;
    }

    @Override
    public ArrayList<Ship4> getShips4List() {
        return null;
    }

    @Override
    public void cleanUp() {
        LOGGER.debug("Start clean up for the ScpFile.");
    }
}
