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

import de.sg.benno.ogl.renderer.RenderUtil;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

/**
 * A TitleGraphic object contains information for display on the screen.
 */
public class TileGraphic {

    //-------------------------------------------------
    // Types
    //-------------------------------------------------

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

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The parent {@link Tile}.
     */
    public Tile parentTile;

    /**
     * The gfx, see haeuser.cod or {@link de.sg.benno.data.Building} for reference.
     */
    public int gfx;

    /**
     * {@link TileHeight}
     */
    public TileHeight tileHeight = TileHeight.SEA_LEVEL;

    /**
     * The position in world space.
     */
    public Vector2i worldPosition = new Vector2i();

    /**
     * The position in screen space.
     */
    public Vector2f screenPosition = new Vector2f();

    /**
     * The tile size.
     */
    public Vector2f size = new Vector2f();

    /**
     * The color of this tile.
     */
    public Vector3f color = new Vector3f();

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Create and get the model matrix of this {@link TileGraphic}.
     *
     * @return {@link Matrix4f}
     */
    public Matrix4f getModelMatrix() {
        return RenderUtil.createModelMatrix(screenPosition, size);
    }
}
