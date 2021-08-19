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
import de.sg.benno.state.Context;
import de.sg.ogl.resource.Texture;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
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
     * A {@link SimpleTextureRenderer} to render the {@link #miniMapTexture} on the screen.
     */
    private final SimpleTextureRenderer simpleTextureRenderer;

    /**
     * A {@link ByteBuffer} with pixel data.
     */
    private final ByteBuffer buffer;

    /**
     * A {@link Texture}.
     */
    private final Texture miniMapTexture;

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
    public MiniMap(WorldData provider, Context context, Camera camera) throws Exception {
        LOGGER.debug("Creates MiniMap object.");

        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.camera = Objects.requireNonNull(camera, "camera must not be null");
        this.simpleTextureRenderer = new SimpleTextureRenderer(Objects.requireNonNull(context, "context must not be null"));

        this.buffer = BufferUtils.createByteBuffer(WORLD_WIDTH * WORLD_HEIGHT * 4);

        this.miniMapTexture = new Texture();
        this.miniMapTexture.setNrChannels(4);
        this.miniMapTexture.setFormat(GL_RGBA);

        init();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #miniMapTexture}
     *
     * @return {@link #miniMapTexture}
     */
    public Texture getMiniMapTexture() {
        return miniMapTexture;
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    public void update() {
        init();
    }

    /**
     * Renders the {@link #miniMapTexture}.
     *
     * @param position The position on the screen.
     * @param size The size/scale of the texture.
     */
    public void render(Vector2f position, Vector2f size) {
        simpleTextureRenderer.render(miniMapTexture, position, size);
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    private void createBottomLayer() {
        for(int y = 0; y < WORLD_HEIGHT; y++) {
            for(int x = 0; x < WORLD_WIDTH; x++) {
                var island5Optional = Island5.isIsland5OnPosition(x, y, provider.getIsland5List());
                if (island5Optional.isEmpty()) {
                    // water were found: blue color
                    buffer.put((byte)0);    // r
                    buffer.put((byte)0);    // g
                    buffer.put((byte)200);  // b
                    buffer.put((byte)255);
                } else {
                    // an island were found
                    var island5 = island5Optional.get();

                    // get island tile
                    var island5TileOptional = island5.getTileFromBottomLayer(x - island5.xPos, y - island5.yPos);
                    if (island5TileOptional.isPresent()) {
                        var island5Tile = island5TileOptional.get();
                        // the island also has water tiles
                        if (Tile.isWaterTile(island5Tile)) {
                            buffer.put((byte)0);    // r
                            buffer.put((byte)0);    // g
                            buffer.put((byte)200);  // b
                            buffer.put((byte)255);
                        } else {
                            buffer.put((byte)0);
                            buffer.put((byte)200);
                            buffer.put((byte)0);
                            buffer.put((byte)255);
                        }
                    } else {
                        throw new BennoRuntimeException("Unexpected error: No tile were found.");
                    }
                }
            }
        }
    }

    private void createTopLayer() {
        // ships
        for(var ship : provider.getShips4List()) {
            for (var y = 0; y < 5; y++) {
                for (var x = 0; x < 5; x++) {
                    var index = TileUtil.getIndexFrom2D(ship.xPos + x, ship.yPos + y) * 4;

                    buffer.put(index++, (byte) 255);
                    buffer.put(index++, (byte) 0);
                    buffer.put(index++, (byte) 0);
                    buffer.put(index, (byte) 255);
                }
            }
        }

        // camera
        for (var y = 0; y < WORLD_HEIGHT; y++) {
            for (var x = 0; x < WORLD_WIDTH; x++) {

                // todo: get zoom
                var ws = TileUtil.worldToScreen(x, y, 8, 4);

                if (Aabb.pointVsAabb(new Vector2f(ws.x, ws.y), camera.getAabb())) {
                    var index = TileUtil.getIndexFrom2D(x, y) * 4;

                    if (x % 2 == 0 && y % 2 == 0) {
                        buffer.put(index++, (byte) 200);
                        buffer.put(index++, (byte) 200);
                        buffer.put(index++, (byte) 200);
                        buffer.put(index, (byte) 255);
                    }
                }
            }
        }
    }

    private void renderBufferToTexture() {
        buffer.flip();

        Texture.bind(miniMapTexture.getId());
        Texture.useBilinearFilter();

        glBindTexture(GL_TEXTURE_2D, miniMapTexture.getId());
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, WORLD_WIDTH, WORLD_HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
    }

    private void init(){
        // todo: zwei Texturen mit blending anzeigen

        createBottomLayer();
        createTopLayer();

        renderBufferToTexture();
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
        miniMapTexture.cleanUp();
    }
}
