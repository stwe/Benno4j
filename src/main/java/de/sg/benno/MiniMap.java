/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno;

import de.sg.benno.chunk.Island5;
import de.sg.benno.chunk.Tile;
import de.sg.benno.chunk.WorldData;
import de.sg.benno.input.Aabb;
import de.sg.benno.input.Camera;
import de.sg.benno.ogl.resource.Texture;
import de.sg.benno.renderer.MiniMapRenderer;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import org.joml.Vector2f;
import org.joml.Vector4i;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

import static de.sg.benno.World.WORLD_HEIGHT;
import static de.sg.benno.World.WORLD_WIDTH;
import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL11.*;

/**
 * Represents a MiniMap.
 */
public class MiniMap {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * Number of bytes for all pixels of a layer.
     */
    private static final int PIXEL_BYTES = WORLD_WIDTH * WORLD_HEIGHT * 4;

    /**
     * So that we can see the camera section better.
     */
    private static final int FIELD_OF_VIEW_EDGE_THICKNESS = 2;

    /**
     * Transparent deep water.
     */
    private static final Vector4i DEEP_WATER_COLOR = new Vector4i(0, 0, 0, 0);

    /**
     * Green islands.
     */
    private static final Vector4i ISLAND_COLOR = new Vector4i(0, 200, 0, 255);

    /**
     * Red ships.
     */
    private static final Vector4i SHIP_COLOR = new Vector4i(255, 0, 0, 255);

    /**
     * White camera.
     */
    private static final Vector4i CAMERA_COLOR = new Vector4i(255, 255, 255, 255);

    /**
     * Brown buildings.
     */
    private static final Vector4i BUILDING_COLOR = new Vector4i(150, 150, 0, 255);

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * Provides data for drawing.
     * For example the {@link Island5} or {@link de.sg.benno.chunk.Ship4} objects from a loaded GAM file.
     */
    private final WorldData provider;

    /**
     * The {@link Camera} object.
     */
    private final Camera camera;

    /**
     * A {@link MiniMapRenderer} to render the textures on the screen.
     */
    private final MiniMapRenderer miniMapRenderer;

    /**
     * The bottom layer pixels.
     */
    private final byte[] bottomLayerPixels = new byte[PIXEL_BYTES];

    /**
     * The ships pixels.
     */
    private final byte[] shipPixels = new byte[PIXEL_BYTES];

    /**
     * The pixels of field of view.
     */
    private final byte[] cameraPixels = new byte[PIXEL_BYTES];

    /**
     * The bottom layer {@link Texture}.
     */
    private Texture bottomLayerTexture;

    /**
     * The {@link Texture} with all ships.
     */
    private Texture shipsTexture;

    /**
     * The {@link Texture} with the field of view of the camera.
     */
    private Texture cameraTexture;

