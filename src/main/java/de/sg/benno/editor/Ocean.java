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

package de.sg.benno.editor;

import de.sg.benno.BennoConfig;
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.chunk.WaterTile;
import de.sg.benno.data.Building;
import de.sg.benno.file.BennoFiles;
import de.sg.benno.input.Camera;
import de.sg.benno.renderer.WaterRenderer;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import de.sg.benno.util.TileUtil;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static de.sg.benno.content.World.WORLD_HEIGHT;
import static de.sg.benno.content.World.WORLD_WIDTH;
import static de.sg.benno.ogl.Log.LOGGER;

/**
 * This class represents the deep water area of the world, which is used by the editor.
 */
public class Ocean {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

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
     * A {@link WaterRenderer} object for each {@link Zoom}.
     */
    private final HashMap<Zoom, WaterRenderer> waterRenderers = new HashMap<>();

    /**
     * To store the gfx start index. Animations do not always start with the first gfx.
     * 0 = use first texture, 1 = use second texture, etc.
     */
    private ArrayList<Integer> waterGfxStartIndex;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Ocean} object.
     *
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public Ocean(Context context) throws Exception {
        this.context = Objects.requireNonNull(context, "context must not be null");

        this.bennoFiles = this.context.bennoFiles;
        this.buildings = this.bennoFiles.getDataFiles().getBuildings();

        init();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Renders a non-animated ocean.
     *
     * @param camera The {@link Camera} object.
     * @param wireframe True for wireframe rendering.
     * @param zoom The current {@link Zoom}.
     */
    public void render(Camera camera, boolean wireframe, Zoom zoom) {
        waterRenderers.get(zoom).render(camera, wireframe, false);
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
        LOGGER.debug("Start init Ocean...");

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
     * @param zoom {@link Zoom}.
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

        // create water tile graphics
        for (int y = 0; y < WORLD_HEIGHT; y++) {
            for (int x = 0; x < WORLD_WIDTH; x++) {
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
            }
        }

        waterTileGraphics.put(zoom, tileGraphics);

        createWaterRenderer(zoom, waterBuilding);
    }

    /**
     * Creates a {@link WaterRenderer} object for a given {@link Zoom}.
     *
     * @param zoom {@link Zoom}.
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
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the Ocean.");

        waterRenderers.forEach((k, v) -> v.cleanUp());
    }
}
