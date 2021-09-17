/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl.camera;

import org.joml.Vector2i;

/**
 * Represents a CameraZoom.
 */
public interface CameraZoom {
    int getTileWidthHalf();
    int getTileHeightHalf();
    int getElevation();
    int getTileWidth();
    int getTileHeight();
    int getSpeedFactor();
    Vector2i getCameraSpeed();
}
