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
 * Represents a GamFile.
 * Loads a savegame.
 */
public class GamFile extends BinaryFile {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The map with all {@link Building} objects.
     */
    private final HashMap<Integer, Building> buildings;

    private final BshFile bshFile;
    private final Zoom zoom;

    /**
     * The list with all {@link Island5} objects.
     */
    private final ArrayList<Island5> island5List = new ArrayList<>();

    private final ArrayList<TileGraphic> deepWaterGraphicTiles = new ArrayList<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Contructs a new {@link GamFile} object.
     *
     * @param path The {@link Path} to the savegame.
     * @param buildings The map with all {@link Building} objects.
     * @throws IOException If an I/O error is thrown.
     */
    public GamFile(
            Path path,
            HashMap<Integer, Building> buildings,
            BshFile bshFile,
            Zoom zoom
    ) throws IOException {
        super(Objects.requireNonNull(path, "path must not be null"));

        LOGGER.debug("Creates GamFile object from file {}.", path);

        Objects.requireNonNull(buildings, "buildings must not be null");
        this.buildings = buildings;

        // todo - hier alle zoomstufen laden?
        this.bshFile = bshFile;
        this.zoom = zoom;

        readDataFromChunks();
    }

    //-------------------------------------------------
    // BinaryFileInterface
    //-------------------------------------------------

    @Override
    public void readDataFromChunks() throws IOException {
        LOGGER.debug("Start reading savegame data from Chunks...");

        for (var chunk : getChunks()) {
            if (chunk.getId().equals("INSEL5")) {
                var island5 = new Island5(chunk, buildings);
                island5List.add(island5);
            }

            if (chunk.getId().equals("INSELHAUS")) {
                var currentIsland5 = island5List.get(island5List.size() - 1);
                var islandHouse = new IslandHouse(chunk, currentIsland5);
                currentIsland5.addIslandHouse(islandHouse);
            }
        }

        initIsland5Layer();
        initDeepWaterArea();

        LOGGER.debug("Savegame data read successfully.");
    }

    //-------------------------------------------------
    // Layer
    //-------------------------------------------------

    /**
     * Initializes the layers of all {@link Island5} objects.
     * This determines which layer is top and which is bottom.
     */
    private void initIsland5Layer() {
        LOGGER.debug("Initialize layer.");

        for (var island5 : island5List) {
            island5.initLayer();
        }
    }

    //-------------------------------------------------
    // Deep water
    //-------------------------------------------------

    private void initDeepWaterArea() {
        LOGGER.debug("Create data for the DeepWaterRenderer.");

        createDeepWaterGraphicTiles();
    }

    private void createDeepWaterGraphicTiles() {
        for (int y = 0; y < WORLD_HEIGHT; y++) {
            for (int x = 0; x < WORLD_WIDTH; x++) {
                var island5 = isIslandOnPosition(x, y, island5List);

                if (island5 == null) {
                    var waterGfxIndex = buildings.get(Building.WATER_ID).gfx;
                    waterGfxIndex += (y + x * 3) % 12;

                    var deepWaterTileGraphic = new TileGraphic();
                    deepWaterTileGraphic.tileGfxInfo.gfxIndex = waterGfxIndex;
                    deepWaterTileGraphic.worldPosition.x = x;
                    deepWaterTileGraphic.worldPosition.y = y;

                    var waterBshTexture = bshFile.getBshTextures().get(waterGfxIndex);
                    var screenPosition = TileUtil.worldToScreen(x, y, zoom.xRaster, zoom.yRaster);
                    var adjustHeight = TileUtil.adjustHeight(zoom.yRaster, TileGraphic.TileHeight.SEA_LEVEL.value, zoom.elevation);

                    screenPosition.y += adjustHeight;

                    screenPosition.x -= waterBshTexture.getWidth();
                    screenPosition.y -= waterBshTexture.getHeight();

                    deepWaterTileGraphic.screenPosition = new Vector2f(screenPosition);
                    deepWaterTileGraphic.size = new Vector2f(waterBshTexture.getWidth(), waterBshTexture.getHeight());

                    deepWaterGraphicTiles.add(deepWaterTileGraphic);
                    // m_deepWaterIndex[chunk::TileUtil::GetIndexFrom2D(x, y)] = static_cast<int>(t_graphicTiles.size()) - 1;
                }
            }
        }
    }
}
