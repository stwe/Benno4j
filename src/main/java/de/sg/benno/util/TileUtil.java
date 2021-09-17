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

package de.sg.benno.util;

import de.sg.benno.World;
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
