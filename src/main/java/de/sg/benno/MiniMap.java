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
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.chunk.WorldData;
import de.sg.benno.renderer.MiniMapRenderer;
import de.sg.benno.renderer.SimpleTextureRenderer;
import de.sg.benno.state.Context;
import de.sg.ogl.Color;
import de.sg.ogl.OpenGL;
import de.sg.ogl.buffer.Fbo;
import de.sg.ogl.resource.Texture;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.util.ArrayList;
import java.util.Objects;

import static de.sg.benno.World.WORLD_HEIGHT;
import static de.sg.benno.World.WORLD_WIDTH;
import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;

/**
 * Represents a MiniMap.
 */
public class MiniMap {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * The width of the minimap.
     */
    private static final int MINIMAP_WIDTH = WORLD_WIDTH;

    /**
     * The height of the minimap.
     */
    private static final int MINIMAP_HEIGHT = WORLD_HEIGHT;

    /**
     * The color for the water.
     */
    private static final Vector3f WATER_COLOR = Color.CORNFLOWER_BLUE.toVector3f();

    /**
     * The color (brown) for the islands.
     */
    private static final Vector3f ISLAND_COLOR = new Vector3f(0.5f, 0.35f, 0.05f);

    /**
     * The color for ships.
     */
    private static final Vector3f SHIP_COLOR = Color.GREEN.toVector3f();

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * Provides data for drawing.
     * For example the {@link Island5} or {@link de.sg.benno.chunk.Ship4} objects from a loaded GAM file.
     */
    private final WorldData provider;

    /**
     * The {@link Context} object.
     */
    private final Context context;

    /**
     * A {@link TileGraphic} for each map cell.
     */
    private final ArrayList<TileGraphic> miniMapTiles = new ArrayList<>();

    /**
     * A {@link MiniMapRenderer} to render the minimap to the {@link #fbo}.
     */
    private MiniMapRenderer miniMapRenderer;

    /**
     * A {@link Fbo} object.
     */
    private Fbo fbo;

    /**
     * A {@link Texture} that is used as a color attachment to the {@link #fbo}.
     */
    private Texture miniMapTexture;

    /**
     * Flag so that we only render once to the {@link Fbo}.
     */
    private boolean renderToFbo = true;

    /**
     * A {@link SimpleTextureRenderer} to render the {@link #miniMapTexture} on the screen.
     */
    private SimpleTextureRenderer simpleTextureRenderer;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link MiniMap} object.
     *
     * @param provider The {@link WorldData} object.
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public MiniMap(WorldData provider, Context context) throws Exception {
        LOGGER.debug("Creates MiniMap object.");

        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");

        init();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Renders the minimap to {@link #miniMapTexture}.
     */
    private void renderToFbo() {
        // render minimap to texture once only
        if (renderToFbo) {
            fbo.bindAsRenderTarget();
            OpenGL.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            OpenGL.clear();
            miniMapRenderer.render();
            fbo.unbindRenderTarget();
            renderToFbo = false;
        }
    }

    /**
     * Renders the {@link #miniMapTexture}.
     *
     * @param position The position on the screen.
     * @param size The size/scale of the texture.
     */
    public void render(Vector2f position, Vector2f size) {
        renderToFbo();
        simpleTextureRenderer.render(miniMapTexture, position, size);
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Init MiniMap.
     *
     * @throws Exception If an error is thrown.
     */
    private void init() throws Exception {
        createTiles();
        createRenderer();
        createFbo();
    }

    /**
     * Creates tiles for the MiniMap.
     */
    private void createTiles() {
        for (int y = 0; y < WORLD_HEIGHT; y++) {
            for (int x = 0; x < WORLD_WIDTH; x++) {
                var tile = new TileGraphic();
                tile.gfx = 0; // no gfx is used
                tile.tileHeight = TileGraphic.TileHeight.SEA_LEVEL;
                tile.worldPosition.x = x;
                tile.worldPosition.y = y;
                tile.screenPosition = new Vector2f(x / (float)WORLD_WIDTH * 2.0f, y / (float)WORLD_HEIGHT * 2.0f);
                tile.screenPosition.x -= 1.0f;
                tile.screenPosition.y -= 1.0f;
                tile.size = new Vector2f(1.0f);

                // water && island
                var island5Optional = Island5.isIslandOnPosition(x, y, provider.getIsland5List());
                if (island5Optional.isEmpty()) {
                    // water were found: blue color
                    tile.color = WATER_COLOR;
                } else {
                    // an island were found
                    var island5 = island5Optional.get();

                    // get island tile
                    var island5TileOptional = island5.getTileFromBottomLayer(x - island5.xPos, y - island5.yPos);
                    if (island5TileOptional.isPresent()) {
                        var island5Tile = island5TileOptional.get();
                        // the island also has water tiles
                        if (Tile.isWaterTile(island5Tile)) {
                            tile.color = WATER_COLOR;
                        } else {
                            tile.color = ISLAND_COLOR;
                        }
                    } else {
                        throw new BennoRuntimeException("Unexpected error: No tile were found.");
                    }
                }

                // ships
                for(var ship : provider.getShips4List()) {
                    if ((x >= ship.xPos && x <= ship.xPos + 5) && (y >= ship.yPos && y <= ship.yPos + 5)) {
                        tile.color = SHIP_COLOR;
                        break;
                    }
                }

                // add tile
                miniMapTiles.add(tile);
            }
        }
    }

    /**
     * Creates and initialize the renderer.
     *
     * @throws Exception If an error is thrown.
     */
    private void createRenderer() throws Exception {
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
        simpleTextureRenderer = new SimpleTextureRenderer(context);
    }

    /**
     * Create a {@link Fbo}.
     */
    private void createFbo() {
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

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the MiniMap.");

        miniMapRenderer.cleanUp();
        simpleTextureRenderer.cleanUp();
        fbo.cleanUp(); // todo: clean up rbo
        miniMapTexture.cleanUp();
    }
}
