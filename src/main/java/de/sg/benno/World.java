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
import de.sg.benno.data.Building;
import de.sg.benno.file.BennoFiles;
import de.sg.benno.file.BshFile;
import de.sg.benno.renderer.*;
import de.sg.benno.state.Context;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
     * The {@link Camera} object.
     */
    private final Camera camera;

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
    private final HashMap<Zoom, ArrayList<TileGraphic>> waterTiles = new HashMap<>();

    /**
     * Stores the instance number for every position in the world if there is a water tile there.
     * Otherwise there is a value of -1 {@link #NO_WATER}.
     */
    private ArrayList<Integer> waterInstancesIndex;

    /**
     * To store the gfx start index. Animations do not always start with the first gfx.
     * 0 = use first texture, 1 = use second texture, etc.
     */
    private ArrayList<Integer> waterGfxStartIndex;

    /**
     * The {@link WaterRenderer} objects to render water for each {@link Zoom}.
     */
    private final HashMap<Zoom, WaterRenderer> waterRenderers = new HashMap<>();

    /**
     * The {@link MiniMap} of this world.
     */
    private MiniMap miniMap;


    // todo: new member

    private final HashMap<Zoom, ArrayList<TileGraphic>> shipTiles = new HashMap<>();
    private TileGraphicRenderer tileGraphicRenderer;
    private BshFile shipBshFile;

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
        this.buildings = this.bennoFiles.getDataFiles().getBuildings();

        init();
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Init object for each {@link Zoom}.
     *
     * @throws Exception If an error is thrown.
     */
    private void init() throws Exception {
        // todo tmp code
        shipBshFile = this.bennoFiles.getShipBshFile(Zoom.GFX);
        tileGraphicRenderer = new TileGraphicRenderer(context);
        initShips(Zoom.GFX);

        // to render deep water
        initWaterRenderer();

        // create minimap
        miniMap = new MiniMap(provider, context);
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Update object.
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
        // render deep water
        waterRenderers.get(zoom).render(camera, wireframe);

        // render texture with the mininmap
        miniMap.render(new Vector2f(0.4f, -0.3f), new Vector2f(0.5f, 0.5f));

        // todo tmp code 93, 240
        var t = shipTiles.get(Zoom.GFX).get(0);
        tileGraphicRenderer.render(camera, t, shipBshFile);
    }

    /**
     * Updates the selected flag at the given world position in each {@link WaterRenderer}.
     * At the moment the color is getting darker.
     *
     * @param selected The x and y position of the tile in world space.
     */
    public void updateSelectedWaterTile(Vector2i selected) {
        var index = getWaterInstanceIndex(selected.x, selected.y);
        if (index > NO_WATER) {
            waterRenderers.forEach((k, v) -> v.updateSelectedVbo(index));
        }
    }

    //-------------------------------------------------
    // Ship
    //-------------------------------------------------

    void initShips(Zoom zoom) throws IOException {
        LOGGER.debug("Create ship tiles for {}.", zoom.toString());

        var ship = provider.getShips4List().get(0);

        var shipBshTexture = shipBshFile.getBshTextures().get(ship.gfx);

        var water = buildings.get(BennoConfig.DEEP_WATER_BUILDING_ID);
        var bshFile = this.bennoFiles.getStadtfldBshFile(zoom);
        var waterBshTexture = bshFile.getBshTextures().get(water.gfx);

        var adjustHeight = TileUtil.adjustHeight(zoom.defaultTileHeightHalf, TileGraphic.TileHeight.SEA_LEVEL.value, zoom.elevation);

        var tile = new TileGraphic();
        tile.gfx = ship.gfx;
        tile.tileHeight = TileGraphic.TileHeight.SEA_LEVEL;
        tile.worldPosition.x = ship.xPos;
        tile.worldPosition.y = ship.yPos;

        var screenPosition = TileUtil.worldToScreen(ship.xPos, ship.yPos, zoom.defaultTileWidthHalf, zoom.defaultTileHeightHalf);
        //screenPosition.y += adjustHeight;
        //screenPosition.x -= waterBshTexture.getWidth();
        //screenPosition.y -= waterBshTexture.getHeight();

        tile.screenPosition = new Vector2f(screenPosition);
        tile.size = new Vector2f(shipBshTexture.getWidth(), shipBshTexture.getHeight());
        tile.color = new Vector3f();

        var tiles = new ArrayList<TileGraphic>();
        tiles.add(tile);

        shipTiles.put(zoom, tiles);

        camera.position = new Vector2f(tile.screenPosition);
    }

    //-------------------------------------------------
    // Water
    //-------------------------------------------------

    /**
     * Creates the {@link WaterRenderer} objects.
     *
     * @throws Exception If an error is thrown.
     */
    private void initWaterRenderer() throws Exception {
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
            createWaterGraphicTiles(zoom, BennoConfig.DEEP_WATER_BUILDING_ID);
        }

        LOGGER.debug("The WaterRenderers have been successfully initialized and created.");
    }

    /**
     * Create water {@link TileGraphic} objects for a given {@link Zoom}.
     *
     * @param zoom {@link Zoom}
     * @param buildingId The building Id for each water tile. An Id can refer to several textures for animation.
     * @throws Exception If an error is thrown.
     */
    private void createWaterGraphicTiles(Zoom zoom, int buildingId) throws Exception {
        LOGGER.debug("Create water tiles for {}.", zoom.toString());

        // get the BSH file for the given zoom
        var bshFile = this.bennoFiles.getStadtfldBshFile(zoom);

        // to store all water tiles
        var tiles = new ArrayList<TileGraphic>();

        // get building data for the given Id
        var water = buildings.get(buildingId);

        // get building texture to get the width and height
        var waterBshTexture = bshFile.getBshTextures().get(water.gfx);

        // calc adjust height
        var adjustHeight = TileUtil.adjustHeight(zoom.defaultTileHeightHalf, TileGraphic.TileHeight.SEA_LEVEL.value, zoom.elevation);

        // saves for each instance whether there is a water tile there
        // this is the same for every zoom and therefore only needs to be done once
        var addInstanceInfo = false;
        if (waterInstancesIndex == null) {
            var values = new Integer[WORLD_HEIGHT * WORLD_WIDTH];
            Arrays.fill(values, NO_WATER);
            waterInstancesIndex = new ArrayList<>(Arrays.asList(values));
            addInstanceInfo = true;
        }

        // create water tiles
        for (int y = 0; y < WORLD_HEIGHT; y++) {
            for (int x = 0; x < WORLD_WIDTH; x++) {
                // only consider deep water tiles here
                var isWater = Island5.isIslandOnPosition(x, y, provider.getIsland5List()).isEmpty();
                if (isWater) {
                    var waterTile = new TileGraphic();
                    waterTile.gfx = 0; // is not needed here because the same gfx is always used
                    waterTile.tileHeight = TileGraphic.TileHeight.SEA_LEVEL;
                    waterTile.worldPosition.x = x;
                    waterTile.worldPosition.y = y;

                    var screenPosition = TileUtil.worldToScreen(x, y, zoom.defaultTileWidthHalf, zoom.defaultTileHeightHalf);
                    screenPosition.y += adjustHeight;
                    screenPosition.x -= waterBshTexture.getWidth();
                    screenPosition.y -= waterBshTexture.getHeight();

                    waterTile.screenPosition = new Vector2f(screenPosition);
                    waterTile.size = new Vector2f(waterBshTexture.getWidth(), waterBshTexture.getHeight());
                    waterTile.color = new Vector3f();

                    tiles.add(waterTile);

                    // only needs to be done once
                    if (addInstanceInfo) {
                        waterInstancesIndex.set(TileUtil.getIndexFrom2D(x, y), tiles.size() - 1);
                    }
                }
            }
        }

        waterTiles.put(zoom, tiles);

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

        for (var tile : waterTiles.get(zoom)) {
            modelMatrices.add(tile.getModelMatrix());
        }

        // create only once
        if (waterGfxStartIndex == null) {
            waterGfxStartIndex = new ArrayList<>();
            for (var tile : waterTiles.get(zoom)) {
                waterGfxStartIndex.add((tile.worldPosition.y + tile.worldPosition.x * 3) % building.animAnz);
            }
        }

        waterRenderers.put(zoom, new WaterRenderer(
                modelMatrices,
                Objects.requireNonNull(waterGfxStartIndex, "waterGfxStartIndex must not be null"),
                building,
                context,
                zoom
        ));
    }

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

        return waterInstancesIndex.get(TileUtil.getIndexFrom2D(x, y));
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the World.");

        waterRenderers.forEach((k, v) -> v.cleanUp());
        tileGraphicRenderer.cleanUp();
        miniMap.cleanUp();

        // clean up passed data provider object (gamFile)
        provider.cleanUp();
    }
}
