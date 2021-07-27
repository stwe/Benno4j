/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.chunk;

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
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix
                .identity()
                .translate(new Vector3f(screenPosition, 0.0f))
                .scale(new Vector3f(size, 1.0f));

        return modelMatrix;
    }
}
