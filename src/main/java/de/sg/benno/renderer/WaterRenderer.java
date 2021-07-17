/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.renderer;

import de.sg.benno.BennoRuntimeException;
import de.sg.benno.file.BennoFiles;
import de.sg.benno.file.BshFile;
import de.sg.benno.state.Context;
import de.sg.ogl.Log;
import de.sg.ogl.OpenGL;
import de.sg.ogl.buffer.Vao;
import de.sg.ogl.buffer.Vertex2D;
import de.sg.ogl.camera.OrthographicCamera;
import de.sg.ogl.resource.Geometry;
import de.sg.ogl.resource.Shader;
import de.sg.ogl.resource.Texture;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.awt.image.DataBufferInt;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL30.glVertexAttribIPointer;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import static org.lwjgl.opengl.GL45.glTextureStorage3D;
import static org.lwjgl.opengl.GL45.glTextureSubImage3D;

/**
 * Represents a WaterRenderer.
 */
public class WaterRenderer {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * Color (blue) if no texture is used.
     */
    private static final Vector3f WATER_COLOR = new Vector3f(0.0f, 0.0f, 1.0f);

    /**
     * Number of vertices to be rendered per instance.
     */
    private static final int DRAW_COUNT = 6;

    /**
     * Number of texture levels.
     */
    private static final int MIP_LEVEL_COUNT = 1;

    /**
     * Number of water textures (START_WATER_GFX_INDEX - END_WATER_GFX_INDEX).
     */
    private static final int LAYER_COUNT = 12;

    /**
     * First water gfx index.
     */
    public static final int START_WATER_GFX_INDEX = 758;

