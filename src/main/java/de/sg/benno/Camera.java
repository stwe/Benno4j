/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno;

import de.sg.benno.renderer.Zoom;
import de.sg.ogl.Log;
import de.sg.ogl.camera.OrthographicCamera;
import de.sg.ogl.input.KeyInput;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;

/**
 * Represents a orthographic camera.
 */
public class Camera extends OrthographicCamera {

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Camera} object.
     */
    public Camera() {
        Log.LOGGER.debug("Creates Camera object.");
    }

    /**
     * Constructs a new {@link Camera} object.
     *
     * @param position The start position in screen space.
     */
    public Camera(Vector2f position) {
        // todo: the given position should be in world space
        super(position);
   }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Update camera.
     *
     * @param zoom The current {@link Zoom}.
     */
    public void update(Zoom zoom) {
        if (KeyInput.isKeyPressed(GLFW.GLFW_KEY_W) || KeyInput.isKeyPressed(GLFW_KEY_UP)) {
            processKeyboard(Direction.UP, zoom);
        }

        if (KeyInput.isKeyPressed(GLFW.GLFW_KEY_S) || KeyInput.isKeyPressed(GLFW_KEY_DOWN)) {
            processKeyboard(Direction.DOWN, zoom);
        }

        if (KeyInput.isKeyPressed(GLFW.GLFW_KEY_A) || KeyInput.isKeyPressed(GLFW_KEY_LEFT)) {
            processKeyboard(Direction.LEFT, zoom);
        }

        if (KeyInput.isKeyPressed(GLFW.GLFW_KEY_D) || KeyInput.isKeyPressed(GLFW_KEY_RIGHT)) {
            processKeyboard(Direction.RIGHT, zoom);
        }

        if (KeyInput.isKeyPressed(GLFW.GLFW_KEY_I)) {
            LOGGER.info("Camera position x: {}, y: {}, current zoom: {}", position.x, position.y, zoom);
        }
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Handle keyboard input.
     *
     * @param direction {@link de.sg.ogl.camera.OrthographicCamera.Direction}
     * @param zoom {@link Zoom}
     */
    protected void processKeyboard(Direction direction, Zoom zoom) {
        if (direction == Direction.UP) {
            // the default tile height in GFX is 31, which must be corrected in some cases
            if (zoom == Zoom.GFX) {
                position.y += (zoom.defaultTileHeight + 1);
            } else {
                position.y += zoom.defaultTileHeight;
            }
        }

        if (direction == Direction.DOWN) {
            // the default tile height in GFX is 31, which must be corrected in some cases
            if (zoom == Zoom.GFX) {
                position.y -= (zoom.defaultTileHeight + 1);
            } else {
                position.y -= zoom.defaultTileHeight;
            }
        }

        if (direction == Direction.LEFT) {
            position.x -= zoom.defaultTileWidth;
        }

        if (direction == Direction.RIGHT) {
            position.x += zoom.defaultTileWidth;
        }
    }
}
