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
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

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

    /**
     * The {@link Vao} objects for each {@link Zoom}.
     */
    private final HashMap<Zoom, Vao> vaos = new HashMap<>();

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
            createModelMatrices(zoom);
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

        shader.setUniform("projection", context.engine.getWindow().getOrthographicProjectionMatrix());
        shader.setUniform("view", camera.getViewMatrix());

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

    private void addVao(Zoom zoom) {
        createVao(zoom);
        addMeshVbo(zoom);
        addModelMatricesVbo(zoom);
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