    /**
     * Last water gfx index.
     */
    private static final int END_WATER_GFX_INDEX = 769;

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link ArrayList<Matrix4f>} objects.
     */
    private final ArrayList<Matrix4f> modelMatrices;

    /**
     * The {@link ArrayList<Integer>} objects.
     */
    private final ArrayList<Integer> textureIds;

    /**
     * The {@link Context} object.
     */
    private final Context context;

    /**
     * The {@link Zoom} using in this renderer.
     */
    private final Zoom zoom;

    /**
     * The {@link Shader} using in this renderer.
     */
    private final Shader shader;

    /**
     * The {@link Vao} object.
     */
    private final Vao vao;

    /**
     * Number of instances to render.
     */
    private final int instances;

    /**
     * The width of all textures.
     */
    private int textureWidth;

    /**
     * The height of all textures.
     */
    private int textureHeight;

    /**
     * The {@link BshFile} object.
     */
    private final BshFile bshFile;

    /**
     * The texture array id.
     */
    private int textureArrayId;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link WaterRenderer} object.
     *
     * @param modelMatrices {@link ArrayList<Matrix4f>}
     * @param textureIds {@link ArrayList<Integer>}
     * @param context {@link Context}
     * @param zoom {@link Zoom}
     * @throws Exception If an error is thrown.
     */
    public WaterRenderer(
            ArrayList<Matrix4f> modelMatrices,
            ArrayList<Integer> textureIds,
            Context context,
            Zoom zoom
    ) throws Exception {
        this.modelMatrices = modelMatrices;
        this.textureIds = textureIds;
        this.context = context;
        this.zoom = zoom;
        this.shader = context.engine.getResourceManager().loadResource(Shader.class, "deepWater");
        this.vao = new Vao();
        this.instances = modelMatrices.size();

        bshFile = context.bennoFiles.getBshFile(context.bennoFiles.getZoomableBshFilePath(
                zoom, BennoFiles.ZoomableBshFileName.STADTFLD_BSH
        ));

        initVao();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Renders the whole deep water area.
     *
     * @param camera {@link OrthographicCamera}
     */
    public void render(OrthographicCamera camera, boolean wireframe) {
        if (!wireframe) {
            OpenGL.enableAlphaBlending();
        } else {
            OpenGL.enableWireframeMode();
        }

        shader.bind();

        Texture.bindForReading(textureArrayId, GL_TEXTURE0, GL_TEXTURE_2D_ARRAY);

        shader.setUniform("projection", new Matrix4f(context.engine.getWindow().getOrthographicProjectionMatrix()));
        shader.setUniform("view", camera.getViewMatrix());
        shader.setUniform("sampler", 0);

        vao.bind();
        glDrawArraysInstanced(GL_TRIANGLES, 0, DRAW_COUNT, modelMatrices.size());
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

    /**
     * Setup {@link #vao}.
     */
    private void initVao() {
        textureWidth = zoom.defaultTileWidth;
        textureHeight = zoom.defaultTileHeight;

        addMeshVbo();
        addModelMatricesVbo();
        addTextureIdsVbo();

        createTextureArray();
    }

    /**
     * Add a 2DQuad to a new {@link de.sg.ogl.buffer.Vbo}.
     */
    private void addMeshVbo() {
        var quadGeometry = context.engine.getResourceManager().loadGeometry(Geometry.GeometryId.QUAD_2D);
        for (var vertex : quadGeometry.vertices) {
            vertex.color = WATER_COLOR;
        }

        vao.addVbo(Vertex2D.toFloatArray(quadGeometry.vertices), quadGeometry.defaultBufferLayout);
    }

    /**
     * Add {@link #modelMatrices} to a new {@link de.sg.ogl.buffer.Vbo}.
     */
    private void addModelMatricesVbo() {
        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // bind vbo
        vbo.bind();

        // store data
        var fb = BufferUtils.createFloatBuffer(instances * 16);
        for (var matrix : modelMatrices) {
            float[] t = new float[16];
            fb.put(matrix.get(t));
        }
        fb.flip();

        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);

        // unbind vbo
        vbo.unbind();

        // set buffer layout
        vbo.addFloatAttribute(3, 4, 16, 0, true);
        vbo.addFloatAttribute(4, 4, 16, 4, true);
        vbo.addFloatAttribute(5, 4, 16, 8, true);
        vbo.addFloatAttribute(6, 4, 16, 12, true);

        // unbind vao
        vao.unbind();
    }

    /**
     * Add {@link #textureIds} to a new {@link de.sg.ogl.buffer.Vbo}.
     */
    private void addTextureIdsVbo() {
        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // bind vbo
        vbo.bind();

        // store data
        var ib = BufferUtils.createIntBuffer(instances);
        for (var textureId : textureIds) {
            ib.put(textureId);
        }
        ib.flip();

        glBufferData(GL_ARRAY_BUFFER, ib, GL_STATIC_DRAW);

        // set buffer layout
        glEnableVertexAttribArray(7);
        glVertexAttribIPointer(7, 1, 4, 4, ib);
        glVertexAttribDivisor(7, 1);

        // unbind vbo
        vbo.unbind();

        // unbind vao
        vao.unbind();
    }

    //-------------------------------------------------
    // Texture array
    //-------------------------------------------------

    /**
     * Creates a texture array from the existing water textures.
     */
    private void createTextureArray() {
        textureArrayId = Texture.generateNewTextureId();
        Texture.bind(textureArrayId, GL_TEXTURE_2D_ARRAY);
        glTextureStorage3D(textureArrayId, MIP_LEVEL_COUNT, GL_RGBA8, textureWidth, textureHeight, LAYER_COUNT);

        var zOffset = 0;

        for (var i = START_WATER_GFX_INDEX; i <= END_WATER_GFX_INDEX; i++) {
            var currentTexture = bshFile.getBshTextures().get(i);
            if (currentTexture.getWidth() != textureWidth || currentTexture.getHeight() != textureHeight) {
                throw new BennoRuntimeException("Invalid texture size.");
            }

            var dbb = (DataBufferInt) currentTexture.getBufferedImage().getRaster().getDataBuffer();

            glTextureSubImage3D(
                    textureArrayId,
                    0,
                    0, 0,
                    zOffset,
                    textureWidth, textureHeight,
                    1,
                    GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV,
                    dbb.getData()
            );

            zOffset++;
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
        Log.LOGGER.debug("Clean up WaterRenderer for {}.", zoom);

        this.vao.cleanUp();
    }
}
