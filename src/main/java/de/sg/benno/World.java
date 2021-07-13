/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno;

import de.sg.benno.chunk.Island5;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Common stuff for the world.
 */
public class World {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * The width of the world in tiles.
     */
    public static final int WORLD_WIDTH = 500;

    /**
     * The height of the world in tiles.
     */
    public static final int WORLD_HEIGHT = 350;

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Checks whether there is an {@link Island5} at the given position in the world.
     *
     * @param x The world position in x direction.
     * @param y The world position in y direction.
     * @param island5List A list of {@link Island5} objects.
     *
     * @return A nullable {@link Island5} Optional.
     */
    public static Optional<Island5> isIslandOnPosition(int x, int y, ArrayList<Island5> island5List) {
        Island5 result = null;

        for (var island5 : island5List) {
            if ((x >= island5.xPos) &&
                (y >= island5.yPos) &&
                (x < island5.xPos + island5.width) &&
                (y < island5.yPos + island5.height)) {
                result = island5;
            }
        }

        return Optional.ofNullable(result);
    }
}
