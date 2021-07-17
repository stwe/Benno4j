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
     * @throws IOException If an I/O error is thrown.
     */
    public GamFile(Path path, Context context) throws Exception {
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
     * Creates the {@link WaterRenderer} objects.
     *
     * @throws Exception If an error is thrown.
     */
    private void initDeepWaterRenderer() throws Exception {
        LOGGER.debug("Start init WaterRenderers...");

        createWaterGraphicTiles(Zoom.GFX);
        createWaterGraphicTiles(Zoom.MGFX);
        createWaterGraphicTiles(Zoom.SGFX);

        createWaterRenderer(Zoom.GFX);
        createWaterRenderer(Zoom.MGFX);
        createWaterRenderer(Zoom.SGFX);

        LOGGER.debug("The WaterRenderers have been successfully initialized and created.");
    }

    /**
     * Create water {@link TileGraphic} objects for a given {@link Zoom}.
     *
     * @param zoom {@link Zoom}
     */
    private void createWaterGraphicTiles(Zoom zoom) throws IOException {
        LOGGER.debug("Create water tiles for {}.", zoom.toString());

        // it is possible to preload the files in BennoFiles
        var bshFile = this.bennoFiles.getBshFile(this.bennoFiles.getZoomableBshFilePath(
                zoom, BennoFiles.ZoomableBshFileName.STADTFLD_BSH
        ));

        // to store all deep water tiles
        var tiles = new ArrayList<TileGraphic>();

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
                }
            }
        }

        deepWaterTiles.put(zoom, tiles);
    }

    /**
     * Creates a {@link WaterRenderer} object for a given {@link Zoom}.
     *
     * @param zoom {@link Zoom}
     * @throws Exception If an error is thrown.
     */
    private void createWaterRenderer(Zoom zoom) throws Exception {
        ArrayList<Matrix4f> modelMatrices = new ArrayList<>();
        ArrayList<Integer> textureIds = new ArrayList<>();

        for (var tile : deepWaterTiles.get(zoom)) {
            modelMatrices.add(tile.getModelMatrix());
            textureIds.add(tile.tileGfxInfo.gfxIndex - START_WATER_GFX_INDEX);
        }

        waterRenderers.put(zoom, new WaterRenderer(modelMatrices, textureIds, context, zoom));
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the GamFile.");

        waterRenderers.get(Zoom.GFX).cleanUp();
        waterRenderers.get(Zoom.MGFX).cleanUp();
        waterRenderers.get(Zoom.SGFX).cleanUp();
    }
}
