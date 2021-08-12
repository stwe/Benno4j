/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.input;

import de.sg.benno.renderer.Zoom;
import de.sg.ogl.Config;
import de.sg.ogl.camera.OrthographicCamera;
import de.sg.ogl.input.KeyInput;
import de.sg.ogl.input.MouseInput;
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
     * The screen space position of the camera in tile units.
     */
    public final Vector2i positionInTileUnits = new Vector2i(0, 0);

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Camera} object.
     */
    public Camera(Zoom zoom) {
        LOGGER.debug("Creates Camera object at (0, 0) in screen space.");
        resetPosition(zoom);
    }

    /**
     * Constructs a new {@link Camera} object.
     *
     * @param x The start x screen space position in tile units.
     * @param y The start y screen space position in tile units.
     */
    public Camera(int x, int y, Zoom zoom) {
        LOGGER.debug("Creates Camera object at ({}, {}) in screen space.", x, y);

        positionInTileUnits.x = x;
        positionInTileUnits.y = y;

        resetPosition(zoom);
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Reset the camera {@link #position} in screen space after the {@link Zoom} was changed.
     *
     * @param zoom The current {@link Zoom}
     */
    public void resetPosition(Zoom zoom) {
        position.x = zoom.defaultTileWidth * positionInTileUnits.x;

        var height = zoom.defaultTileHeight;
        if (height == 31) {
            height = 32;
        }
        position.y = height * positionInTileUnits.y;
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Input camera.
     *
     * @param zoom {@link Zoom}
     */
    public void input(Zoom zoom) {
        // mouse input
        if (MouseInput.isMouseInWindow()) {
            var delta = 2.0f;
            if (MouseInput.isMouseButtonDown(GLFW_MOUSE_BUTTON_2)) { // right mouse button
                if (MouseInput.getDx() > delta) {
                    processUpdate(Direction.RIGHT, zoom);
                }

                if (MouseInput.getDx() < -delta) {
                    processUpdate(Direction.LEFT, zoom);
                }

                if (MouseInput.getDy() > delta) {
                    processUpdate(Direction.UP, zoom);
                }

                if (MouseInput.getDy() < -delta) {
                    processUpdate(Direction.DOWN, zoom);
                }
            }

            /*
            if (MouseInput.getX() < 30) {
                processUpdate(Direction.LEFT, zoom);
            }

            if (MouseInput.getX() > Config.WIDTH - 30) {
                processUpdate(Direction.RIGHT, zoom);
            }

            if (MouseInput.getY() < 30) {
                processUpdate(Direction.DOWN, zoom);
            }

            if (MouseInput.getY() > Config.HEIGHT - 30) {
                processUpdate(Direction.UP, zoom);
            }
            */
        }

        // key input

        if (KeyInput.isKeyDown(GLFW.GLFW_KEY_W) || KeyInput.isKeyDown(GLFW_KEY_UP)) {
            processUpdate(Direction.DOWN, zoom);
        }

        if (KeyInput.isKeyDown(GLFW.GLFW_KEY_S) || KeyInput.isKeyDown(GLFW_KEY_DOWN)) {
            processUpdate(Direction.UP, zoom);
        }

        if (KeyInput.isKeyDown(GLFW.GLFW_KEY_A) || KeyInput.isKeyDown(GLFW_KEY_LEFT)) {
            processUpdate(Direction.LEFT, zoom);
        }

        if (KeyInput.isKeyDown(GLFW.GLFW_KEY_D) || KeyInput.isKeyDown(GLFW_KEY_RIGHT)) {
            processUpdate(Direction.RIGHT, zoom);
        }
    }

    /**
     * Update camera.
     */
    public void update(float dt) {
        // todo reset? was removed from engine
        //MouseInput.input();
        //KeyInput.input();
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Update positions by given {@link de.sg.ogl.camera.OrthographicCamera.Direction}.
     *
     * @param direction {@link de.sg.ogl.camera.OrthographicCamera.Direction}
     * @param zoom {@link Zoom}
     */
    private void processUpdate(Direction direction, Zoom zoom) {
        if (direction == Direction.UP) {
            positionInTileUnits.y += zoom.speedFactor;
            position.y += zoom.getCameraSpeed().y;
        }

        if (direction == Direction.DOWN) {
            positionInTileUnits.y -= zoom.speedFactor;
            position.y -= zoom.getCameraSpeed().y;
        }

        if (direction == Direction.LEFT) {
            positionInTileUnits.x -= zoom.speedFactor;
            position.x -= zoom.getCameraSpeed().x;
        }

        if (direction == Direction.RIGHT) {
            positionInTileUnits.x += zoom.speedFactor;
            position.x += zoom.getCameraSpeed().x;
        }
    }
}
