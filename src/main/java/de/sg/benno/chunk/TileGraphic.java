/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.chunk;

import org.joml.Vector2f;
import org.joml.Vector2i;

public class TileGraphic {

    /**
     * Represents the two possible heights of the terrain.
     */
    public enum TileHeight {
        SEA_LEVEL(0),
        CLIFF(20);

        public final int value;

        TileHeight(int value) {
            this.value = value;
        }
    }

    public static class TileGfxInfo {
        public int gfxIndex;
        public TileHeight tileHeight = TileHeight.SEA_LEVEL;
    }

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    public TileGfxInfo tileGfxInfo = new TileGfxInfo();
    public Vector2i worldPosition = new Vector2i();
    public Vector2f screenPosition = new Vector2f();
    public Vector2f size = new Vector2f();
}
