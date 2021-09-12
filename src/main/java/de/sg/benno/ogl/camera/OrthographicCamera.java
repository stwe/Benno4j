/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl.camera;

import de.sg.benno.ogl.OglEngine;
import de.sg.benno.ogl.Window;
import de.sg.benno.ogl.input.MouseInput;
import de.sg.benno.ogl.physics.Aabb;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.*;

/**
 * Represents a OrthographicCamera.
 */
public class OrthographicCamera {

    //-------------------------------------------------
    // Types
    //-------------------------------------------------

    /**
     * Possible camera directions.
     */
    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The camera position in screen space.
     */
    public Vector2f position = new Vector2f();

    /**
     * The position of the camera in tile units.
     */
    public final Vector2i positionInTileUnits = new Vector2i();

    /**
     * The camera {@link Aabb}.
     */
    private final Aabb aabb;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link OrthographicCamera} object.
     *
     * @param x The start x position in tile units.
     * @param y The start y position in tile units.
     * @param engine The parent {@link OglEngine} object.
     * @param zoom The current {@link CameraZoom}.
     * @throws Exception If an error is thrown.
     */
    public OrthographicCamera(int x, int y, OglEngine engine, CameraZoom zoom) throws Exception {
        LOGGER.debug("Creates orthographic camera at ({}, {}).", x, y);

        this.positionInTileUnits.x = x;
        this.positionInTileUnits.y = y;

        this.aabb = new Aabb(Objects.requireNonNull(engine, "engine must not be null"));

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

    /**
     * Recalculates and get the view matrix.
     *
     * @return {@link Matrix4f}
     */
    public Matrix4f getViewMatrix() {
        Matrix4f transformMatrix = new Matrix4f();
        transformMatrix
                .identity()
                .translate(new Vector3f(position, 0.0f))
                .scale(new Vector3f(1.0f));

        return transformMatrix.invert();
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Reset the camera {@link #position} in screen space after the {@link CameraZoom} was changed.
     *
     * @param zoom The current {@link CameraZoom}
     */
    public void resetPosition(CameraZoom zoom) {
        position.x = zoom.getTileWidth() * positionInTileUnits.x;
        position.y = (zoom.getTileHeightHalf() * 2) * positionInTileUnits.y; // GFX height is 31 instead 32

        aabb.position = position;
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Updates the camera.
     *
     * @param window The {@link Window} to get the key input.
     * @param mouseInput The {@link MouseInput} object.
     * @param zoom The current {@link CameraZoom}
     */
    public void update(Window window, MouseInput mouseInput, CameraZoom zoom) {
        handleKeyInput(window, zoom);
        handleMouseInput(mouseInput, zoom);
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Handle key input.
     *
     * @param window The {@link Window} to get the key input.
     * @param zoom The current {@link CameraZoom}
     */
    private void handleKeyInput(Window window, CameraZoom zoom) {
        if (window.isKeyPressed(GLFW.GLFW_KEY_W) || window.isKeyPressed(GLFW_KEY_UP)) {
            processUpdate(Direction.DOWN, zoom);
        }

        if (window.isKeyPressed(GLFW.GLFW_KEY_S) || window.isKeyPressed(GLFW_KEY_DOWN)) {
            processUpdate(Direction.UP, zoom);
        }

        if (window.isKeyPressed(GLFW.GLFW_KEY_A) || window.isKeyPressed(GLFW_KEY_LEFT)) {
            processUpdate(Direction.LEFT, zoom);
        }

        if (window.isKeyPressed(GLFW.GLFW_KEY_D) || window.isKeyPressed(GLFW_KEY_RIGHT)) {
            processUpdate(Direction.RIGHT, zoom);
        }
    }

    /**
     * Handle mouse input.
     *
     * @param mouseInput The {@link MouseInput} object.
     * @param zoom The current {@link CameraZoom}
     */
    private void handleMouseInput(MouseInput mouseInput, CameraZoom zoom) {
        if (mouseInput.isInWindow()) {
            var delta = 2.0f;

            if (mouseInput.isRightButtonPressed()) {
                if (mouseInput.getDisplVec().x > delta) {
                    processUpdate(Direction.RIGHT, zoom);
                }

                if (mouseInput.getDisplVec().x < -delta) {
                    processUpdate(Direction.LEFT, zoom);
                }

                if (mouseInput.getDisplVec().y > delta) {
                    processUpdate(Direction.UP, zoom);
                }

                if (mouseInput.getDisplVec().y < -delta) {
                    processUpdate(Direction.DOWN, zoom);
                }
            }
        }
    }

    /**
     * Update positions by given {@link Direction}.
     *
     * @param direction {@link Direction}
     * @param zoom {@link CameraZoom}
     */
    private void processUpdate(Direction direction, CameraZoom zoom) {
        if (direction == Direction.UP) {
            positionInTileUnits.y += zoom.getSpeedFactor();
            position.y += zoom.getCameraSpeed().y;
        }

        if (direction == Direction.DOWN) {
            positionInTileUnits.y -= zoom.getSpeedFactor();
            position.y -= zoom.getCameraSpeed().y;
        }

        if (direction == Direction.LEFT) {
            positionInTileUnits.x -= zoom.getSpeedFactor();
            position.x -= zoom.getCameraSpeed().x;
        }

        if (direction == Direction.RIGHT) {
            positionInTileUnits.x += zoom.getSpeedFactor();
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
        LOGGER.debug("Start clean up for the OrthographicCamera.");

        aabb.cleanUp();
    }
}
