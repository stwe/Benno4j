/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.renderer;

import de.sg.benno.chunk.Island5;
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

import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

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

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The parent {@link Island5} object.
     */
    private final Island5 island5;

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

    private final HashMap<Zoom, Float> xWidth = new HashMap<>();
    private final HashMap<Zoom, Float> yHeight = new HashMap<>();

    /**
     * The {@link Vao} objects for each {@link Zoom}.
     */
    private final HashMap<Zoom, Vao> vaos = new HashMap<>();

    private final HashMap<Zoom, ArrayList<Integer>> textureBuffer = new HashMap<>();
    private final HashMap<Zoom, ArrayList<Float>> yBuffer = new HashMap<>();
    private final HashMap<Zoom, HashMap<Integer, Integer>> gfxIndexMap = new HashMap<>();
    private final HashMap<Zoom, Integer> textureArrayIds = new HashMap<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public IslandRenderer(
            Island5 island5,
            HashMap<Zoom, ArrayList<TileGraphic>> tileGraphics,
            Context context
    ) throws Exception {
        this.island5 = island5;
        this.tileGraphics = tileGraphics;
        this.context = context;

        this.shader = context.engine.getResourceManager().loadResource(Shader.class, SHADER_NAME);

        for (var zoom: Zoom.values()) {
            xWidth.put(zoom, (float)context.bennoFiles.getStadtfldBshFile(zoom).getMaxX());
            yHeight.put(zoom, (float)context.bennoFiles.getStadtfldBshFile(zoom).getMaxY());
            createModelMatrices(zoom);
            createTextureIndex(zoom);
            addVao(zoom);
        }
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

        Texture.bindForReading(textureArrayIds.get(zoom), GL_TEXTURE0, GL_TEXTURE_2D_ARRAY);

        float ym = yHeight.get(zoom);

        shader.setUniform("projection", context.engine.getWindow().getOrthographicProjectionMatrix());
        shader.setUniform("view", camera.getViewMatrix());
        shader.setUniform("maxY", ym);
        shader.setUniform("sampler", 0);

        var vao = vaos.get(zoom);
        vao.bind();
        vao.drawInstanced(GL_TRIANGLES, instances);
        vao.unbind();

        Shader.unbind();

        if (!wireframe) {
            OpenGL.disableBlending();
        } else {
            OpenGL.disableWireframeMode();
        }
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

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

    private void createTextureIndex(Zoom zoom) {
        var texturesToLoad = new HashSet<Integer>();
        for (var tile : tileGraphics.get(zoom)) {
            texturesToLoad.add(tile.gfx);
        }

        /*
        529 = 0
        679 = 1
        200 = 2
        800 = 3
        etc
        */
        var zOffset = 0;
        var indexMap = new HashMap<Integer, Integer>();
        for (var texture : texturesToLoad) {
            indexMap.put(texture, zOffset);
            zOffset++;
        }
        gfxIndexMap.put(zoom, indexMap); // todo: immer gleich

        var instance = 0;
        var texBuffer = new ArrayList<Integer>();
        var heightBuffer = new ArrayList<Float>();
        for (var tile : tileGraphics.get(zoom)) {
            var gfxMap = gfxIndexMap.get(zoom);
            texBuffer.add(instance, gfxMap.get(tile.gfx));
            heightBuffer.add(instance, tile.size.y);
            instance++;
        }
        textureBuffer.put(zoom, texBuffer);
        yBuffer.put(zoom, heightBuffer);
    }

    private void addVao(Zoom zoom) throws IOException {
        createVao(zoom);
        addMeshVbo(zoom);
        addModelMatricesVbo(zoom);
        addTextureIndexVbo(zoom);
        addYVbo(zoom);
        createTextureArray(zoom);
    }

    private void createVao(Zoom zoom) {
        var vao = new Vao();
        vao.setDrawCount(DRAW_COUNT);
        vaos.put(zoom, vao);
    }

    private void addMeshVbo(Zoom zoom) {
        var quadGeometry = context.engine.getResourceManager().loadGeometry(Geometry.GeometryId.QUAD_2D);
        var vao = vaos.get(zoom);
        vao.addVbo(Vertex2D.toFloatArray(quadGeometry.vertices), quadGeometry.defaultBufferLayout);
    }

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
        vbo.storeIntegerInstances(textureBuffer.get(zoom), instances, GL_STATIC_DRAW);

        // set buffer layout
        vbo.addIntAttribute(7, 1, 1, 0, true);

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
        vbo.addFloatAttribute(8, 1, 1, 0, true);

        // unbind vao
        vao.unbind();
    }

    private void createTextureArray(Zoom zoom) throws IOException {
        var maxX = xWidth.get(zoom).intValue();
        var maxY = yHeight.get(zoom).intValue();

        var textureId = Texture.generateNewTextureId();
        textureArrayIds.put(zoom, textureId);

        Texture.bind(textureId, GL_TEXTURE_2D_ARRAY);

        var gfxMap = gfxIndexMap.get(zoom);

        glTextureStorage3D(textureId, 1, GL_RGBA8, maxX, maxY, gfxMap.size());

        var n = new int[maxX * maxY];
        Arrays.fill(n, 0);

        for (var entry : gfxMap.entrySet()) {

            var currentTexture = context.bennoFiles.getStadtfldBshFile(zoom).getBshTextures().get(entry.getKey());

            glTextureSubImage3D(
                    textureId,
                    0,
                    0, 0,
                    entry.getValue(), // zOffset
                    maxX, maxY,
                    1,
                    GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV,
                    n
            );

            var dbb = (DataBufferInt) currentTexture.getBufferedImage().getRaster().getDataBuffer();

            glTextureSubImage3D(
                    textureId,
                    0,
                    0, 0,
                    entry.getValue(),
                    currentTexture.getWidth(), currentTexture.getHeight(),
                    1,
                    GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV,
                    dbb.getData()
            );
        }

        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
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
