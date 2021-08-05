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
import de.sg.benno.file.BennoFiles;
import de.sg.benno.input.Camera;
import de.sg.benno.renderer.*;
import de.sg.benno.state.Context;

import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;

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
     * The {@link BennoFiles} object.
     */
    private final BennoFiles bennoFiles;

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

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link World} object.
     *
     * @param provider A {@link WorldData} object (e.g. {@link de.sg.benno.file.GamFile}).
     * @param context The {@link Context} object.
     * @param camera The {@link Camera} object.
     * @throws Exception If an error is thrown.
     */
    public World(WorldData provider, Context context, Camera camera) throws Exception {
        LOGGER.debug("Creates World object from provider class {}.", provider.getClass());

        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");
        this.camera = Objects.requireNonNull(camera, "camera must not be null");

        this.bennoFiles = this.context.bennoFiles;

        init();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #miniMap}.
     *
     * @return {@link #miniMap}
     */
    public MiniMap getMiniMap() {
        return miniMap;
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

        // create and render minimap to a Fbo (creates a texture)
        miniMap = new MiniMap(provider, context, camera);
        miniMap.renderToFbo();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Update world.
     *
     * @param dt The delta time.
     */
    public void update(float dt) {

    }

    /**
     * Renders the world.
     *
     * @param wireframe Boolean flag for wireframe rendering.
     * @param zoom The current {@link Zoom}.
     */
    public void render(boolean wireframe, Zoom zoom) {
        // render water
        water.render(camera, wireframe, zoom);

        // render terrain
        terrain.render(camera, wireframe, zoom);
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
        terrain.cleanUp();
        miniMap.cleanUp();

        // clean up passed data provider object (gamFile)
        provider.cleanUp();
    }
}
