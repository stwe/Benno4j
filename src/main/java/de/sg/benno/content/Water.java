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
import de.sg.benno.ai.Astar;
import de.sg.benno.chunk.*;
import de.sg.benno.util.TileUtil;
import de.sg.benno.data.Building;
import de.sg.benno.file.BennoFiles;
import de.sg.benno.input.Camera;
import de.sg.benno.renderer.WaterRenderer;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;

import static de.sg.benno.content.World.WORLD_HEIGHT;
import static de.sg.benno.content.World.WORLD_WIDTH;
import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents Water.
 */
public class Water {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * Indicates that there is no water tile in one place.
     */
    public static final int NO_WATER = -1;

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
     * The map with all {@link Building} objects.
     */
    private final HashMap<Integer, Building> buildings;

    /**
     * Water {@link TileGraphic} objects for each {@link Zoom} level.
     */
    private final HashMap<Zoom, ArrayList<TileGraphic>> waterTileGraphics = new HashMap<>();

    /**
     * Stores the instance number for every position in the world if there is a water tile there.
     * Otherwise, there is a value of -1 {@link #NO_WATER}.
     */
    private ArrayList<Integer> waterInstancesIndex;

    /**
     * Stores a 0 for every position in the world if it is passable for a ship, otherwise a 1.
     */
    private ArrayList<Integer> passableArea;

    /**
     * To store the gfx start index. Animations do not always start with the first gfx.
     * 0 = use first texture, 1 = use second texture, etc.
     */
    private ArrayList<Integer> waterGfxStartIndex;

