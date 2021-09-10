/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.input;

import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

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

    /**
     * The current {@link Aabb}.
     */
    private final Aabb aabb;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Camera} object.
     *
     * @param x The start x screen space position in tile units.
     * @param y The start y screen space position in tile units.
     * @param context {@link Context}
     * @param zoom {@link Zoom}
     * @throws Exception If an error is thrown.
     */
    public Camera(int x, int y, Context context, Zoom zoom) throws Exception {
        LOGGER.debug("Creates Camera object at ({}, {}) in screen space.", x, y);

        this.positionInTileUnits.x = x;
        this.positionInTileUnits.y = y;

        this.aabb = new Aabb(context);

        resetPosition(zoom);
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #aabb}.
     *
     * @return {@link #aabb}
     */
    public Aabb getAabb() {
        return aabb;
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

        aabb.position = position;
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Update camera.
     */
    public void update(Zoom zoom) {
        // todo reset? was removed from engine
        //MouseInput.input();
        //KeyInput.input();

        handleKeyInput(zoom);
        handleMouseInput(zoom);
    }

    /**
     * Handle key input.
     *
     * @param zoom {@link Zoom}
     */
    private void handleKeyInput(Zoom zoom) {
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
     * Handle mouse input.
     *
     * @param zoom {@link Zoom}
     */
    private void handleMouseInput(Zoom zoom) {
        if (MouseInput.isMouseInWindow()) {
            var delta = 2.0f;

            // right mouse button
            if (MouseInput.isMouseButtonDown(GLFW_MOUSE_BUTTON_2)) {
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

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the Camera.");

        aabb.cleanUp();
    }
}
