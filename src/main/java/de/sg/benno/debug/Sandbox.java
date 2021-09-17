/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.debug;

import de.sg.benno.BennoConfig;
import de.sg.benno.content.Shipping;
import de.sg.benno.content.Water;
import de.sg.benno.chunk.Island5;
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.chunk.WorldData;
import de.sg.benno.input.Camera;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;

import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_3;

/**
 * Represents a Sandbox.
 */
public class Sandbox {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * Provides data for drawing. For example the {@link Island5} objects from a loaded GAM file.
     */
    private final WorldData provider;

    /**
     * The {@link Context} object.
     */
    private final Context context;

    /**
     * The {@link Camera} object.
     */
    private final Camera camera;

    /**
     * The current {@link Zoom}.
     */
    private Zoom currentZoom = Zoom.GFX;

    /**
     * The {@link Water} object with the deep water area.
     */
    private Water water;

    /**
     * The {@link Shipping} object manages all ships.
     */
    private Shipping shipping;

    /**
     * A {@link MousePicker} object to select tiles.
     */
    private MousePicker mousePicker;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Sandbox} object.
     *
     * @param provider A {@link WorldData} object (e.g. {@link de.sg.benno.file.GamFile}).
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public Sandbox(WorldData provider, Context context) throws Exception {
        LOGGER.debug("Creates Sandbox object from provider class {}.", provider.getClass());

        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");

        if (BennoConfig.ZOOM_START >= 1 && BennoConfig.ZOOM_START <= 3) {
            currentZoom = Zoom.values()[BennoConfig.ZOOM_START - 1];
        }

        this.camera = new Camera(BennoConfig.CAMERA_START_X, BennoConfig.CAMERA_START_Y, context.engine, currentZoom);

        init();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #camera}.
     *
     * @return {@link #camera}
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * Get {@link #currentZoom}.
     *
     * @return {@link #currentZoom}
     */
    public Zoom getCurrentZoom() {
        return currentZoom;
    }

    /**
     * Get {@link #shipping}.
     *
     * @return {@link #shipping}
     */
    public Shipping getShipping() {
        return shipping;
    }

    /**
     * Get {@link #mousePicker}.
     *
     * @return {@link #mousePicker}
     */
    public MousePicker getMousePicker() {
        return mousePicker;
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Initialize sandbox content.
     *
     * @throws Exception If an error is thrown.
     */
    private void init() throws Exception {
        // create water
        water = new Water(provider, context);

        // create shipping
        shipping = new Shipping(provider, context);

        // the mouse picker
        mousePicker = new MousePicker(context, water, shipping, TileGraphic.TileHeight.SEA_LEVEL);
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Nothing
     */
    public void input() {}

    /**
     * Update sandbox.
     */
    public void update() {
        // change zoom
        if (context.engine.getWindow().isKeyPressed(GLFW_KEY_1)) {
            currentZoom = Zoom.SGFX;
            camera.resetPosition(currentZoom);
        }

        if (context.engine.getWindow().isKeyPressed(GLFW_KEY_2)) {
            currentZoom = Zoom.MGFX;
            camera.resetPosition(currentZoom);
        }

        if (context.engine.getWindow().isKeyPressed(GLFW_KEY_3)) {
            currentZoom = Zoom.GFX;
            camera.resetPosition(currentZoom);
        }

        camera.update(context.engine.getWindow(), context.engine.getMouseInput(), currentZoom);
        shipping.update();
        mousePicker.update(camera, currentZoom);
    }

    /**
     * Renders the sandbox.
     */
    public void render() {
        water.render(camera, false, currentZoom);
        shipping.render(camera, currentZoom);
        mousePicker.render(camera, currentZoom);
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the Sandbox.");

        camera.cleanUp();
        water.cleanUp();
        shipping.cleanUp();
        mousePicker.cleanUp();
    }
}
