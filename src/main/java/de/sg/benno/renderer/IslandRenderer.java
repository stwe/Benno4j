/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.renderer;

import de.sg.benno.TileAtlas;
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.input.Camera;
import de.sg.benno.state.Context;
import de.sg.ogl.OpenGL;
import de.sg.ogl.buffer.Vao;
import de.sg.ogl.buffer.Vertex2D;
import de.sg.ogl.resource.Geometry;
import de.sg.ogl.resource.Shader;
import de.sg.ogl.resource.Texture;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;

import static de.sg.benno.TileAtlas.*;
import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;

/**
 * Represents an IslandRenderer.
 */
public class IslandRenderer {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * Number of vertices to be rendered per instance.
     */
    private static final int DRAW_COUNT = 6;

    /**
     * The name of the used shader.
     */
    private static final String SHADER_NAME = "island";

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link TileGraphic} objects for each {@link Zoom}.
     */
    private final HashMap<Zoom, ArrayList<TileGraphic>> tileGraphics;

    /**
     * The {@link Context} object.
     */
    private final Context context;

    /**
     * The {@link Shader} using in this renderer.
     */
    private final Shader shader;

    /**
     * The model matrices for each {@link Zoom}.
     */
    private final HashMap<Zoom, ArrayList<Matrix4f>> modelMatrices = new HashMap<>();

    /**
     * Number of instances to render.
     */
    private int instances = 0;

    /**
     * Specifies from which atlas image the texture is to be taken.
     */
    private final HashMap<Zoom, ArrayList<Integer>> textureAtlasIndex = new HashMap<>();

    /**
     * Specifies the index of the texture on the atlas image.
     */
    private final HashMap<Zoom, ArrayList<Integer>> textureIndex = new HashMap<>();

    /**
     * Precalculated texture offsets.
     * Specifies where the texture is on the atlas image (coordinates).
     */
    private final HashMap<Zoom, ArrayList<Float>> offsets = new HashMap<>();

    /**
     * The height of each texture.
     */
    private final HashMap<Zoom, ArrayList<Float>> yBuffer = new HashMap<>();

    /**
     * The {@link Vao} objects for each {@link Zoom}.
     */
    private final HashMap<Zoom, Vao> vaos = new HashMap<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link IslandRenderer} object.
     *
     * @param tileGraphics The {@link TileGraphic} objects of an {@link de.sg.benno.chunk.Island5}.
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public IslandRenderer(HashMap<Zoom, ArrayList<TileGraphic>> tileGraphics, Context context) throws Exception {
        this.tileGraphics = tileGraphics;
        this.context = context;
        this.shader = context.engine.getResourceManager().loadResource(Shader.class, SHADER_NAME);

        // for each zoom...
        for (var zoom: Zoom.values()) {
            // prepare data
            createModelMatrices(zoom);
            createTextureInfo(zoom);

            // load data into gpu
            addVao(zoom);
        }
    }

    //-------------------------------------------------
    // Prepare data
    //-------------------------------------------------

    /**
     * Stores the model matrix of the {@link TileGraphic} objects.
     *
     * @param zoom {@link Zoom}
     */
    private void createModelMatrices(Zoom zoom) {
        var matrices = new ArrayList<Matrix4f>();
        for (var tile : tileGraphics.get(zoom)) {
            matrices.add(tile.getModelMatrix());
        }

        modelMatrices.put(zoom, matrices);

        if (instances == 0) {
            instances = matrices.size();
        }
    }

    /**
     * Stores the information about which texture is used.
     *
     * @param zoom {@link Zoom}
     */
    private void createTextureInfo(Zoom zoom) {
        var textureIndexList = new ArrayList<Integer>();
        var offsetList = new ArrayList<Float>();
        var textureAtlasIndexList = new ArrayList<Integer>();
        var heightBuffer = new ArrayList<Float>();

        var rows = 0;
        switch (zoom) {
            case GFX: rows = NR_OF_GFX_ROWS; break;
            case MGFX: rows = NR_OF_MGFX_ROWS; break;
            case SGFX: rows = NR_OF_SGFX_ROWS; break;
            default:
        }

        for (var tile : tileGraphics.get(zoom)) {
            // texture index
            var index = tile.gfx % (rows * rows);
            textureIndexList.add(index);

            // offset
            var offset = TileAtlas.getTextureOffset(index, rows);
            offsetList.add(offset.x);
            offsetList.add(offset.y);

            // texture atlas index
            textureAtlasIndexList.add(tile.gfx / (rows * rows));

            // yBuffer (height of each tile graphic)
            heightBuffer.add(tile.size.y);
        }

        textureIndex.put(zoom, textureIndexList);
        offsets.put(zoom, offsetList);
        textureAtlasIndex.put(zoom, textureAtlasIndexList);
        yBuffer.put(zoom, heightBuffer);
    }

    //-------------------------------------------------
    // Data into Gpu
    //-------------------------------------------------

    /**
     * Creates a Vao and all Vbos.
     *
     * @param zoom {@link Zoom}
     */
    private void addVao(Zoom zoom) {
        createVao(zoom);
        addMeshVbo(zoom);
        addModelMatricesVbo(zoom);
        addTextureIndexVbo(zoom);
        addTextureAtlasIndexVbo(zoom);
        addTextureOffsetsVbo(zoom);
        addYVbo(zoom);
    }

    /**
     * Creates a new {@link Vao} for a given {@link Zoom}.
     *
     * @param zoom {@link Zoom}.
     */
    private void createVao(Zoom zoom) {
        var vao = new Vao();
        vao.setDrawCount(DRAW_COUNT);
        vaos.put(zoom, vao);
    }

    /**
     * Stores a new 2D Quad to the {@link Vao} for the given {@link Zoom}.
     *
     * @param zoom {@link Zoom}.
     */
    private void addMeshVbo(Zoom zoom) {
        var quadGeometry = context.engine.getResourceManager().loadGeometry(Geometry.GeometryId.QUAD_2D);
        var vao = vaos.get(zoom);
        vao.addVbo(Vertex2D.toFloatArray(quadGeometry.vertices), quadGeometry.defaultBufferLayout);
    }

    /**
     * Stores the model matrices for the given {@link Zoom} to the {@link Vao}.
     *
     * @param zoom {@link Zoom}.
     */
    private void addModelMatricesVbo(Zoom zoom) {
        var vao = vaos.get(zoom);
        var matrices = modelMatrices.get(zoom);

        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // store model matrices (static draw)
        vbo.storeMatrix4fInstances(matrices, instances);

        // set buffer layout
        vbo.addFloatAttribute(3, 4, 16, 0, true);
        vbo.addFloatAttribute(4, 4, 16, 4, true);
        vbo.addFloatAttribute(5, 4, 16, 8, true);
        vbo.addFloatAttribute(6, 4, 16, 12, true);

        // unbind vao
        vao.unbind();
    }

    /**
     * Stores the texture indices for the given {@link Zoom} to the {@link Vao}.
     *
     * @param zoom {@link Zoom}.
     */
    private void addTextureIndexVbo(Zoom zoom) {
        var vao = vaos.get(zoom);

        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // store index (static draw)
        vbo.storeIntegerInstances(textureIndex.get(zoom), instances, GL_STATIC_DRAW);

        // set buffer layout
        vbo.addIntAttribute(7, 1, 1, 0, true);

        // unbind vao
        vao.unbind();
    }

    /**
     * Stores the atlas image indices for the given {@link Zoom} to the {@link Vao}.
     *
     * @param zoom {@link Zoom}.
     */
    private void addTextureAtlasIndexVbo(Zoom zoom) {
        var vao = vaos.get(zoom);

        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // store index (static draw)
        vbo.storeIntegerInstances(textureAtlasIndex.get(zoom), instances, GL_STATIC_DRAW);

        // set buffer layout
        vbo.addIntAttribute(8, 1, 1, 0, true);

        // unbind vao
        vao.unbind();
    }

    /**
     * Stores the offsets for the given {@link Zoom} to the {@link Vao}.
     *
     * @param zoom {@link Zoom}.
     */
    private void addTextureOffsetsVbo(Zoom zoom) {
        var vao = vaos.get(zoom);

        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // store offsets (static draw)
        vbo.storeFloatArrayList(offsets.get(zoom), GL_STATIC_DRAW);

        // set buffer layout
        vbo.addFloatAttribute(9, 2, 2, 0, true);

        // unbind vao
        vao.unbind();
    }

    /**
     * Stores the heights for the given {@link Zoom} to the {@link Vao}.
     *
     * @param zoom {@link Zoom}.
     */
    private void addYVbo(Zoom zoom) {
        var vao = vaos.get(zoom);

        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // store buffer (static draw)
        vbo.storeFloatArrayList(yBuffer.get(zoom), GL_STATIC_DRAW);

        // set buffer layout
        vbo.addFloatAttribute(10, 1, 1, 0, true);

        // unbind vao
        vao.unbind();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Render the {@link de.sg.benno.chunk.Island5}.
     *
     * @param camera {@link Camera} to get the view matrix.
     * @param wireframe True or false whether to render a wireframe model.
     * @param zoom {@link Zoom}
     */
    public void render(Camera camera, boolean wireframe, Zoom zoom) {
        if (!wireframe) {
            OpenGL.enableAlphaBlending();
        } else {
            OpenGL.enableWireframeMode();
        }

        var textureId = 0;
        var maxYHeight = 0.0f;
        var rows = 0.0f;
        switch (zoom) {
            case GFX:
                textureId = TileAtlas.getGfxTextureArrayId();
                maxYHeight = MAX_GFX_HEIGHT;
                rows = (float)NR_OF_GFX_ROWS;
                break;
            case MGFX:
                textureId = TileAtlas.getMgfxTextureArrayId();
                maxYHeight = MAX_MGFX_HEIGHT;
                rows = (float)NR_OF_MGFX_ROWS;
                break;
            case SGFX:
                textureId = TileAtlas.getSgfxTextureArrayId();
                maxYHeight = MAX_SGFX_HEIGHT;
                rows = (float)NR_OF_SGFX_ROWS;
                break;
            default:
        }

        shader.bind();

        Texture.bindForReading(textureId, GL_TEXTURE0, GL_TEXTURE_2D_ARRAY);

        shader.setUniform("projection", context.engine.getWindow().getOrthographicProjectionMatrix());
        shader.setUniform("view", camera.getViewMatrix());
        shader.setUniform("maxY", maxYHeight);
        shader.setUniform("nrOfRows", rows);
        shader.setUniform("sampler", 0);

        var vao = vaos.get(zoom);
        vao.bind();
        vao.drawInstanced(GL_TRIANGLES, instances);
        vao.unbind();

        Shader.unbind();
        Texture.unbind();

        if (!wireframe) {
            OpenGL.disableBlending();
        } else {
            OpenGL.disableWireframeMode();
        }
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the IslandRenderer.");

        for (var zoom : Zoom.values()) {
            var vao = vaos.get(zoom);
            vao.cleanUp();
        }
    }
}
