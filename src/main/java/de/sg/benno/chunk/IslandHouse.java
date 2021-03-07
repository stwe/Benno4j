/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.chunk;

import java.util.ArrayList;
import java.util.Objects;

import static de.sg.benno.Util.*;
import static de.sg.benno.chunk.Tile.BYTES_PER_TILE;
import static de.sg.ogl.Log.LOGGER;

/**
 * Represents an IslandHouse. An IslandHouse represents a tile layer consists of many tiles.
 */
public class IslandHouse {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * A {@link Chunk} holding the {@link IslandHouse} data.
     */
    private final Chunk chunk;

    /**
     * The {@link Island5} object belonging to this object.
     */
    private final Island5 parentIsland;

    /**
     * The number of raw tiles.
     */
    private final int nrOfRawElements;

    /**
     * The raw tiles list. A raw {@link Tile} contains the data as read from the {@link #chunk}.
     */
    private final ArrayList<Tile> rawTiles = new ArrayList<>();

    /**
     * The layer tiles list.
     */
    private final ArrayList<Tile> layerTiles = new ArrayList<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link IslandHouse} object by a given {@link Chunk}.
     *
     * @param chunk {@link Chunk}
     * @param parentIsland {@link Island5}
     */
    public IslandHouse(Chunk chunk, Island5 parentIsland) {
        this.chunk = Objects.requireNonNull(chunk, "chunk must not be null");
        this.parentIsland = Objects.requireNonNull(parentIsland, "parentIsland must not be null");

        LOGGER.debug("Creates IslandHouse object.");

        nrOfRawElements = calcNrOfRawElements();
        if (nrOfRawElements > 0) {
            createRawTiles();
        } else {
            LOGGER.debug("Skip reading. There were no tiles found.");
        }

        createLayerTiles();
    }

    //-------------------------------------------------
    // Raw Tiles
    //-------------------------------------------------

    /**
     * Calculates the number of tiles in the {@link #chunk}.
     *
     * @return The number of raw tiles.
     */
    private int calcNrOfRawElements() {
        var res = chunk.getDataLength() / BYTES_PER_TILE;
        if (res > 0) {
            LOGGER.debug("Detected {} tiles.", res);
        }

        return res;
    }

    /**
     * Reads the data from the {@link #chunk} and uses it to create the raw tiles.
     */
    private void createRawTiles() {
        LOGGER.debug("Start reading the IslandHouse tile data...");

        for (var i = 0; i < nrOfRawElements; i++) {
            var graphicId = shortToInt(chunk.getData().getShort()); // 2
            var xPosOnIsland = byteToInt(chunk.getData().get());    // 1
            var yPosOnIsland = byteToInt(chunk.getData().get());    // 1
            var packedInt = chunk.getData().getInt();               // 4
                                                                         // = 8 Bytes

            var tile = new Tile();
            tile.graphicId = graphicId;
            tile.xPosOnIsland = xPosOnIsland;
            tile.yPosOnIsland = yPosOnIsland;

            tile.orientation = bitExtracted(packedInt, 2, 1);
            tile.animationCount = bitExtracted(packedInt, 4, 3);

            tile.islandNumber = bitExtracted(packedInt, 8, 7);
            if (!isValidIslandNumber(tile.islandNumber)) {
                throw new RuntimeException("Invalid island number.");
            }

            tile.cityNumber = bitExtracted(packedInt, 3, 15);
            tile.randomNumber = bitExtracted(packedInt, 5, 18);
            tile.playerNumber = bitExtracted(packedInt, 4, 23);

            rawTiles.add(tile);
        }

        LOGGER.debug("IslandHouse tile data read successfully.");
    }

    //-------------------------------------------------
    // Layer Tiles
    //-------------------------------------------------

    /**
     * Creates the layer tiles using raw tiles.
     */
    private void createLayerTiles() {
        var width = parentIsland.width;
        var height = parentIsland.height;
        var buildings = parentIsland.getBuildings();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                layerTiles.add(new Tile());
            }
        }

        for (int i = 0; i < nrOfRawElements; i++) {
            var rawTile = rawTiles.get(i);

            if (isValidTilePosition(rawTile.xPosOnIsland, rawTile.yPosOnIsland)) {
                var building = buildings.get(rawTile.graphicId);

                var w = 0;
                var h = 0;
                if (rawTile.orientation % 2 == 0) {
                    w = building.width;
                    h = building.height;
                } else {
                    w = building.height;
                    h = building.width;
                }

                for (int y = 0; y < h && isValidTilePosY(rawTile.yPosOnIsland + y); y++) {
                    for (int x = 0; x < w && isValidTilePosX(rawTile.xPosOnIsland + x); x++) {
                        var targetIndex = (rawTile.yPosOnIsland + y) * width + rawTile.xPosOnIsland + x;

                        layerTiles.get(targetIndex).graphicId = rawTile.graphicId;
                        layerTiles.get(targetIndex).xPosOnIsland = x;
                        layerTiles.get(targetIndex).yPosOnIsland = y;

                        layerTiles.get(targetIndex).orientation = rawTile.orientation;
                        layerTiles.get(targetIndex).animationCount = rawTile.animationCount;

                        layerTiles.get(targetIndex).islandNumber = rawTile.islandNumber;
                        layerTiles.get(targetIndex).cityNumber = rawTile.cityNumber;
                        layerTiles.get(targetIndex).randomNumber = rawTile.randomNumber;
                        layerTiles.get(targetIndex).playerNumber = rawTile.playerNumber;
                    }
                }
            } else {
                throw new RuntimeException("Invalid Tile position.");
            }
        }
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Checks whether the island number from the {@link #chunk} belong to the {@link #parentIsland}.
     *
     * @param islandNumber The given island number from the {@link #chunk}.
     * @return True if valid.
     */
    private boolean isValidIslandNumber(int islandNumber) {
        return islandNumber == parentIsland.islandNumber;
    }

    /**
     * Checks whether the specified position is on the island.
     *
     * @param xPosOnIsland The x position on the island.
     * @param yPosOnIsland The y position on the island.
     * @return True if valid.
     */
    private boolean isValidTilePosition(int xPosOnIsland, int yPosOnIsland) {
        return isValidTilePosX(xPosOnIsland) && isValidTilePosY(yPosOnIsland);
    }

    /**
     * Checks whether the specified x position is on the island.
     *
     * @param xPosOnIsland The x position on the island.
     * @return True if valid.
     */
    private boolean isValidTilePosX(int xPosOnIsland) {
        return xPosOnIsland < parentIsland.width;
    }

    /**
     * Checks whether the specified y position is on the island.
     *
     * @param yPosOnIsland The y position on the island.
     * @return True if valid.
     */
    private boolean isValidTilePosY(int yPosOnIsland) {
        return yPosOnIsland < parentIsland.height;
    }
}
