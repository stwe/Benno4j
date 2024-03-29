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

package de.sg.benno.renderer;

import de.sg.benno.BennoConfig;
import de.sg.benno.ogl.camera.CameraZoom;
import org.joml.Vector2i;

/**
 * Represents a zoom level with the respective constants.
 */
public enum Zoom implements CameraZoom {

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

    /**
     * The tile width half.
     */
    private final int tileWidthHalf;

    /**
     * The tile height half.
     */
    private final int tileHeightHalf;

    /**
     * The tile elevation.
     */
    private final int elevation;

    /**
     * The tile width.
     */
    private final int tileWidth;

    /**
     * The tile height.
     */
    private final int tileHeight;

    /**
     * The configured speed factor.
     */
    private final int speedFactor;

    /**
     * The camera speed.
     */
    private final Vector2i cameraSpeed = new Vector2i();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Zoom}.
     *
     * @param tileWidthHalf The tile width half.
     * @param tileHeightHalf The tile height half.
     * @param elevation The tile elevation.
     * @param tileWidth The tile width.
     * @param tileHeight The tile height.
     * @param speedFactor The camera speed.
     */
    Zoom(
            int tileWidthHalf, int tileHeightHalf,
            int elevation,
            int tileWidth, int tileHeight,
            int speedFactor
    ) {
        this.tileWidthHalf = tileWidthHalf;
        this.tileHeightHalf = tileHeightHalf;
        this.elevation = elevation;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.speedFactor = speedFactor;

        calcCameraSpeed();
    }

    //-------------------------------------------------
    // Implement CameraZoom
    //-------------------------------------------------

    @Override
    public int getTileWidthHalf() {
        return tileWidthHalf;
    }

    @Override
    public int getTileHeightHalf() {
        return tileHeightHalf;
    }

    @Override
    public int getElevation() {
        return elevation;
    }

    @Override
    public int getTileWidth() {
        return tileWidth;
    }

    @Override
    public int getTileHeight() {
        return tileHeight;
    }

    @Override
    public int getSpeedFactor() {
        return speedFactor;
    }

    @Override
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
        this.cameraSpeed.x = tileWidth * speedFactor;
        this.cameraSpeed.y = (tileHeightHalf * 2) * speedFactor; // GFX height is 31, which must be corrected to 32
    }
}
