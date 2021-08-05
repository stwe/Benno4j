/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.renderer;

import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.file.BennoFiles;
import de.sg.benno.file.ImageFile;
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

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL45.glTextureStorage3D;
import static org.lwjgl.opengl.GL45.glTextureSubImage3D;

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

    private static final int NR_OF_GFX_ATLAS_IMAGES = 24; // 24 textures a (16 * 16) pics = 6144

    private static final int NR_OF_GFX_ROWS = 16;

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
     * The max x value for each {@link Zoom}.
     */
    private final HashMap<Zoom, Float> maxXList = new HashMap<>();

    /**
     * The max y value for each {@link Zoom}.
     */
    private final HashMap<Zoom, Float> maxYList = new HashMap<>();

    /**
     * The model matrices for each {@link Zoom}.
     */
    private final HashMap<Zoom, ArrayList<Matrix4f>> modelMatrices = new HashMap<>();

    /**
     * Number of instances to render.
     */
    private int instances = 0;

    /**
     * Specifies from which atlas the texture is to be taken.
     */
    private final ArrayList<Integer> textureAtlasIndex = new ArrayList<>();

    /**
     * Specifies the index of the texture on the atlas.
     */
    private final ArrayList<Integer> textureIndex = new ArrayList<>();

    /**
     * Precalculated offsets.
     */
    private final ArrayList<Float> offsets = new ArrayList<>();

    /**
     * THe height of each texture.
     */
    private final HashMap<Zoom, ArrayList<Float>> yBuffer = new HashMap<>();

    /**
     * The {@link Vao} objects for each {@link Zoom}.
     */
    private final HashMap<Zoom, Vao> vaos = new HashMap<>();

    /**
     * The texture array Id.
     */
    private int textureArrayId;

    /**
     * A list of GFX tile atlas images.
     */
    private final ArrayList<ImageFile> gfxAtlasTextures = new ArrayList<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public IslandRenderer(HashMap<Zoom, ArrayList<TileGraphic>> tileGraphics, Context context) throws Exception {
        this.tileGraphics = tileGraphics;
        this.context = context;
        this.shader = context.engine.getResourceManager().loadResource(Shader.class, SHADER_NAME);

        for (var zoom: Zoom.values()) {
            // prepare data
            maxXList.put(zoom, (float)context.bennoFiles.getStadtfldBshFile(zoom).getMaxX());
            maxYList.put(zoom, (float)context.bennoFiles.getStadtfldBshFile(zoom).getMaxY());
            createModelMatrices(zoom);
            createTextureInfo(zoom);

            // load data into gpu
            addVao(zoom);
        }

        // load atlas textures into Gpu
        createTextureArray();
    }

    //-------------------------------------------------
    // Prepare data
    //-------------------------------------------------

    /**
     * Stores the model matrix of the {@link TileGraphic} objects in each zoom level for each instance.
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
        // todo: hardcoded for GFX
        if (zoom == Zoom.GFX) {
            for (var tile : tileGraphics.get(zoom)) {
                var index = tile.gfx % (NR_OF_GFX_ROWS * NR_OF_GFX_ROWS);
                textureIndex.add(index);

                var offset = BennoFiles.getGfxTextureOffset(index, NR_OF_GFX_ROWS);
                offsets.add(offset.x);
                offsets.add(offset.y);

                textureAtlasIndex.add(tile.gfx / (NR_OF_GFX_ROWS * NR_OF_GFX_ROWS));
            }
        }

        // store for each zoom
        var heightBuffer = new ArrayList<Float>();
        for (var tile : tileGraphics.get(zoom)) {
            heightBuffer.add(tile.size.y);
        }

        yBuffer.put(zoom, heightBuffer);
    }

    //-------------------------------------------------
    // Data into Gpu
    //-------------------------------------------------

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
     * Stores a new 2D Quad to the {@link Vao} of the given {@link Zoom}.
     *
     * @param zoom {@link Zoom}.
     */
    private void addMeshVbo(Zoom zoom) {
        var quadGeometry = context.engine.getResourceManager().loadGeometry(Geometry.GeometryId.QUAD_2D);
        var vao = vaos.get(zoom);
        vao.addVbo(Vertex2D.toFloatArray(quadGeometry.vertices), quadGeometry.defaultBufferLayout);
    }

    /**
     * Stores the model matrices of the given {@link Zoom} to the {@link Vao}.
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

    private void addTextureIndexVbo(Zoom zoom) {
        var vao = vaos.get(zoom);

        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // store index (static draw)
        vbo.storeIntegerInstances(textureIndex, instances, GL_STATIC_DRAW);

        // set buffer layout
        vbo.addIntAttribute(7, 1, 1, 0, true);

        // unbind vao
        vao.unbind();
    }

    private void addTextureAtlasIndexVbo(Zoom zoom) {
        var vao = vaos.get(zoom);

        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // store index (static draw)
        vbo.storeIntegerInstances(textureAtlasIndex, instances, GL_STATIC_DRAW);

        // set buffer layout
        vbo.addIntAttribute(8, 1, 1, 0, true);

        // unbind vao
        vao.unbind();
    }

    private void addTextureOffsetsVbo(Zoom zoom) {
        var vao = vaos.get(zoom);

        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // store offsets (static draw)
        vbo.storeFloatArrayList(offsets, GL_STATIC_DRAW);

        // set buffer layout
        vbo.addFloatAttribute(9, 2, 2, 0, true);

        // unbind vao
        vao.unbind();
    }

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
    // Textures into Gpu
    //-------------------------------------------------

    private void loadGfxTextures() throws Exception {
        for (var i = 0; i < NR_OF_GFX_ATLAS_IMAGES; i++) {
            var path = "atlas/GFX/" + i + ".png";
            var image = new ImageFile(path);
            gfxAtlasTextures.add(image);
        }
    }

    private void createTextureArray() throws Exception {
        textureArrayId = Texture.generateNewTextureId();
        Texture.bind(textureArrayId, GL_TEXTURE_2D_ARRAY);

        loadGfxTextures();
        glTextureStorage3D(
                textureArrayId,
                1,
                GL_RGBA8,
                64 * NR_OF_GFX_ROWS,
                286 * NR_OF_GFX_ROWS,
                NR_OF_GFX_ATLAS_IMAGES
        );

        var zOffset = 0;
        for (var texture : gfxAtlasTextures) {
            glTextureSubImage3D(
                    textureArrayId,
                    0,
                    0, 0,
                    zOffset,
                    64 * NR_OF_GFX_ROWS,
                    286 * NR_OF_GFX_ROWS,
                    1,
                    GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV,
                    texture.getArgb()
            );

            zOffset++;
        }

        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    public void render(Camera camera, boolean wireframe, Zoom zoom) {
        if (!wireframe) {
            OpenGL.enableAlphaBlending();
        } else {
            OpenGL.enableWireframeMode();
        }

        shader.bind();

        Texture.bindForReading(textureArrayId, GL_TEXTURE0, GL_TEXTURE_2D_ARRAY);

        var my = maxYList.get(Zoom.GFX);

        shader.setUniform("projection", context.engine.getWindow().getOrthographicProjectionMatrix());
        shader.setUniform("view", camera.getViewMatrix());
        shader.setUniform("maxY", 286.0f);
        shader.setUniform("nrOfRows", (float)NR_OF_GFX_ROWS);
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
