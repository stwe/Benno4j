/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.chunk;

/**
 * Maps the 8-byte C structure TISOSAVE and represents a Tile.
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
 */
public class Tile {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * The size of the original C structure TISOSAVE in bytes.
     */
    public static final int BYTES_PER_TILE = 8;

    /**
     * This means that the tile is not set.
     */
    public static final int NO_GRAPHIC = 0xFFFF;

    //-------------------------------------------------
    // TISOSAVE
    //-------------------------------------------------

    /**
     * The tile gaphic Id, see haeuser.cod for reference.
     */
    public int graphicId = NO_GRAPHIC;

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
     * Constructs a new {@link Tile} object.
     */
    public Tile() {}

    /**
     * Constructs a new {@link Tile} object by a {@link #graphicId}.
     *
     * @param graphicId {@link #graphicId}.
     */
    public Tile(int graphicId) {
        this.graphicId = graphicId;
    }

    /**
     * Constructs a new {@link Tile} object by another {@link Tile} (copy constructor).
     *
     * @param tile {@link Tile}.
     */
    public Tile(Tile tile) {
        this.graphicId = tile.graphicId;
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
