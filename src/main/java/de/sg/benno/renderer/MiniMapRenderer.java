/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.renderer;

import de.sg.benno.state.Context;
import de.sg.ogl.OpenGL;
import de.sg.ogl.buffer.Vao;
import de.sg.ogl.buffer.Vertex2D;
import de.sg.ogl.resource.Geometry;
import de.sg.ogl.resource.Shader;
import org.joml.Matrix4f;

import java.util.ArrayList;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;

/**
 * Represents a MiniMapRenderer.
 */
public class MiniMapRenderer {

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
    private static final String SHADER_NAME = "miniMap";

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link ArrayList<Matrix4f>} object with model matrices.
     */
    private final ArrayList<Matrix4f> modelMatrices;

    /**
     * The color (3 floats per tile) for each tile.
     */
    private final float[] colors;

    /**
     * The {@link Context} object.
     */
    private final Context context;

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

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link MiniMapRenderer} object.
     *
     * @param modelMatrices {@link ArrayList<Matrix4f>}
     * @param colors The color (3 floats per tile) for each tile.
     * @param context {@link Context}
     * @throws Exception If an error is thrown.
     */
    public MiniMapRenderer(ArrayList<Matrix4f> modelMatrices, float[] colors, Context context) throws Exception {
        LOGGER.debug("Creates MiniMapRenderer object.");

        this.modelMatrices = modelMatrices;
        this.colors = colors;
        this.context = context;
        this.shader = context.engine.getResourceManager().loadResource(Shader.class, SHADER_NAME);
        this.vao = new Vao();
        this.vao.setDrawCount(DRAW_COUNT);
        this.instances = modelMatrices.size();

        initVao();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Renders the map.
     */
    public void render() {
        OpenGL.enableAlphaBlending();

        shader.bind();

        vao.bind();
        vao.drawInstanced(GL_TRIANGLES, instances);
        vao.unbind();

        Shader.unbind();

        OpenGL.disableBlending();
    }

    //-------------------------------------------------
    // Init Vao/Vbo
    //-------------------------------------------------

    /**
     * Setup {@link #vao}.
     */
    private void initVao() {
        addMeshVbo();
        addModelMatricesVbo();
        addColorVbo();
    }

    /**
     * Add a 2DQuad to a new {@link de.sg.ogl.buffer.Vbo}.
     */
    private void addMeshVbo() {
        var quadGeometry = context.engine.getResourceManager().loadGeometry(Geometry.GeometryId.QUAD_2D);
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

        // store model matrices (static draw)
        vbo.storeMatrix4fInstances(modelMatrices, instances);

        // set buffer layout
        vbo.addFloatAttribute(3, 4, 16, 0, true);
        vbo.addFloatAttribute(4, 4, 16, 4, true);
        vbo.addFloatAttribute(5, 4, 16, 8, true);
        vbo.addFloatAttribute(6, 4, 16, 12, true);

        // unbind vao
        vao.unbind();
    }

    /**
     * Add {@link #colors} to a new {@link de.sg.ogl.buffer.Vbo}.
     */
    private void addColorVbo() {
        // bind vao
        vao.bind();

        // create and add new vbo
        var colorVbo = vao.addVbo();

        // store index (dynamic draw)
        colorVbo.storeFloats(colors, GL_DYNAMIC_DRAW);

        // set buffer layout
        colorVbo.addFloatAttribute(7, 3, 3, 0, true);

        // unbind vao
        vao.unbind();
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Clean up MiniMapRenderer.");

        this.vao.cleanUp();
    }
}
