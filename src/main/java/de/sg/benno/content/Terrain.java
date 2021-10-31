/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021, stwe <https://github.com/stwe/Benno4j>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.sg.benno.content;

import de.sg.benno.BennoConfig;
import de.sg.benno.BennoRuntimeException;
import de.sg.benno.util.TileUtil;
import de.sg.benno.chunk.Island5;
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.chunk.WorldData;
import de.sg.benno.file.BennoFiles;
import de.sg.benno.input.Camera;
import de.sg.benno.ogl.physics.Aabb;
import de.sg.benno.renderer.IslandRenderer;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.*;

import static de.sg.benno.content.World.WORLD_HEIGHT;
import static de.sg.benno.content.World.WORLD_WIDTH;
import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents the terrain, i.e. all islands in the world.
 */
public class Terrain {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * Indicates that there is no island tile in one place.
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

    /**
     * Stores the instance number for every position in the world if there is an island5 tile there.
     * Otherwise there is a value of -1 {@link #NO_ISLAND}.
     */
    private final HashMap<Island5, ArrayList<Integer>> islandInstancesIndex = new HashMap<>();

    /**
     * Stores a 1 for the non-passable area; otherwise a 0.
     */
    private ArrayList<Integer> passableArea;

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
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #provider}.
     *
     * @return {@link #provider}
     */
    public WorldData getProvider() {
        return provider;
    }

    /**
     * Get a {@link TileGraphic} from a given {@link Island5}.
     *
     * @param zoom {@link Zoom}
     * @param island5 An {@link Island5}
     * @param x The x position in world space.
     * @param y The y position in world space.
     *
     * @return {@link Optional} of nullable {@link TileGraphic}
     */
    public Optional<TileGraphic> getTileGraphic(Zoom zoom, Island5 island5, int x, int y) {
        TileGraphic result = null;

        var index = getIsland5InstanceIndex(island5, x, y);
        if (index > NO_ISLAND) {
            result = islandTileGraphics.get(island5).get(zoom).get(index);
        }

        return Optional.ofNullable(result);
    }

    /**
     * Get {@link #passableArea}.
     *
     * @return {@link #passableArea}
     */
    public ArrayList<Integer> getPassableArea() {
        return passableArea;
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Update {@link #islandRenderer}.
     */
    public void update() {
        islandRenderer.forEach((k, v) -> v.update());
    }

    /**
     * Updates the selected flag at the given world position in {@link IslandRenderer}.
     * At the moment the color is getting darker.
     *
     * @param island5 An {@link Island5} object.
     * @param selected The x and y position of the tile in world space.
     *
     * @return True or false, depending on whether the tile has been changed successfully.
     */
    public boolean updateSelectedTile(Island5 island5, Vector2i selected) {
        var index = getIsland5InstanceIndex(island5, selected.x, selected.y);

        if (index > NO_ISLAND) {
            for (var zoom : Zoom.values()) {
                islandRenderer.get(island5).updateSelectedVbo(zoom, index);
            }
            return true;
        }

        return false;
    }

    /**
     * Renders the terrain.
     *
     * @param camera The {@link Camera} object.
     * @param wireframe True for wireframe rendering.
     * @param zoom The current {@link Zoom}.
     */
    public void render(Camera camera, boolean wireframe, Zoom zoom) {
        // only deep water:        fps 1900
        // + 17 islands:           fps 1400
        // + 17 islands and Aabbs: fps 1600

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

        var values = new Integer[WORLD_HEIGHT * WORLD_WIDTH];
        Arrays.fill(values, 0);
        passableArea = new ArrayList<>(Arrays.asList(values));

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

        // saves for each instance whether there is an island tile there
        // this is the same for every zoom and therefore only needs to be done once
        var addInstanceInfo = false;
        if (islandInstancesIndex.get(island5) == null) {
            var values = new Integer[WORLD_HEIGHT * WORLD_WIDTH];
            Arrays.fill(values, NO_ISLAND);
            islandInstancesIndex.put(island5, new ArrayList<>(Arrays.asList(values)));
            addInstanceInfo = true;
        }

        for (var y = island5.yPos; y < island5.yPos + island5.height; y++) {
            for (var x = island5.xPos; x < island5.xPos + island5.width; x++) {

                // get the tile from the top layer
                var island5TileOptional = island5.getTileFromTopLayer(x - island5.xPos, y - island5.yPos);

                // todo: write method
                // fallback to bottom layer
                if (island5TileOptional.isPresent() && island5TileOptional.get().getGraphicId() == 0xFFFF) {
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
                    var bshTexture = bshFile.getBshTextures().get(tileGraphic.gfxIndex);

                    // set world position
                    tileGraphic.worldPosition.x = x;
                    tileGraphic.worldPosition.y = y;

                    // calc screen position
                    var screenPosition = TileUtil.worldToScreen(x, y, zoom.getTileWidthHalf(), zoom.getTileHeightHalf());

                    // calc height correction
                    var adjustHeight = TileUtil.adjustHeight(zoom.getTileHeightHalf(), tileGraphic.tileHeight.value, zoom.getElevation());

                    // set screen position
                    screenPosition.y += adjustHeight;
                    screenPosition.x -= bshTexture.getWidth();
                    screenPosition.y -= bshTexture.getHeight();
                    tileGraphic.screenPosition = new Vector2f(screenPosition);

                    // set size and color
                    tileGraphic.size = new Vector2f(bshTexture.getWidth(), bshTexture.getHeight());
                    tileGraphic.color = new Vector3f();

                    tiles.add(tileGraphic);

                    // only needs to be done once
                    if (addInstanceInfo) {
                        var instancesIndex = islandInstancesIndex.get(island5);
                        instancesIndex.set(TileUtil.getIndexFrom2D(x, y), tiles.size() - 1);

                        // todo: use graphicId
                        if (tileGraphic.gfxIndex < 680 || tileGraphic.gfxIndex > 1051) {
                            passableArea.set(TileUtil.getIndexFrom2D(x, y), 1);
                        }
                    }
                } else {
                    throw new BennoRuntimeException("Missing tile at bottom layer.");
                }
            }
        }

        zoomTiles.put(zoom, tiles);
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Returns the instance index for the given {@link Island5} location.
     *
     * @param island5 The {@link Island5} object.
     * @param x The x position in world space.
     * @param y The y position in world space.
     *
     * @return {@link #NO_ISLAND} or the instance index.
     */
    private int getIsland5InstanceIndex(Island5 island5, int x, int y) {
        if (x < 0 || y < 0) {
            return NO_ISLAND;
        }

        return islandInstancesIndex.get(island5).get(TileUtil.getIndexFrom2D(x, y));
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
