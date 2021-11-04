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

package de.sg.benno.content;

import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.renderer.Zoom;

import java.util.Optional;

/**
 * Represents a World.
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
    // Member
    //-------------------------------------------------

    /**
     * The {@link Water} object with the deep water area.
     */
    private final Water water;

    /**
     * The {@link Terrain} object contains all islands.
     */
    private final Terrain terrain;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link World} object.
     *
     * @param water The {@link Water} object.
     * @param terrain The {@link Terrain} object.
     */
    public World(Water water, Terrain terrain) {
        this.water = water;
        this.terrain = terrain;
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #water}.
     *
     * @return {@link #water}
     */
    public Water getWater() {
        return water;
    }

    /**
     * Get {@link #terrain}.
     *
     * @return {@link #terrain}
     */
    public Terrain getTerrain() {
        return terrain;
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Get a {@link TileGraphic} by a given world space position.
     *
     * @param zoom The current {@link Zoom}.
     * @param x The x position in world space.
     * @param y The y position in world space.
     *
     * @return A nullable {@link TileGraphic} Optional.
     */
    public Optional<TileGraphic> getTileGraphic(Zoom zoom, int x, int y) {
        var tileGraphicOptional = water.getWaterTileGraphic(zoom, x, y);
        if (tileGraphicOptional.isPresent()) {
            return tileGraphicOptional;
        } else {
            for (var island : terrain.getProvider().getIsland5List()) {
                tileGraphicOptional = terrain.getTileGraphic(zoom, island, x, y);
                if (tileGraphicOptional.isPresent()) {
                    return tileGraphicOptional;
                }
            }
        }

        return Optional.empty();
    }
}
