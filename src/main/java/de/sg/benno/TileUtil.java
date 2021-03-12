/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno;

import org.joml.Vector2i;

public class TileUtil {

    public static Vector2i worldToScreen(int worldX, int worldY, int tileWidthHalf, int tileHeightHalf) {
        return new Vector2i(
                (worldX - worldY) * tileWidthHalf,
                (worldX + worldY) * tileHeightHalf
        );
    }

    public static int adjustHeight(int tileHeightHalf, int tileHeight, int elevation) {
        return 2 * tileHeightHalf - tileHeight / elevation;
    }

    public static int getIndexFrom2D(int worldX, int worldY) {
        return worldY * World.WORLD_WIDTH + worldX;
    }
}