    /**
     * A {@link WaterRenderer} object for each {@link Zoom}.
     */
    private final HashMap<Zoom, WaterRenderer> waterRenderers = new HashMap<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Water} object.
     *
     * @param provider A {@link WorldData} object (e.g. {@link de.sg.benno.file.GamFile}).
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public Water(WorldData provider, Context context) throws Exception {
        LOGGER.debug("Creates Water object.");

        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");

        this.bennoFiles = this.context.bennoFiles;
        this.buildings = this.bennoFiles.getDataFiles().getBuildings();

        init();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #passableArea}.
     *
     * @return {@link #passableArea}
     */
    public ArrayList<Integer> getPassableArea() {
        return passableArea;
    }

    /**
     * Get a {@link TileGraphic} by a given world space position.
     *
     * @param zoom The current {@link Zoom}.
     * @param x The x position in world space.
     * @param y The y position in world space.
     *
     * @return A nullable {@link TileGraphic} Optional.
     */
    public Optional<TileGraphic> getWaterTileGraphic(Zoom zoom, int x, int y) {
        TileGraphic result = null;

        var index = getWaterInstanceIndex(x, y);
        if (index != NO_WATER) {
            result = waterTileGraphics.get(zoom).get(index);
        }

        return Optional.ofNullable(result);
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Renders the water.
     *
     * @param camera The {@link Camera} object.
     * @param wireframe True for wireframe rendering.
     * @param zoom The current {@link Zoom}.
     */
    public void render(Camera camera, boolean wireframe, Zoom zoom) {
        waterRenderers.get(zoom).render(camera, wireframe);
    }

    /**
     * Updates the selected flag at the given world position.
     * At the moment the color is getting darker.
     *
     * @param x The x position in world space.
     * @param y The y position in world space.
     *
     * @return True or false, depending on whether the water tile graphic has been changed successfully.
     */
    public boolean updateSelectedWaterTile(int x, int y) {
        var index = getWaterInstanceIndex(x, y);
        if (index != NO_WATER) {

            // update for CPU
            for (var zoom : Zoom.values()) {
                var tileGraphic = waterTileGraphics.get(zoom).get(index);
                tileGraphic.selected = true;
            }

            // update for GPU
            waterRenderers.forEach((k, v) -> v.updateSelectedVbo(index));

            return true;
        }

        return false;
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Initializes the class.
     *
     * @throws Exception If an error is thrown.
     */
    private void init() throws Exception {
        LOGGER.debug("Start init Water...");

        /*
        2622 (Ruine):    673    Rot: 0   AnimAnz:  -   AnimAdd:  -
        1383 (Wald):     674    Rot: 0   AnimAnz:  5   AnimAdd:  1
               ?         679
        ----------------------------------------------------------
        1253 (Meer):     680    Rot: 0   AnimAnz:  6   AnimAdd:  1
        1203 (Meer):     686    Rot: 0   AnimAnz:  6   AnimAdd:  1
        1252 (Meer):     692    Rot: 1   AnimAnz:  6   AnimAdd:  4
        1202 (Meer):     716    Rot: 1   AnimAnz:  6   AnimAdd:  4
        1254 (Meer):     740    Rot: 0   AnimAnz:  6   AnimAdd:  1
        1204 (Meer):     746    Rot: 0   AnimAnz:  6   AnimAdd:  1
        1251 (Meer):     752    Rot: 0   AnimAnz:  6   AnimAdd:  1
        1201 (Meer):     758    Rot: 0   AnimAnz:  6   AnimAdd:  1
        1259 (Meer):     764    Rot: 1   AnimAnz:  6   AnimAdd:  4
        1209 (Meer):     788    Rot: 1   AnimAnz:  6   AnimAdd:  4
        ----------------------------------------------------------
        1205 (Brandung)  812
        */

        for (var zoom : Zoom.values()) {
            createWaterGraphicTiles(zoom, BennoConfig.DEEP_WATER_BUILDING_ID);
        }

        LOGGER.debug("The initialization process was completed successfully.");
    }

    /**
     * Create water {@link TileGraphic} objects for a given {@link Zoom}.
     *
     * @param zoom {@link Zoom}
     * @param buildingId The building ID for each water tile. An ID can refer to several gfx for animation.
     * @throws Exception If an error is thrown.
     */
    private void createWaterGraphicTiles(Zoom zoom, int buildingId) throws Exception {
        LOGGER.debug("Create water tiles for {}.", zoom.toString());

        // get the BSH file for the given zoom
        var bshFile = bennoFiles.getStadtfldBshFile(zoom);

        // to store all water tile graphics
        var tileGraphics = new ArrayList<TileGraphic>();

        // get building data for the given ID
        var waterBuilding = buildings.get(buildingId);

        // get texture (gfx) of the building ID to get the width and height later
        var waterBshTexture = bshFile.getBshTextures().get(waterBuilding.gfx);

        // calc adjust height
        var adjustHeight = TileUtil.adjustHeight(zoom.getTileHeightHalf(), TileGraphic.TileHeight.SEA_LEVEL.value, zoom.getElevation());

        // saves for each instance whether there is a water tile there
        // this is the same for every zoom and therefore only needs to be done once
        var addInstanceInfo = false;
        if (waterInstancesIndex == null) {
            // store NO_WATER for each position in the world by default
            var values = new Integer[WORLD_HEIGHT * WORLD_WIDTH];
            Arrays.fill(values, NO_WATER);
            waterInstancesIndex = new ArrayList<>(Arrays.asList(values));

            // each position in the world is passable by a ship by default
            var passable = new Integer[WORLD_HEIGHT * WORLD_WIDTH];
            Arrays.fill(passable, Astar.PASSABLE);
            passableArea = new ArrayList<>(Arrays.asList(passable));

            addInstanceInfo = true;
        }

        // create water tile graphics
        for (int y = 0; y < WORLD_HEIGHT; y++) {
            for (int x = 0; x < WORLD_WIDTH; x++) {
                // create a TileGraphic only if there is no island at the world position
                var isWater = Island5.isIsland5OnPosition(x, y, provider.getIsland5List()).isEmpty();
                if (isWater) {
                    var waterTileGraphic = new TileGraphic();

                    waterTileGraphic.parentTile = new WaterTile(buildingId);

                    waterTileGraphic.gfxIndex = waterBuilding.gfx;
                    waterTileGraphic.tileHeight = TileGraphic.TileHeight.SEA_LEVEL;
                    waterTileGraphic.worldPosition.x = x;
                    waterTileGraphic.worldPosition.y = y;

                    var screenPosition = TileUtil.worldToScreen(x, y, zoom.getTileWidthHalf(), zoom.getTileHeightHalf());
                    screenPosition.y += adjustHeight;
                    screenPosition.x -= waterBshTexture.getWidth();
                    screenPosition.y -= waterBshTexture.getHeight();

                    waterTileGraphic.screenPosition = new Vector2f(screenPosition);
                    waterTileGraphic.size = new Vector2f(waterBshTexture.getWidth(), waterBshTexture.getHeight());
                    waterTileGraphic.color = new Vector3f();

                    tileGraphics.add(waterTileGraphic);

                    // only needs to be done once
                    if (addInstanceInfo) {
                        // All water tile graphics are stored in tileGraphics.
                        // If there is a water tile graphic at a position in the world,
                        // the position from tileGraphics is saved in waterInstancesIndex.
                        waterInstancesIndex.set(TileUtil.getIndexFrom2D(x, y), tileGraphics.size() - 1);

                        /*
                        TileUtil.getIndexFrom2D(x, y)
                                               ||
                                               ||
                                               \/
                            waterInstancesIndex[0] -> -1 = no water
                            waterInstancesIndex[1] -> -1 = no water
                            waterInstancesIndex[2] -> last element in tileGraphics (tileGraphics.size() - 1) if there is a water tile
                            waterInstancesIndex[3] -> -1 = no water
                        */
                    }
                } else {
                    // 1 = not passable for a ship (island)
                    passableArea.set(TileUtil.getIndexFrom2D(x, y), Astar.OBSTACLE);
                }
            }
        }

        waterTileGraphics.put(zoom, tileGraphics);

        createWaterRenderer(zoom, waterBuilding);
    }

    /**
     * Creates a {@link WaterRenderer} object for a given {@link Zoom}.
     *
     * @param zoom {@link Zoom}
     * @param building The water {@link Building}.
     * @throws Exception If an error is thrown.
     */
    private void createWaterRenderer(Zoom zoom, Building building) throws Exception {
        LOGGER.debug("Create water renderer for {}.", zoom.toString());

        // store a model matrix for each water tile graphic
        ArrayList<Matrix4f> modelMatrices = new ArrayList<>();
        for (var tileGraphic : waterTileGraphics.get(zoom)) {
            modelMatrices.add(tileGraphic.getModelMatrix());
        }

        // create only once
        // store a value [0 ... building.animAnz - 1] for each water tile graphic as start gfx for animation
        if (waterGfxStartIndex == null) {
            waterGfxStartIndex = new ArrayList<>();
            for (var tileGraphic : waterTileGraphics.get(zoom)) {
                waterGfxStartIndex.add((tileGraphic.worldPosition.y + tileGraphic.worldPosition.x * 3) % building.animAnz);
            }
        }

        // create and store renderer
        waterRenderers.put(zoom, new WaterRenderer(
                modelMatrices,
                Objects.requireNonNull(waterGfxStartIndex, "waterGfxStartIndex must not be null"),
                building,
                context,
                zoom
        ));
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Returns the instance index for the given location.
     *
     * @param x The x position in world space.
     * @param y The y position in world space.
     *
     * @return {@link #NO_WATER} or the instance index.
     */
    private int getWaterInstanceIndex(int x, int y) {
        if (x < 0 || y < 0) {
            return NO_WATER;
        }

        var index = TileUtil.getIndexFrom2D(x, y);
        if (index > waterInstancesIndex.size()) {
            return NO_WATER;
        }

        return waterInstancesIndex.get(index);
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the Water.");

        waterRenderers.forEach((k, v) -> v.cleanUp());
    }
}
