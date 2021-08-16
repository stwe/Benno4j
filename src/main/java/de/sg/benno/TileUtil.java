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

    /*
    h/w = 0.5

    126,87 deg

        \    /
         \  /
          \/  arctan(sin(30)) = 26.565
    -----------
    */

    public static final double ANGLE_TO_THE_HORIZONTAL = 26.565;

    public static Vector2i worldToScreen(int worldX, int worldY, int tileWidthHalf, int tileHeightHalf) {
        return new Vector2i(
                (worldX - worldY) * tileWidthHalf,
                (worldX + worldY) * tileHeightHalf
        );
    }

    public static Vector2i screenToWorld(int screenX, int screenY, int tileWidth, int tileHeight) {
        return new Vector2i(
                (screenX / tileWidth + screenY / tileHeight),
                (screenY / tileHeight - screenX / tileWidth)
        );
    }

    public static int adjustHeight(int tileHeightHalf, int tileHeight, int elevation) {
        return 2 * tileHeightHalf - tileHeight / elevation;
    }

    public static int getIndexFrom2D(int worldX, int worldY) {
        return worldY * World.WORLD_WIDTH + worldX;
    }
}
