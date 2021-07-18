/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.file;

import de.sg.benno.TileUtil;
import de.sg.benno.chunk.Island5;
import de.sg.benno.chunk.IslandHouse;
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.data.Building;
import de.sg.benno.renderer.WaterRenderer;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import de.sg.ogl.camera.OrthographicCamera;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static de.sg.benno.World.*;
import static de.sg.ogl.Log.LOGGER;

/**
 * Loads a savegame (.gam file).
 */
public class GamFile extends BinaryFile {

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
     * The list with all {@link Island5} objects.
     */
    private final ArrayList<Island5> island5List = new ArrayList<>();

    /**
     * Water {@link TileGraphic} objects for each {@link Zoom} level.
     */
    private final HashMap<Zoom, ArrayList<TileGraphic>> deepWaterTiles = new HashMap<>();

    /**
     * The {@link WaterRenderer} objects to render water {@link TileGraphic}.
     */
    private final HashMap<Zoom, WaterRenderer> waterRenderers = new HashMap<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Contructs a new {@link GamFile} object.
     *
     * @param path The {@link Path} to the savegame.
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public GamFile(Path path, Context context) throws Exception {
        super(Objects.requireNonNull(path, "path must not be null"));

        LOGGER.debug("Creates GamFile object from file {}.", path);

        this.context = Objects.requireNonNull(context, "context must not be null");

        this.bennoFiles = this.context.bennoFiles;
        this.buildings = this.bennoFiles.getDataFiles().getBuildings();

        readDataFromChunks();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Renders the savegame.
     *
     * @param camera The {@link OrthographicCamera} object.
     * @param wireframe Boolean flag for wireframe rendering.
     * @param zoom {@link Zoom}
     */
    public void render(OrthographicCamera camera, boolean wireframe, Zoom zoom) {
        waterRenderers.get(zoom).render(camera, wireframe);
    }

    //-------------------------------------------------
    // BinaryFileInterface
    //-------------------------------------------------

    @Override
    public void readDataFromChunks() throws Exception {
        LOGGER.debug("Start reading savegame data from Chunks...");

        for (var chunk : getChunks()) {
            if (chunk.getId().equals("INSEL5")) {
                var island5 = new Island5(chunk, bennoFiles);
                island5List.add(island5);
            }

            if (chunk.getId().equals("INSELHAUS")) {
                var currentIsland5 = island5List.get(island5List.size() - 1);
                var islandHouse = new IslandHouse(chunk, currentIsland5);
                currentIsland5.addIslandHouse(islandHouse);
            }
        }

        // set top and bottom layer for each Island5
        initIsland5Layer();

        // init water tiles and renderer
        initDeepWaterRenderer();

        LOGGER.debug("Savegame data read successfully.");
    }

    //-------------------------------------------------
    // Layer
    //-------------------------------------------------

    /**
     * Initializes the layers of all {@link Island5} objects.
     * Sets the final top and bottom layer.
     *
     * @throws IOException If an I/O error is thrown.
     */
    private void initIsland5Layer() throws IOException {
        LOGGER.debug("Initialize top and bottom layer for each island.");

        for (var island5 : island5List) {
            island5.setTopAndBottomLayer();
        }
    }

    //-------------------------------------------------
    // Deep water
    //-------------------------------------------------

    /**
     * Creates the {@link WaterRenderer} objects.
     *
     * @throws Exception If an error is thrown.
     */
    private void initDeepWaterRenderer() throws Exception {
        LOGGER.debug("Start init WaterRenderers...");

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
            createWaterGraphicTiles(zoom, 1203);
        }

        LOGGER.debug("The WaterRenderers have been successfully initialized and created.");
    }

    /**
     * Create water {@link TileGraphic} objects for a given {@link Zoom}.
     *
     * @param zoom {@link Zoom}
     * @param buildingId A building Id for water area. An Id can refer to several textures for animation.
     * @throws Exception If an error is thrown.
     */
    private void createWaterGraphicTiles(Zoom zoom, int buildingId) throws Exception {
        LOGGER.debug("Create water tiles for {}.", zoom.toString());

        var bshFile = this.bennoFiles.getBshFile(this.bennoFiles.getZoomableBshFilePath(
                zoom, BennoFiles.ZoomableBshFileName.STADTFLD_BSH
        ));

        // to store all deep water tiles
        var tiles = new ArrayList<TileGraphic>();

        // get building
        var water = buildings.get(buildingId);

        // create tiles
        for (int y = 0; y < WORLD_HEIGHT; y++) {
            for (int x = 0; x < WORLD_WIDTH; x++) {
                var isWater = isIslandOnPosition(x, y, island5List).isEmpty();
                if (isWater) {
                    var deepWaterTile = new TileGraphic();
                    deepWaterTile.tileGfxInfo.gfxIndex = water.gfx;
                    deepWaterTile.worldPosition.x = x;
                    deepWaterTile.worldPosition.y = y;

                    var waterBshTexture = bshFile.getBshTextures().get(water.gfx);
                    var screenPosition = TileUtil.worldToScreen(x, y, zoom.xRaster, zoom.yRaster);
                    var adjustHeight = TileUtil.adjustHeight(zoom.yRaster, TileGraphic.TileHeight.SEA_LEVEL.value, zoom.elevation);

                    screenPosition.y += adjustHeight;

                    screenPosition.x -= waterBshTexture.getWidth();
                    screenPosition.y -= waterBshTexture.getHeight();

                    deepWaterTile.screenPosition = new Vector2f(screenPosition);
                    deepWaterTile.size = new Vector2f(waterBshTexture.getWidth(), waterBshTexture.getHeight());

                    tiles.add(deepWaterTile);
                }
            }
        }

        deepWaterTiles.put(zoom, tiles);

        createWaterRenderer(zoom, water);
    }

    /**
     * Creates a {@link WaterRenderer} object for a given {@link Zoom}.
     *
     * @param zoom {@link Zoom}
     * @param building The water {@link Building}.
     * @throws Exception If an error is thrown.
     */
    private void createWaterRenderer(Zoom zoom, Building building) throws Exception {
        ArrayList<Matrix4f> modelMatrices = new ArrayList<>();

        for (var tile : deepWaterTiles.get(zoom)) {
            modelMatrices.add(tile.getModelMatrix());
        }

        waterRenderers.put(zoom, new WaterRenderer(modelMatrices, building, context, zoom));
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the GamFile.");

        waterRenderers.forEach((k, v) -> v.cleanUp());
    }
}
