/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno;

import de.sg.benno.chunk.Island5;
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.chunk.WorldData;
import de.sg.benno.input.Camera;
import de.sg.benno.input.MousePicker;
import de.sg.benno.renderer.*;
import de.sg.benno.state.Context;
import org.joml.Vector2f;

import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_3;

/**
 * Represents a complete game world.
 */
public class World {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * The width of the world in tiles.
     */
    public static final int WORLD_WIDTH = 500;

    /**
     * The height of the world in tiles.
     */
    public static final int WORLD_HEIGHT = 350;

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
     * Enable and disable wireframe mode.
     */
    private boolean wireframe = false;

    /**
     * The {@link Water} object with the deep water area.
     */
    private Water water;

    /**
     * The {@link Terrain} object contains all islands.
     */
    private Terrain terrain;

    /**
     * The {@link Shipping} object manages all ships.
     */
    private Shipping shipping;

    /**
     * The {@link MiniMap} of this world.
     */
    private MiniMap miniMap;

    /**
     * A {@link MousePicker} object to select tiles.
     */
    private MousePicker mousePicker;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link World} object.
     *
     * @param provider A {@link WorldData} object (e.g. {@link de.sg.benno.file.GamFile}).
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public World(WorldData provider, Context context) throws Exception {
        LOGGER.debug("Creates World object from provider class {}.", provider.getClass());

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
     * Get {@link #miniMap}.
     *
     * @return {@link #miniMap}
     */
    public MiniMap getMiniMap() {
        return miniMap;
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
    // Setter
    //-------------------------------------------------

    /**
     * Set {@link #currentZoom}.
     *
     * @param currentZoom {@link #currentZoom}
     */
    public void setCurrentZoom(Zoom currentZoom) {
        this.currentZoom = currentZoom;
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Initialize world content.
     *
     * @throws Exception If an error is thrown.
     */
    private void init() throws Exception {
        // create water
        water = new Water(provider, context);

        // create terrain
        terrain = new Terrain(provider, context);

        // create shipping
        shipping = new Shipping(provider, context);

        // create minimap
        miniMap = new MiniMap(provider, context, camera, currentZoom);

        // the mouse picker
        mousePicker = new MousePicker(context, water, terrain, shipping, TileGraphic.TileHeight.SEA_LEVEL);
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Nothing
     */
    public void input() {}

    /**
     * Update world.
     */
    public void update() {
        // wireframe flag
        if (context.engine.getWindow().isKeyPressed(GLFW_KEY_G)) {
            wireframe = !wireframe;
        }

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
        //water.update();
        terrain.update();
        shipping.update();
        miniMap.update(currentZoom);
        mousePicker.update(camera, currentZoom);
    }

    /**
     * Renders the world.
     */
    public void render() {
        water.render(camera, wireframe, currentZoom);
        terrain.render(camera, wireframe, currentZoom);
        shipping.render(camera, currentZoom);
        miniMap.render(new Vector2f(0.55f, -0.9f), new Vector2f(0.4f));
        mousePicker.render(camera, currentZoom);
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the World.");

        camera.cleanUp();
        water.cleanUp();
        terrain.cleanUp();
        shipping.cleanUp();
        miniMap.cleanUp();
        mousePicker.cleanUp();
    }
}
