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
import de.sg.benno.chunk.Ship4;
import de.sg.benno.chunk.WorldData;
import de.sg.benno.state.Context;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static de.sg.ogl.Log.LOGGER;

/**
 * Loads a savegame (GAM file).
 */
public class GamFile extends BinaryFile implements WorldData {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link BennoFiles} object.
     */
    private final BennoFiles bennoFiles;

    /**
     * The list with all {@link Island5} objects.
     */
    private final ArrayList<Island5> island5List = new ArrayList<>();

    /**
     * The list with all {@link Ship4} objects.
     */
    private final ArrayList<Ship4> ship4sList = new ArrayList<>();

    // todo: load other content

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Contructs a new {@link GamFile} object.
     *
     * @param path The {@link Path} to the savegame.
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public GamFile(Path path, Context context) throws Exception {
        super(Objects.requireNonNull(path, "path must not be null"));

        LOGGER.debug("Creates GamFile object from file {}.", path);

        this.bennoFiles = Objects.requireNonNull(context, "context must not be null").bennoFiles;

        readDataFromChunks();
    }

    //-------------------------------------------------
    // BinaryFileInterface
    //-------------------------------------------------

    @Override
    public void readDataFromChunks() throws Exception {
        LOGGER.debug("Start reading savegame data from Chunks...");

        for (var chunk : getChunks()) {
            if (chunk.getId().equals("INSEL5")) {
                var island5 = new Island5(chunk, bennoFiles);
                island5List.add(island5);
            }

            if (chunk.getId().equals("INSELHAUS")) {
                var currentIsland5 = island5List.get(island5List.size() - 1);
                var islandHouse = new IslandHouse(chunk, currentIsland5);
                currentIsland5.addIslandHouse(islandHouse);
            }

            if (chunk.getId().equals("SHIP4")) {
                var ship4 = new Ship4(chunk);
                ship4sList.add(ship4);
            }
        }

        initIsland5Layer();

        LOGGER.debug("Savegame data read successfully.");
    }

    //-------------------------------------------------
    // WorldDataInterface
    //-------------------------------------------------

    @Override
    public ArrayList<Island5> getIsland5List() {
        return island5List;
    }

    @Override
    public ArrayList<Ship4> getShips4List() {
        return ship4sList;
    }

    @Override
    public void cleanUp() {
        LOGGER.debug("Start clean up for the GamFile.");
    }

    //-------------------------------------------------
    // Layer
    //-------------------------------------------------

    /**
     * Initializes the layers of all {@link Island5} objects.
     * Sets the final top and bottom layer.
     *
     * @throws IOException If an I/O error is thrown.
     */
    private void initIsland5Layer() throws IOException {
        LOGGER.debug("Initialize top and bottom layer for each island.");

        for (var island5 : island5List) {
            island5.setTopAndBottomLayer();
        }
    }
}
