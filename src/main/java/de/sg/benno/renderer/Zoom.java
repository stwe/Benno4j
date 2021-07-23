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

    private final Vector2i cameraSpeed = new Vector2i();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    Zoom(
            int xRaster, int yRaster,
            int elevation,
            int defaultTileWidth, int defaultTileHeight,
            int cameraInitialSpeedFactor
    ) {
        this.xRaster = xRaster;
        this.yRaster = yRaster;
        this.elevation = elevation;
        this.defaultTileWidth = defaultTileWidth;
        this.defaultTileHeight = defaultTileHeight;

        setCameraSpeed(cameraInitialSpeedFactor);
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    public Vector2i getCameraSpeed() {
        return cameraSpeed;
    }

    //-------------------------------------------------
    // Setter
    //-------------------------------------------------

    private void setCameraSpeed(int factor) {
        this.cameraSpeed.x = defaultTileWidth * factor;

        if (defaultTileHeight != 31) {
            this.cameraSpeed.y = defaultTileHeight * factor;
        } else {
            // the default tile height in GFX is 31, which must be corrected to 32
            this.cameraSpeed.y = xRaster * factor;
        }
    }
}
