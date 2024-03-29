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
import de.sg.benno.chunk.IslandHouse;
import de.sg.benno.chunk.Ship4;
import de.sg.benno.chunk.WorldData;
import de.sg.benno.state.Context;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Loads a savegame (GAM file).
 */
public class GamFile extends BinaryFile implements WorldData {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link Context} object.
     */
    private final Context context;

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

        this.context = Objects.requireNonNull(context, "context must not be null");

        readDataFromChunks();
    }

    /**
     * Contructs a new {@link GamFile} object.
     *
     * @param inputStream An {@link InputStream}.
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public GamFile(InputStream inputStream, Context context) throws Exception {
        super(Objects.requireNonNull(inputStream, "inputStream must not be null"));

        LOGGER.debug("Creates GamFile object from input stream.");

        this.context = Objects.requireNonNull(context, "context must not be null");

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
                var island5 = new Island5(chunk, context);
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

        for (var island5 : island5List) {
            island5.cleanUp();
        }
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
