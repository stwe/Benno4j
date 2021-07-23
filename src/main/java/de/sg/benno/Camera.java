/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno;

import de.sg.benno.renderer.Zoom;
import de.sg.ogl.camera.OrthographicCamera;
import de.sg.ogl.input.KeyInput;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;

/**
 * Represents a orthographic camera.
 */
public class Camera extends OrthographicCamera {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The origin or startposition of the camera.
     */
    public final Vector2i origin = new Vector2i(0, 0);

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Camera} object.
     */
    public Camera(Zoom zoom) {
        LOGGER.debug("Creates Camera object at (0, 0) in world space.");
        resetPosition(zoom);
    }

    /**
     * Constructs a new {@link Camera} object.
     *
     * @param x The start x position in world space.
     * @param y The start y position in world space.
     */
    public Camera(int x, int y, Zoom zoom) {
        LOGGER.debug("Creates Camera object at ({}, {}) in world space.", x, y);

        origin.x = x;
        origin.y = y;

        resetPosition(zoom);
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Reset the camera position after the {@link Zoom} was changed.
     *
     * @param zoom The current {@link Zoom}
     */
    public void resetPosition(Zoom zoom) {
        position.x = zoom.defaultTileWidth * origin.x;

        var height = zoom.defaultTileHeight;
        if (height == 31) {
            height = 32;
        }
        position.y = height * origin.y;
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
            origin.y += zoom.speedFactor;
            position.y += zoom.getCameraSpeed().y;
        }

        if (direction == Direction.DOWN) {
            origin.y -= zoom.speedFactor;
            position.y -= zoom.getCameraSpeed().y;
        }

        if (direction == Direction.LEFT) {
            origin.x -= zoom.speedFactor;
            position.x -= zoom.getCameraSpeed().x;
        }

        if (direction == Direction.RIGHT) {
            origin.x += zoom.speedFactor;
            position.x += zoom.getCameraSpeed().x;
        }
    }
}
