/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.renderer;

import de.sg.benno.BennoConfig;
import org.joml.Vector2i;

/**
 * Represents a zoom level with the respective constants.
 */
public enum Zoom {
    /**
     * Small zoom graphics.
     */
    SGFX(8, 4 ,4 ,16, 8, BennoConfig.SGFX_CAMERA_SPEED),

    /**
     * Medium zoom graphics.
     */
    MGFX(16, 8, 2, 32, 16, BennoConfig.MGFX_CAMERA_SPEED),

    /**
     * Full zoom graphics.
     */
    GFX(32, 16, 1, 64, 31, BennoConfig.GFX_CAMERA_SPEED);

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    public int xRaster;
    public int yRaster;
    public int elevation;
    public int defaultTileWidth;
    public int defaultTileHeight;
    public int speedFactor;

    private final Vector2i cameraSpeed = new Vector2i();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    Zoom(
            int xRaster, int yRaster,
            int elevation,
            int defaultTileWidth, int defaultTileHeight,
            int speedFactor
    ) {
        this.xRaster = xRaster;
        this.yRaster = yRaster;
        this.elevation = elevation;
        this.defaultTileWidth = defaultTileWidth;
        this.defaultTileHeight = defaultTileHeight;

        this.speedFactor = speedFactor;

        calcCameraSpeed();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #cameraSpeed}.
     *
     * @return {@link #cameraSpeed}
     */
    public Vector2i getCameraSpeed() {
        return cameraSpeed;
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Calc {@link #cameraSpeed}.
     * The camera moves in world space units.
     */
    private void calcCameraSpeed() {
        this.cameraSpeed.x = defaultTileWidth * speedFactor;

        // the default tile height in GFX is 31, which must be corrected to 32
        if (defaultTileHeight != 31) {
            this.cameraSpeed.y = defaultTileHeight * speedFactor;
        } else {
            this.cameraSpeed.y = 32 * speedFactor;
        }
    }
}
