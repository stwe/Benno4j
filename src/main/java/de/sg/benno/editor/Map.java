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

package de.sg.benno.editor;

import de.sg.benno.BennoConfig;
import de.sg.benno.input.Camera;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;

import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.*;

/**
 * Represents a Map.
 */
public class Map {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link Context} object.
     */
    private final Context context;

    /**
     * The current {@link Zoom}.
     */
    private Zoom currentZoom = Zoom.GFX;

    /**
     * The {@link Camera} object.
     */
    private final Camera camera;

    /**
     * The {@link Ocean} object represents the deep water area.
     */
    private final Ocean ocean;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Map} object.
     *
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public Map(Context context) throws Exception {
        LOGGER.debug("Creates Map object");

        this.context = Objects.requireNonNull(context, "context must not be null");

        if (BennoConfig.ZOOM_START >= 1 && BennoConfig.ZOOM_START <= 3) {
            currentZoom = Zoom.values()[BennoConfig.ZOOM_START - 1];
        }

        this.camera = new Camera(BennoConfig.CAMERA_START_X, BennoConfig.CAMERA_START_Y, context.engine, currentZoom);
        this.ocean = new Ocean(context);
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #currentZoom}.
     *
     * @return {@link #currentZoom}
     */
    public Zoom getCurrentZoom() {
        return currentZoom;
    }

    /**
     * Get {@link #camera}.
     *
     * @return {@link #camera}
     */
    public Camera getCamera() {
        return camera;
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Input map.
     */
    public void input() {
    }

    /**
     * Update map.
     */
    public void update() {
        if (context.engine.getWindow().isKeyPressed(GLFW_KEY_1)) {
            changeZoomTo(Zoom.SGFX);
        }

        if (context.engine.getWindow().isKeyPressed(GLFW_KEY_2)) {
            changeZoomTo(Zoom.MGFX);
        }

        if (context.engine.getWindow().isKeyPressed(GLFW_KEY_3)) {
            changeZoomTo(Zoom.GFX);
        }

        camera.update(context.engine.getWindow(), context.engine.getMouseInput(), currentZoom);
    }

    /**
     * Render map.
     */
    public void render() {
        ocean.render(camera, false, currentZoom);
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Reinitialize stuff when the zoom has changed.
     *
     * @param zoom The new {@link Zoom}.
     */
    private void changeZoomTo(Zoom zoom) {
        currentZoom = zoom;
        camera.resetPosition(currentZoom);
    }

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the Map.");

        camera.cleanUp();
        ocean.cleanUp();
    }
}
