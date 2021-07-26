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
import de.sg.benno.renderer.MiniMapRenderer;
import de.sg.benno.renderer.WaterRenderer;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import de.sg.ogl.Color;
import de.sg.ogl.Config;
import de.sg.ogl.OpenGL;
import de.sg.ogl.buffer.Fbo;
import de.sg.ogl.camera.OrthographicCamera;
import de.sg.ogl.renderer.TileRenderer;
import de.sg.ogl.resource.Texture;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

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

    /**
     * The width of the minimap.
     */
    private static final int MINIMAP_WIDTH = Config.WIDTH / 4;

    /**
     * The height of the minimap.
     */
    private static final int MINIMAP_HEIGHT = Config.HEIGHT / 4;

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
     * A {@link TileGraphic} for each map cell.
     */
    private final ArrayList<TileGraphic> miniMapTiles = new ArrayList<>();

    /**
     * A {@link MiniMapRenderer} to render the mini-map.
     */
    private MiniMapRenderer miniMapRenderer;

    private Fbo fbo;
    private Texture miniMapTexture;
    private TileRenderer tileRenderer;
    private boolean renderToFbo = true;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public World(WorldData provider, Context context) {
        LOGGER.debug("Creates World object from provider class {}.", provider.getClass());

        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");
        this.bennoFiles = this.context.bennoFiles;
        this.buildings = this.bennoFiles.getDataFiles().getBuildings();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Init object for each {@link Zoom}.
     *
     * @throws Exception If an error is thrown.
     */
    public void init() throws Exception {
        initWaterRenderer();
        initMiniMapRenderer();

        initFbo();
        tileRenderer = new TileRenderer(context.engine);
    }

    /**
     * Update object.
     *
     * @param dt The delta time.
     */
    public void update(float dt) {

    }

    // todo

    /**
     * Renders the world.
     *
     * @param camera The {@link OrthographicCamera} object.
     * @param wireframe Boolean flag for wireframe rendering.
     * @param zoom The current {@link Zoom}.
     */
    public void render(OrthographicCamera camera, boolean wireframe, Zoom zoom) {
        // render minimap to texture once only
        if (renderToFbo) {
            fbo.bindAsRenderTarget();
            OpenGL.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            OpenGL.clear();
            miniMapRenderer.render();
            fbo.unbindRenderTarget();
            renderToFbo = false;
        }

        // render deep water
        waterRenderers.get(zoom).render(camera, wireframe);

        // render minimap as texture
        // todo: the TileRenderer use a projection matrix, so we need to transform it this screen space
        tileRenderer.render(
                miniMapTexture.getId(),
                new Vector2f(Config.WIDTH - MINIMAP_WIDTH - 10, 350.0f),
                new Vector2f(MINIMAP_WIDTH, MINIMAP_HEIGHT)
        );
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
                    waterTile.tileGfxInfo.gfxIndex = water.gfx;
                    waterTile.worldPosition.x = x;
                    waterTile.worldPosition.y = y;

                    var screenPosition = TileUtil.worldToScreen(x, y, zoom.defaultTileWidthHalf, zoom.defaultTileHeightHalf);
                    screenPosition.y += adjustHeight;
                    screenPosition.x -= waterBshTexture.getWidth();
                    screenPosition.y -= waterBshTexture.getHeight();

                    waterTile.screenPosition = new Vector2f(screenPosition);
                    waterTile.size = new Vector2f(waterBshTexture.getWidth(), waterBshTexture.getHeight());

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
    // Minimap
    //-------------------------------------------------

    // todo: gfxIndex, graphicId

    /**
     * Creates the tiles for a minimap.
     */
    private void initMiniMapRenderer() throws Exception {
        for (int y = 0; y < WORLD_HEIGHT; y++) {
            for (int x = 0; x < WORLD_WIDTH; x++) {
                var tile = new TileGraphic();
                tile.tileGfxInfo.gfxIndex = 0;
                tile.worldPosition.x = x;
                tile.worldPosition.y = y;
                // todo
                tile.screenPosition = new Vector2f(x / (float)WORLD_WIDTH * 2.0f, y / (float)WORLD_HEIGHT * 2.0f);
                tile.screenPosition.x -= 1.0f;
                tile.screenPosition.y -= 1.0f;
                tile.size = new Vector2f(1.0f);

                var island5Optional = Island5.isIslandOnPosition(x, y, provider.getIsland5List());
                if (island5Optional.isEmpty()) {
                    // water were found: blue color
                    tile.color = Color.CORNFLOWER_BLUE.toVector3f();
                } else {
                    // an island were found
                    var island5 = island5Optional.get();

                    // get tile
                    var island5TileOptional = island5.getTileFromBottomLayer(x - island5.xPos, y - island5.yPos);
                    if (island5TileOptional.isPresent()) {
                        var island5Tile = island5TileOptional.get();
                        if (!((island5Tile.graphicId >= 1201 && island5Tile.graphicId <= 1221) ||
                                (island5Tile.graphicId >= 1251 && island5Tile.graphicId <= 1259))
                        ) {
                            tile.color = new Vector3f(0.5f, 0.35f, 0.05f); // brown = island
                        } else {
                            tile.color = Color.CORNFLOWER_BLUE.toVector3f(); // blue = use also color of water
                        }
                    } else {
                        throw new BennoRuntimeException("Unexpected error: No tile were found.");
                    }
                }

                miniMapTiles.add(tile);
            }
        }

        ArrayList<Matrix4f> modelMatrices = new ArrayList<>();
        var colors = new float[miniMapTiles.size() * 3];

        var i = 0;
        for (var tile : miniMapTiles) {
            modelMatrices.add(tile.getModelMatrix());
            colors[i++] = tile.color.x;
            colors[i++] = tile.color.y;
            colors[i++] = tile.color.z;
        }

        miniMapRenderer = new MiniMapRenderer(modelMatrices, colors, context);
    }

    // todo:

    private void initFbo() {
        fbo = new Fbo(context.engine, MINIMAP_WIDTH, MINIMAP_HEIGHT);
        fbo.bind();

        miniMapTexture = new Texture();
        Texture.bind(miniMapTexture.getId());
        Texture.useBilinearFilter();
        miniMapTexture.setNrChannels(4);
        miniMapTexture.setFormat(GL_RGBA);

        var ib = BufferUtils.createIntBuffer(MINIMAP_WIDTH * MINIMAP_HEIGHT);
        BufferUtils.zeroBuffer(ib);
        ib.flip();

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, MINIMAP_WIDTH, MINIMAP_HEIGHT, 0, miniMapTexture.getFormat(), GL_UNSIGNED_BYTE, ib);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, miniMapTexture.getId(), 0);

        int rboId = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, rboId);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, MINIMAP_WIDTH, MINIMAP_HEIGHT);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rboId);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new BennoRuntimeException("Error while creating renderbuffer.");
        }

        fbo.unbind();
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    public void cleanUp() {
        LOGGER.debug("Start clean up for the World.");

        // clean up created WaterRender objects
        waterRenderers.forEach((k, v) -> v.cleanUp());

        // clean up MiniMapRenderer
        miniMapRenderer.cleanUp();

        // clean up passed data provider object (gamFile)
        provider.cleanUp();
    }
}
