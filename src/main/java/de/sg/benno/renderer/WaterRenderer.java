/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.renderer;

import de.sg.benno.state.Context;
import de.sg.ogl.Log;
import de.sg.ogl.OpenGL;
import de.sg.ogl.buffer.Vao;
import de.sg.ogl.buffer.Vertex2D;
import de.sg.ogl.camera.OrthographicCamera;
import de.sg.ogl.resource.Geometry;
import de.sg.ogl.resource.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glVertexAttribIPointer;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class WaterRenderer {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * Color if no texture is used.
     */
    private static final Vector3f WATER_COLOR = new Vector3f(0.0f, 0.0f, 1.0f);

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    private final ArrayList<Matrix4f> modelMatrices;
    private final ArrayList<Integer> textureIds;
    private final Context context;
    private final Zoom zoom;
    private final Geometry quadGeometry;
    private final Shader shader;
    private final Vao vao;
    private int instances;
    private int textureWidth;
    private int textureHeight;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

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
        this.quadGeometry = context.engine.getResourceManager().loadGeometry(Geometry.GeometryId.QUAD_2D);
        this.shader = context.engine.getResourceManager().loadResource(Shader.class, "deepWater");
        this.vao = new Vao();
        this.instances = modelMatrices.size();

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
    public void render(OrthographicCamera camera) {
        OpenGL.enableWireframeMode();
        //OpenGL.enableAlphaBlending();
        shader.bind();

        shader.setUniform("projection", new Matrix4f(context.engine.getWindow().getOrthographicProjectionMatrix()));
        shader.setUniform("view", camera.getViewMatrix());

        vao.bind();
        glDrawArraysInstanced(GL_TRIANGLES, 0, 6, modelMatrices.size());
        vao.unbind();

        //OpenGL.disableBlending();
        Shader.unbind();
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

    private void createTextureArray() {

    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        Log.LOGGER.debug("Clean up WaterRenderer.");

        this.vao.cleanUp();
    }
}