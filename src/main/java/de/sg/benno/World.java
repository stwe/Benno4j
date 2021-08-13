/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno;

import de.sg.benno.chunk.Island5;
import de.sg.benno.chunk.WorldData;
import de.sg.benno.input.Camera;
import de.sg.benno.input.MousePicker;
import de.sg.benno.renderer.*;
import de.sg.benno.state.Context;
import de.sg.ogl.input.KeyInput;

import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;
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
     * The {@link Water} object.
     */
    private Water water;

    /**
     * The {@link Terrain} object.
     */
    private Terrain terrain;

    /**
     * The {@link MiniMap} of this world.
     */
    private MiniMap miniMap;

    /**
     * A {@link MousePicker} object to select tiles.
     */
    private final MousePicker mousePicker;

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
        this.camera = new Camera(0, 0, currentZoom);
        this.mousePicker = new MousePicker(this.context);

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
        //terrain = new Terrain(provider, context);

        // create and render minimap to a Fbo (creates a texture)
        //miniMap = new MiniMap(provider, context, camera);
        //miniMap.renderToFbo();
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
     *
     * @param dt The delta time.
     */
    public void update(float dt) {
        // wireframe flag
        if (KeyInput.isKeyPressed(GLFW_KEY_G)) {
            KeyInput.input(); // todo: a workaround
            wireframe = !wireframe;
        }

        // change zoom
        if (KeyInput.isKeyPressed(GLFW_KEY_1)) {
            currentZoom = Zoom.SGFX;
            camera.resetPosition(currentZoom);
        }

        if (KeyInput.isKeyPressed(GLFW_KEY_2)) {
            currentZoom = Zoom.MGFX;
            camera.resetPosition(currentZoom);
        }

        if (KeyInput.isKeyPressed(GLFW_KEY_3)) {
            currentZoom = Zoom.GFX;
            camera.resetPosition(currentZoom);
        }

        camera.update(currentZoom);

        //terrain.update(dt);
    }

    /**
     * Renders the world.
     */
    public void render() {
        water.render(camera, wireframe, currentZoom);
        //terrain.render(camera, wireframe, currentZoom);

        mousePicker.renderDebug(currentZoom, true, true);
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the World.");

        water.cleanUp();
        //terrain.cleanUp();
        //miniMap.cleanUp();
        mousePicker.cleanUp();
    }
}
