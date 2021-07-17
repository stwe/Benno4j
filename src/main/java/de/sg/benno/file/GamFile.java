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
import static de.sg.benno.renderer.WaterRenderer.START_WATER_GFX_INDEX;
import static de.sg.ogl.Log.LOGGER;

/**
 * Loads a savegame (.gam file).
 */
public class GamFile extends BinaryFile {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * No water value for {@link #deepWaterIndex}.
     */
    private static final int NO_DEEP_WATER = -1;

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
     * Many {@link ArrayList} objects holding deep water {@link TileGraphic} for each {@link Zoom} level.
     */
    private final HashMap<Zoom, ArrayList<TileGraphic>> deepWaterTiles = new HashMap<>();

    /**
     * Stores for every position in the world whether there is a deep water tile there.
     */
    private final ArrayList<Integer> deepWaterIndex = new ArrayList<>();

    /**
     * The {@link WaterRenderer} object to render deep water {@link TileGraphic}.
     */
    private WaterRenderer waterRenderer;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Contructs a new {@link GamFile} object.
     *
     * @param path The {@link Path} to the savegame.
     * @param context The {@link Context} object.
     * @throws IOException If an I/O error is thrown.
     */
    public GamFile(Path path, Context context) throws IOException {
        super(Objects.requireNonNull(path, "path must not be null"));

        LOGGER.debug("Creates GamFile object from file {}.", path);

        this.context = Objects.requireNonNull(context, "context must not be null");
        this.bennoFiles = Objects.requireNonNull(context.bennoFiles, "bennoFiles must not be null");
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
     */
    public void render(OrthographicCamera camera, boolean wireframe) {
        waterRenderer.render(camera, wireframe);
    }

    //-------------------------------------------------
    // BinaryFileInterface
    //-------------------------------------------------

    @Override
    public void readDataFromChunks() throws IOException {
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

        // init the WaterRenderer
        // todo add Exception instead IOException to the interface
        try {
            initDeepWaterRenderer();
        } catch (Exception e) {
            e.printStackTrace();
        }

        LOGGER.debug("Savegame data read successfully.");
    }

    //-------------------------------------------------
    // Layer
    //-------------------------------------------------

    /**
     * Initializes the layers of all {@link Island5} objects.
     * Sets the final top and bottom layer.
     */
    private void initIsland5Layer() throws IOException {
        LOGGER.debug("Initialize layer.");

        for (var island5 : island5List) {
            island5.setTopAndBottomLayer();
        }
    }

    //-------------------------------------------------
    // Deep water
    //-------------------------------------------------

    /**
     * Create the {@link WaterRenderer}.
     *
     * @throws Exception If an error is thrown.
     */
    private void initDeepWaterRenderer() throws Exception {
        LOGGER.debug("Start init WaterRenderer...");

        createDeepWaterGraphicTiles(Zoom.GFX);
        //createDeepWaterGraphicTiles(Zoom.MGFX);
        //createDeepWaterGraphicTiles(Zoom.SGFX);

        // todo: all zoom levels
        ArrayList<Matrix4f> modelMatrices = new ArrayList<>();
        ArrayList<Integer> textureIds = new ArrayList<>();
        LOGGER.debug("Buffer model matrices and texture Ids.");
        for (var tile : deepWaterTiles.get(Zoom.GFX)) {
            modelMatrices.add(tile.getModelMatrix());
            textureIds.add(tile.tileGfxInfo.gfxIndex - START_WATER_GFX_INDEX);
        }

        waterRenderer = new WaterRenderer(modelMatrices, textureIds, context, Zoom.GFX);

        LOGGER.debug("The WaterRenderer was created successfully.");
    }

    /**
     * Create deep water {@link TileGraphic} objects for a given {@link Zoom}.
     *
     * @param zoom {@link Zoom}
     */
    private void createDeepWaterGraphicTiles(Zoom zoom) throws IOException {
        LOGGER.debug("Create deep water tiles for {}.", zoom.toString());

        // it is possible to preload the files in BennoFiles
        var bshFile = this.bennoFiles.getBshFile(this.bennoFiles.getZoomableBshFilePath(
                zoom, BennoFiles.ZoomableBshFileName.STADTFLD_BSH
        ));

        // to store all deep water tiles
        var tiles = new ArrayList<TileGraphic>();

        // not every tile in the world is a water tile
        // todo: für jede Zoomstufe gleich -> muss nur 1x ausgeführt werden
        for (var i = 0; i < WORLD_WIDTH * WORLD_HEIGHT; i++) {
            deepWaterIndex.add(i, NO_DEEP_WATER);
        }

        // create tiles
        for (int y = 0; y < WORLD_HEIGHT; y++) {
            for (int x = 0; x < WORLD_WIDTH; x++) {
                var isWater = isIslandOnPosition(x, y, island5List).isEmpty();
                if (isWater) {
                    var waterGfxIndex = buildings.get(Building.WATER_ID).gfx;
                    waterGfxIndex += (y + x * 3) % 12;

                    var deepWaterTile = new TileGraphic();
                    deepWaterTile.tileGfxInfo.gfxIndex = waterGfxIndex;
                    deepWaterTile.worldPosition.x = x;
                    deepWaterTile.worldPosition.y = y;

                    var waterBshTexture = bshFile.getBshTextures().get(waterGfxIndex);
                    var screenPosition = TileUtil.worldToScreen(x, y, zoom.xRaster, zoom.yRaster);
                    var adjustHeight = TileUtil.adjustHeight(zoom.yRaster, TileGraphic.TileHeight.SEA_LEVEL.value, zoom.elevation);

                    screenPosition.y += adjustHeight;

                    screenPosition.x -= waterBshTexture.getWidth();
                    screenPosition.y -= waterBshTexture.getHeight();

                    deepWaterTile.screenPosition = new Vector2f(screenPosition);
                    deepWaterTile.size = new Vector2f(waterBshTexture.getWidth(), waterBshTexture.getHeight());

                    tiles.add(deepWaterTile);

                    deepWaterIndex.add(TileUtil.getIndexFrom2D(x, y), tiles.size() - 1);
                }
            }
        }

        deepWaterTiles.put(zoom, tiles);
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    public void cleanUp() {
        LOGGER.debug("Start clean up for the GamFile.");

        waterRenderer.cleanUp();
    }
}
