/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.file;

import de.sg.benno.chunk.Island5;
import de.sg.benno.chunk.IslandHouse;
import de.sg.benno.data.Building;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;

public class GamFile extends BinaryFile {

    private final HashMap<Integer, Building> buildings;
    private final ArrayList<Island5> island5List = new ArrayList<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public GamFile(Path path, HashMap<Integer, Building> buildings) throws IOException {
        super(Objects.requireNonNull(path, "path must not be null"));

        LOGGER.debug("Creates GamFile object from file {}.", path);

        Objects.requireNonNull(buildings, "buildings must not be null");
        this.buildings = buildings;

        readDataFromChunks();
        initIsland5Layer();
    }

    //-------------------------------------------------
    // BinaryFileInterface
    //-------------------------------------------------

    @Override
    public void readDataFromChunks() throws IOException {
        LOGGER.debug("Start reading savegame data from Chunks...");

        for (var chunk : getChunks()) {
            if (chunk.getId().equals("INSEL5")) {
                var island5 = new Island5(chunk, buildings);
                island5List.add(island5);
            }

            if (chunk.getId().equals("INSELHAUS")) {
                var currentIsland5 = island5List.get(island5List.size() - 1);
                var islandHouse = new IslandHouse(chunk, currentIsland5);
                currentIsland5.addIslandHouse(islandHouse);
            }
        }

        LOGGER.debug("Savegame data read successfully.");
    }

    //-------------------------------------------------
    // Layer
    //-------------------------------------------------

    private void initIsland5Layer() {
        for (var island5 : island5List) {
            island5.initLayer();
        }
    }
}
