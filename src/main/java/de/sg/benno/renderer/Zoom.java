/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.renderer;

/**
 * Represents a zoom level with the respective constants.
 */
public enum Zoom {
    SGFX(8, 4 ,4 ,16, 8),
    MGFX(16, 8, 2, 32, 16),
    GFX(32, 16, 1, 64, 31);

    public int xRaster;
    public int yRaster;
    public int elevation;
    public int defaultTileWidth;
    public int defaultTileHeight;

    Zoom(int xRaster, int yRaster, int elevation, int defaultTileWidth, int defaultTileHeight) {
        this.xRaster = xRaster;
        this.yRaster = yRaster;
        this.elevation = elevation;
        this.defaultTileWidth = defaultTileWidth;
        this.defaultTileHeight = defaultTileHeight;
    }
}
