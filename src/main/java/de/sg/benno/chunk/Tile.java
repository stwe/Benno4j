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
 * Represents a Tile.
 * Common stuff to display all water and island tiles.
 */
public abstract class Tile implements WorldTile {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * This means that the {@link Tile} is not set.
     */
    public static final int NO_GRAPHIC = 0xFFFF;

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The tile graphic ID, see <i>haeuser.cod</i> or {@link de.sg.benno.data.Building} for reference.
     * Each building ID refers to one or more graphics (gfx).
     */
    private int graphicId = NO_GRAPHIC;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Tile} object.
     */
    public Tile() {
    }

    /**
     * Constructs a new {@link Tile} object.
     *
     * @param graphicId {@link #graphicId}.
     */
    public Tile(int graphicId) {
        this.graphicId = graphicId;
    }

    //-------------------------------------------------
    // Implement WorldTile
    //-------------------------------------------------

    @Override
    public int getGraphicId() {
        return graphicId;
    }

    //-------------------------------------------------
    // Setter
    //-------------------------------------------------

    /**
     * Set {@link #graphicId}.
     *
     * @param graphicId The tile graphic ID.
     */
    public void setGraphicId(int graphicId) {
        this.graphicId = graphicId;
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Checks whether the {@link #graphicId} refers to a water graphic ID.
     *
     * @param tile {@link Tile}
     *
     * @return true if the {@link #graphicId} refers to a water graphic ID.
     */
    public static boolean isWaterTile(Tile tile) {
        return (tile.graphicId >= 1201 && tile.graphicId <= 1209) ||
                (tile.graphicId >= 1251 && tile.graphicId <= 1259);
    }
}
