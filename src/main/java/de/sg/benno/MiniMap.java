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
import de.sg.benno.renderer.SimpleTextureRenderer;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import de.sg.ogl.resource.Texture;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

import static de.sg.benno.World.WORLD_HEIGHT;
import static de.sg.benno.World.WORLD_WIDTH;
import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL11.*;

/**
 * Represents a MiniMap.
 */
public class MiniMap {

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
     * A {@link SimpleTextureRenderer} to render the textures on the screen.
     */
    private final SimpleTextureRenderer simpleTextureRenderer;

    /**
     *
     */
    private final byte[] bottomLayerPixels = new byte[WORLD_WIDTH * WORLD_HEIGHT * 4];

    /**
     *
     */
    private final byte[] shipPixels = new byte[WORLD_WIDTH * WORLD_HEIGHT * 4];

    /**
     * The bottom layer {@link Texture}.
     */
    private final Texture bottomLayerTexture;

    /**
     *
     */
    private final Texture shipsTexture;

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
     * @throws Exception If an error is thrown.
     */
    public MiniMap(WorldData provider, Context context, Camera camera, Zoom zoom) throws Exception {
        LOGGER.debug("Creates MiniMap object.");

        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.camera = Objects.requireNonNull(camera, "camera must not be null");
        this.simpleTextureRenderer = new SimpleTextureRenderer(Objects.requireNonNull(context, "context must not be null"));
        this.zoom = Objects.requireNonNull(zoom, "zoom must not be null");

        this.bottomLayerTexture = new Texture();
        this.bottomLayerTexture.setNrChannels(4);
        this.bottomLayerTexture.setFormat(GL_RGBA);

        this.shipsTexture = new Texture();
        this.shipsTexture.setNrChannels(4);
        this.shipsTexture.setFormat(GL_RGBA);

        initBottomLayer();
        initShipsLayer();
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

    public Texture getShipsTexture() {
        return shipsTexture;
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
    }

    /**
     * Renders the {@link MiniMap}.
     *
     * @param position The position on the screen.
     * @param size The size/scale of the texture.
     */
    public void render(Vector2f position, Vector2f size) {
        simpleTextureRenderer.render(bottomLayerTexture, position, size);
        simpleTextureRenderer.render(shipsTexture, position, size);
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    private static void addPixel(byte[] pixels, int index, int r, int g, int b) {
        pixels[index++] = (byte)r;
        pixels[index++] = (byte)g;
        pixels[index++] = (byte)b;
        pixels[index] = (byte)255;
    }

    private void createBottomLayer() {
        var index = 0;

        for(int y = 0; y < WORLD_HEIGHT; y++) {
            for(int x = 0; x < WORLD_WIDTH; x++) {
                var island5Optional = Island5.isIsland5OnPosition(x, y, provider.getIsland5List());
                if (island5Optional.isEmpty()) {
                    // water were found: blue color
                    addPixel(bottomLayerPixels, index, 0, 0, 200);
                    index += 4;
                } else {
                    // an island were found
                    var island5 = island5Optional.get();

                    // get island tile
                    var island5TileOptional = island5.getTileFromBottomLayer(x - island5.xPos, y - island5.yPos);
                    if (island5TileOptional.isPresent()) {
                        var island5Tile = island5TileOptional.get();
                        // the island also has water tiles
                        if (Tile.isWaterTile(island5Tile)) {
                            addPixel(bottomLayerPixels, index, 0, 0, 200);
                            index += 4;
                        } else {
                            addPixel(bottomLayerPixels, index, 0, 200, 0);
                            index += 4;
                        }
                    } else {
                        throw new BennoRuntimeException("Unexpected error: No tile were found.");
                    }
                }
            }
        }
    }

    private void createTopLayer() {
        // camera
        for (var y = 0; y < WORLD_HEIGHT; y++) {
            for (var x = 0; x < WORLD_WIDTH; x++) {
                var ws = TileUtil.worldToScreen(x, y, zoom.defaultTileWidthHalf, zoom.defaultTileHeightHalf);

                if (Aabb.pointVsAabb(new Vector2f(ws.x, ws.y), camera.getAabb())) {
                    var index = TileUtil.getIndexFrom2D(x, y) * 4;

                    if (x % 2 == 0 && y % 2 == 0) {
                        //bufferIndexPut(index, 200, 200, 200);
                    }
                }
            }
        }
    }

    private void createShipsLayer() {
        Arrays.fill(shipPixels, (byte)0);

        for(var ship : provider.getShips4List()) {
            for (var y = 0; y < 5; y++) {
                for (var x = 0; x < 5; x++) {
                    var index = TileUtil.getIndexFrom2D(ship.xPos + x, ship.yPos + y) * 4;
                    addPixel(shipPixels, index, 255, 0, 0);
                }
            }
        }
    }

    private void initBottomLayer() {
        createBottomLayer();

        ByteBuffer buffer = BufferUtils.createByteBuffer(bottomLayerPixels.length);
        buffer.put(bottomLayerPixels);
        buffer.flip();

        Texture.bind(bottomLayerTexture.getId());
        Texture.useBilinearFilter();

        glBindTexture(GL_TEXTURE_2D, bottomLayerTexture.getId());
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, WORLD_WIDTH, WORLD_HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
    }

    private void initShipsLayer() {
        createShipsLayer();

        ByteBuffer buffer = BufferUtils.createByteBuffer(shipPixels.length);
        buffer.put(shipPixels);
        buffer.flip();

        Texture.bind(shipsTexture.getId());
        Texture.useBilinearFilter();

        glBindTexture(GL_TEXTURE_2D, shipsTexture.getId());
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

        simpleTextureRenderer.cleanUp();
        bottomLayerTexture.cleanUp();
    }
}