    /**
     * The current {@link Zoom}.
     */
    private Zoom zoom;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link MiniMap} object.
     *
     * @param provider The {@link WorldData} object.
     * @param context The {@link Context} object.
     * @param camera The {@link Camera} object to display in the minimap.
     * @param zoom The current {@link Zoom}.
     * @throws Exception If an error is thrown.
     */
    public MiniMap(WorldData provider, Context context, Camera camera, Zoom zoom) throws Exception {
        LOGGER.debug("Creates MiniMap object.");

        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.camera = Objects.requireNonNull(camera, "camera must not be null");
        this.miniMapRenderer = new MiniMapRenderer(Objects.requireNonNull(context, "context must not be null"));
        this.zoom = Objects.requireNonNull(zoom, "zoom must not be null");

        createTextureObjects();

        createBottomLayer();
        createShipsLayer();
        createCameraLayer();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #bottomLayerTexture}.
     *
     * @return {@link #bottomLayerTexture}
     */
    public Texture getBottomLayerTexture() {
        return bottomLayerTexture;
    }

    /**
     * Get {@link #shipsTexture}.
     *
     * @return {@link #shipsTexture}
     */
    public Texture getShipsTexture() {
        return shipsTexture;
    }

    /**
     * Get {@link #cameraTexture}.
     *
     * @return {@link #cameraTexture}
     */
    public Texture getCameraTexture() {
        return cameraTexture;
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Updates the {@link MiniMap}.
     *
     * @param zoom The current {@link Zoom}.
     */
    public void update(Zoom zoom) {
        this.zoom = zoom;

        // todo: handle ships update

        createCameraLayer();
    }

    /**
     * Renders the {@link MiniMap}.
     *
     * @param position The position on the screen.
     * @param size The size/scale of the texture.
     */
    public void render(Vector2f position, Vector2f size) {
        miniMapRenderer.render(bottomLayerTexture, shipsTexture, cameraTexture, position, size);
    }

    //-------------------------------------------------
    // Content
    //-------------------------------------------------

    /**
     * Creates the bottom layer pixels.
     */
    private void createBottomLayerPixels() {
        var index = 0;

        for(int y = 0; y < WORLD_HEIGHT; y++) {
            for(int x = 0; x < WORLD_WIDTH; x++) {
                var island5Optional = Island5.isIsland5OnPosition(x, y, provider.getIsland5List());
                if (island5Optional.isEmpty()) {
                    // water were found
                    addPixel(bottomLayerPixels, index, DEEP_WATER_COLOR);
                    index += 4;
                } else {
                    // an island were found
                    var island5 = island5Optional.get();

                    // get island bottom layer (terrain)
                    var tileFromBottomLayer = island5.getTileFromBottomLayer(x - island5.xPos, y - island5.yPos);

                    // get island top layer (building)
                    var tileFromTopLayer = island5.getTileFromTopLayer(x - island5.xPos, y - island5.yPos);

                    // highlight buildings from top layer
                    if (tileFromTopLayer.isPresent() && tileFromTopLayer.get().graphicId != 0xFFFF) {
                        addPixel(bottomLayerPixels, index, BUILDING_COLOR);
                        index += 4;
                    }

                    if (tileFromTopLayer.isPresent() && tileFromTopLayer.get().graphicId == 0xFFFF && tileFromBottomLayer.isPresent()) {
                        var tile = tileFromBottomLayer.get();
                        // the island also has water tiles
                        if (Tile.isWaterTile(tile)) {
                            addPixel(bottomLayerPixels, index, DEEP_WATER_COLOR);
                        } else {
                            addPixel(bottomLayerPixels, index, ISLAND_COLOR);
                        }

                        index += 4;
                    }

                }
            }
        }
    }

    /**
     * Creates the ships layer pixels.
     */
    private void createShipsLayerPixels() {
        Arrays.fill(shipPixels, (byte)0);

        for(var ship : provider.getShips4List()) {
            for (var y = 0; y < 5; y++) {
                for (var x = 0; x < 5; x++) {
                    var index = TileUtil.getIndexFrom2D(ship.xPos + x, ship.yPos + y) * 4;
                    addPixel(shipPixels, index, SHIP_COLOR);
                }
            }
        }
    }

    /**
     * Creates the camera layer pixels.
     */
    private void createCameraLayerPixels() {
        Arrays.fill(cameraPixels, (byte)0);

        for (var y = 0; y < WORLD_HEIGHT; y++) {
            for (var x = 0; x < WORLD_WIDTH; x++) {
                var ws = new Vector2f(TileUtil.worldToScreen(x, y, zoom.defaultTileWidthHalf, zoom.defaultTileHeightHalf));
                var wsXMinusOne = new Vector2f(TileUtil.worldToScreen(x - FIELD_OF_VIEW_EDGE_THICKNESS, y, zoom.defaultTileWidthHalf, zoom.defaultTileHeightHalf));
                var wsYMinusOne = new Vector2f(TileUtil.worldToScreen(x, y - FIELD_OF_VIEW_EDGE_THICKNESS, zoom.defaultTileWidthHalf, zoom.defaultTileHeightHalf));
                var wsYPlusOne = new Vector2f(TileUtil.worldToScreen(x, y + FIELD_OF_VIEW_EDGE_THICKNESS, zoom.defaultTileWidthHalf, zoom.defaultTileHeightHalf));

                if (Aabb.pointVsAabb(ws, camera.getAabb())) {
                    if (!Aabb.pointVsAabb(wsXMinusOne, camera.getAabb()) ||
                            !Aabb.pointVsAabb(wsYMinusOne, camera.getAabb()) || !Aabb.pointVsAabb(wsYPlusOne, camera.getAabb())) {
                        var index = TileUtil.getIndexFrom2D(x, y) * 4;
                        addPixel(cameraPixels, index, CAMERA_COLOR);
                    }
                }
            }
        }
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Creates empty textures.
     */
    private void createTextureObjects() {
        this.bottomLayerTexture = new Texture();
        this.bottomLayerTexture.setNrChannels(4);
        this.bottomLayerTexture.setFormat(GL_RGBA);

        this.shipsTexture = new Texture();
        this.shipsTexture.setNrChannels(4);
        this.shipsTexture.setFormat(GL_RGBA);

        this.cameraTexture = new Texture();
        this.cameraTexture.setNrChannels(4);
        this.cameraTexture.setFormat(GL_RGBA);
    }

    /**
     * Creates bottom layer.
     */
    private void createBottomLayer() {
        createBottomLayerPixels();
        pixelToTexture(bottomLayerPixels, bottomLayerTexture);
    }

    /**
     * Creates ships layer.
     */
    private void createShipsLayer() {
        createShipsLayerPixels();
        pixelToTexture(shipPixels, shipsTexture);
    }

    /**
     * Creates camera layer.
     */
    private void createCameraLayer() {
        createCameraLayerPixels();
        pixelToTexture(cameraPixels, cameraTexture);
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Adds rgba values to a given pixel byte map.
     *
     * @param pixels The pixel byte map.
     * @param index The map index.
     * @param r The value for red.
     * @param g The value for green.
     * @param b The value for blue.
     * @param a The value for alpha.
     */
    private static void addPixel(byte[] pixels, int index, int r, int g, int b, int a) {
        pixels[index++] = (byte)r;
        pixels[index++] = (byte)g;
        pixels[index++] = (byte)b;
        pixels[index] = (byte)a;
    }

    /**
     * Adds rgba values to a given pixel byte map.
     *
     * @param pixels The pixel byte map.
     * @param index The map index.
     * @param color The rgba values.
     */
    private static void addPixel(byte[] pixels, int index, Vector4i color) {
        addPixel(pixels, index, color.x, color.y, color.z, color.w);
    }

    /**
     * Creates a texture based on the pixel data.
     *
     * @param pixels The pixel byte map.
     * @param texture A {@link Texture} object.
     */
    private static void pixelToTexture(byte[] pixels, Texture texture) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(pixels.length);
        buffer.put(pixels);
        buffer.flip();

        Texture.bind(texture.getId());
        Texture.useBilinearFilter();

        glBindTexture(GL_TEXTURE_2D, texture.getId());
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, WORLD_WIDTH, WORLD_HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the MiniMap.");

        miniMapRenderer.cleanUp();

        bottomLayerTexture.cleanUp();
        shipsTexture.cleanUp();
        cameraTexture.cleanUp();
    }
}
