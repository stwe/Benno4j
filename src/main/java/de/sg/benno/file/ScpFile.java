/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.file;

import de.sg.benno.chunk.Island5;
import de.sg.benno.chunk.Ship4;
import de.sg.benno.chunk.WorldData;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;

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
