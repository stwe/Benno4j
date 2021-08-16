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
import de.sg.benno.file.BennoFiles;
import de.sg.benno.input.Aabb;
import de.sg.benno.input.Camera;
import de.sg.benno.renderer.IslandRenderer;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;

/**
 * Represents the terrain, i.e. all islands in the world.
 */
public class Terrain {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * Indicates that there is no island tile in one place.
     * todo: evtl. kann der Index aus der Water class benutzt werden
     */
    public static final int NO_ISLAND = -1;

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
     * The {@link BennoFiles} object.
     */
    private final BennoFiles bennoFiles;

    /**
     * {@link TileGraphic} objects of each {@link Island5} and in each {@link Zoom} level.
     */
    private final HashMap<Island5, HashMap<Zoom, ArrayList<TileGraphic>>> islandTileGraphics = new HashMap<>();

    /**
     * A {@link IslandRenderer} for each {@link Island5}.
     */
    private final HashMap<Island5, IslandRenderer> islandRenderer = new HashMap<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Terrain} object.
     *
     * @param provider A {@link WorldData} object (e.g. {@link de.sg.benno.file.GamFile}).
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public Terrain(WorldData provider, Context context) throws Exception {
        LOGGER.debug("Creates Terrain object.");

        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");

        this.bennoFiles = this.context.bennoFiles;

        init();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Update {@link #islandRenderer}.
     *
     * @param dt The delta time.
     */
    public void update(float dt) {
        islandRenderer.forEach((k, v) -> v.update(dt));
    }

    /**
     * Renders the terrain.
     *
     * @param camera The {@link Camera} object.
     * @param wireframe True for wireframe rendering.
     * @param zoom The current {@link Zoom}.
     */
    public void render(Camera camera, boolean wireframe, Zoom zoom) {
        // only deep water:        1900
        // + 17 islands:           1400
        // + 17 islands and Aabbs: 1600

        if (!BennoConfig.AABB_COLLISION_DETECTION) {
            islandRenderer.forEach(
                    (k, v) -> {
                        v.render(camera, wireframe, zoom);
                        if (BennoConfig.SHOW_ISLAND5_AABBS) {
                            k.getAabb(zoom).render(camera);
                        }
                    }
            );
        }

        if (BennoConfig.AABB_COLLISION_DETECTION) {
            islandRenderer.forEach(
                    (k, v) -> {
                        if (Aabb.aabbVsAabb(camera.getAabb(), k.getAabb(zoom))) {
                            v.render(camera, wireframe, zoom);
                            if (BennoConfig.SHOW_ISLAND5_AABBS) {
                                k.getAabb(zoom).render(camera);
                            }
                        }
                    }
            );
        }
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Initializes the terrain.
     *
     * @throws Exception If an error is thrown.
     */
    private void init() throws Exception {
        LOGGER.debug("Start init Terrain...");

        for (var island5 : provider.getIsland5List()) {
            var zoomTiles = new HashMap<Zoom, ArrayList<TileGraphic>>();
            for (var zoom : Zoom.values()) {
                createGraphicTiles(island5, zoom, zoomTiles);
            }

            islandTileGraphics.put(island5, zoomTiles);
        }

        for (var island5 : provider.getIsland5List()) {
            var tiles = islandTileGraphics.get(island5);
            islandRenderer.put(island5, new IslandRenderer(tiles, context));
        }

        LOGGER.debug("The initialization process was completed successfully.");
    }

    /**
     * Create {@link TileGraphic} objects for a given {@link Zoom}.
     *
     * @param island5 The {@link Island5} from which the {@link TileGraphic} objects are created.
     * @param zoom {@link Zoom}
     * @param zoomTiles To store the generated {@link TileGraphic} objects.
     * @throws Exception If an error is thrown.
     */
    private void createGraphicTiles(Island5 island5, Zoom zoom, HashMap<Zoom, ArrayList<TileGraphic>> zoomTiles) throws Exception {
        var bshFile = this.bennoFiles.getStadtfldBshFile(zoom);
        var tiles = new ArrayList<TileGraphic>();

        for (var y = island5.yPos; y < island5.yPos + island5.height; y++) {
            for (var x = island5.xPos; x < island5.xPos + island5.width; x++) {

                // get the tile from the top layer
                var island5TileOptional = island5.getTileFromTopLayer(x - island5.xPos, y - island5.yPos);

                // todo: write method
                // fallback to bottom layer
                if (island5TileOptional.isPresent() && island5TileOptional.get().graphicId == 0xFFFF) {
                    island5TileOptional = island5.getTileFromBottomLayer(x - island5.xPos, y - island5.yPos);
                }

                if (island5TileOptional.isPresent()) {
                    var island5Tile = island5TileOptional.get();

                    // create tile to display (TileGraphic)
                    var tileGraphic = new TileGraphic();

                    // set island5 tile as parent
                    tileGraphic.parentTile = island5Tile;

                    // work out and set gfx index and tile height (Cliff or Sea-level)
                    island5.setGfxInfo(island5Tile, tileGraphic);

                    // get bsh texture by gfx index
                    var bshTexture = bshFile.getBshTextures().get(tileGraphic.gfx);

                    // set world position
                    tileGraphic.worldPosition.x = x;
                    tileGraphic.worldPosition.y = y;

                    // calc screen position
                    var screenPosition = TileUtil.worldToScreen(x, y, zoom.defaultTileWidthHalf, zoom.defaultTileHeightHalf);

                    // calc height correction
                    var adjustHeight = TileUtil.adjustHeight(zoom.defaultTileHeightHalf, tileGraphic.tileHeight.value, zoom.elevation);

                    // set screen position
                    screenPosition.y += adjustHeight;
                    screenPosition.x -= bshTexture.getWidth();
                    screenPosition.y -= bshTexture.getHeight();
                    tileGraphic.screenPosition = new Vector2f(screenPosition);

                    // set size and color
                    tileGraphic.size = new Vector2f(bshTexture.getWidth(), bshTexture.getHeight());
                    tileGraphic.color = new Vector3f();

                    tiles.add(tileGraphic);
                } else {
                    throw new BennoRuntimeException("Missing tile at bottom layer.");
                }
            }
        }

        zoomTiles.put(zoom, tiles);
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the Terrain.");

        islandRenderer.forEach((k, v) -> v.cleanUp());
    }
}
