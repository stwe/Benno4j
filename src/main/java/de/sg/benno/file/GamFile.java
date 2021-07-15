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
import de.sg.benno.renderer.Zoom;
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
     * {@link ArrayList} objects holding deep water {@link TileGraphic} for each {@link Zoom} level.
     */
    private final HashMap<Zoom, ArrayList<TileGraphic>> deepWaterTiles = new HashMap<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Contructs a new {@link GamFile} object.
     *
     * @param path The {@link Path} to the savegame.
     * @param bennoFiles The {@link BennoFiles} object.
     * @throws IOException If an I/O error is thrown.
     */
    public GamFile(Path path, BennoFiles bennoFiles) throws IOException {
        super(Objects.requireNonNull(path, "path must not be null"));

        LOGGER.debug("Creates GamFile object from file {}.", path);

        this.bennoFiles = Objects.requireNonNull(bennoFiles, "bennoFiles must not be null");
        this.buildings = this.bennoFiles.getDataFiles().getBuildings();

        readDataFromChunks();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link BennoFiles}.
     *
     * @return {@link BennoFiles}
     */
    public BennoFiles getBennoFiles() {
        return bennoFiles;
    }

    /**
     * Get {@link #deepWaterTiles}.
     *
     * @return {@link #deepWaterTiles}
     */
    public HashMap<Zoom, ArrayList<TileGraphic>> getDeepWaterTiles() {
        return deepWaterTiles;
    }

    /**
     * Get an {@link ArrayList} with {@link TileGraphic} objects of the deep water area.
     *
     * @param zoom {@link Zoom}
     *
     * @return An {@link ArrayList} with {@link TileGraphic} objects.
     */
    public ArrayList<TileGraphic> getDeepWaterTiles(Zoom zoom) {
        return deepWaterTiles.get(zoom);
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

        // set top and bottom layer
        initIsland5Layer();

        // create deep water tiles for each zoom level
        createDeepWaterAreaTiles();

        LOGGER.debug("Savegame data read successfully.");
    }

    //-------------------------------------------------
    // Layer
    //-------------------------------------------------

    /**
     * Initializes the layers of all {@link Island5} objects.
     * Sets the top and bottom layer.
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
     * Creates deep water tiles for each zoom level.
     */
    private void createDeepWaterAreaTiles() throws IOException {
        LOGGER.debug("Create tiles for the DeepWaterRenderer.");

        createDeepWaterGraphicTiles(Zoom.GFX);
        createDeepWaterGraphicTiles(Zoom.MGFX);
        createDeepWaterGraphicTiles(Zoom.SGFX);
    }

    /**
     * Create deep water {@link TileGraphic} objects for a given {@link Zoom}.
     *
     * @param zoom {@link Zoom}
     */
    private void createDeepWaterGraphicTiles(Zoom zoom) throws IOException {
        // it is possible to preload the files in BennoFiles
        var bshFile = this.bennoFiles.getBshFile(this.bennoFiles.getZoomableBshFilePath(
                zoom, BennoFiles.ZoomableBshFileName.STADTFLD_BSH
        ));

        var tiles = new ArrayList<TileGraphic>();

        for (int y = 0; y < WORLD_HEIGHT; y++) {
            for (int x = 0; x < WORLD_WIDTH; x++) {
                var island5 = isIslandOnPosition(x, y, island5List);

                if (island5.isEmpty()) {
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
}
