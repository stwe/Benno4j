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

package de.sg.benno.chunk;

/**
 * Maps the 8-byte C structure TISOSAVE and represents an IslandTile.
 *
 * <pre>
 * typedef struct {
 *     uint16_t id;
 *     uint8_t posx;
 *     uint8_t posy;
 *     uint32_t ausricht : 2;
 *     uint32_t animcnt : 4;
 *     uint32_t inselnr : 8;
 *     uint32_t stadtnr : 3;
 *     uint32_t randnr : 5;
 *     uint32_t playernr : 4;
 * } TISOSAVE;
 * </pre>
 *
 * Represents a IslandTile to display the terrain.
 */
public class IslandTile extends Tile {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * The size of the original C structure TISOSAVE in bytes.
     */
    public static final int BYTES_PER_TILE = 8;

    //-------------------------------------------------
    // TISOSAVE
    //-------------------------------------------------

    /**
     * The x position on island.
     */
    public int xPosOnIsland;

    /**
     * The y position on island.
     */
    public int yPosOnIsland;

    /**
     * The orientation/rotation of the tile.
     */
    public int orientation;

    /**
     * The animation step for the tile.
     */
    public int animationCount;

    /**
     * The parent Island number.
     */
    public int islandNumber;

    /**
     * The parent City number.
     */
    public int cityNumber;

    /**
     * An unknown random number.
     */
    public int randomNumber;

    /**
     * The player that has occupies this tile.
     */
    public int playerNumber;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link IslandTile} object.
     */
    public IslandTile() {
        super();
    }

    /**
     * Constructs a new {@link IslandTile} object.
     *
     * @param graphicId The tile graphic ID.
     */
    public IslandTile(int graphicId) {
        super(graphicId);
    }

    /**
     * Copy constructor (copy each field of the input object into the new instance).
     * Constructs a new {@link IslandTile} object by another {@link IslandTile}.
     *
     * @param tile {@link IslandTile}.
     */
    public IslandTile(IslandTile tile) {
        setGraphicId(tile.getGraphicId());

        this.xPosOnIsland = tile.xPosOnIsland;
        this.yPosOnIsland = tile.yPosOnIsland;
        this.orientation = tile.orientation;
        this.animationCount = tile.animationCount;
        this.islandNumber = tile.islandNumber;
        this.cityNumber = tile.cityNumber;
        this.randomNumber = tile.randomNumber;
        this.playerNumber = tile.playerNumber;
    }
}
