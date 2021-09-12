/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl.buffer;

import de.sg.benno.ogl.OglRuntimeException;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;

/**
 * Represents a Vertex Array Object.
 */
public class Vao implements Buffer {

    /**
     * Stores the handle of the Vao.
     */
    private int id;

    /**
     * The Vbo buffers assigned to the Vao.
     */
    private final ArrayList<Vbo> vbos = new ArrayList<>();

    /**
     * Vertices to draw.
     */
    private int drawCount;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Vao} object.
     */
    public Vao() {
        LOGGER.debug("Creates Vao object.");

        createId();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #drawCount}.
     *
     * @return {@link #drawCount}
     */
    public int getDrawCount() {
        return drawCount;
    }

    //-------------------------------------------------
    // Setter
    //-------------------------------------------------

    /**
     * Set {@link #drawCount}.
     *
     * @param drawCount The vertices to draw.
     */
    public void setDrawCount(int drawCount) {
        this.drawCount = drawCount;
    }

    //-------------------------------------------------
    // Implement Buffer
    //-------------------------------------------------

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void createId() {
        id = glGenVertexArrays();
        if (id == 0) {
            throw new OglRuntimeException("Vao creation has failed.");
        }

        LOGGER.debug("A new Vao was created. The Id is {}.", id);
    }

    @Override
    public void bind() {
        glBindVertexArray(id);
    }

    @Override
    public void unbind() {
        glBindVertexArray(0);
    }

    @Override
    public void cleanUp() {
        LOGGER.debug("Clean up Vao.");

        glDisableVertexAttribArray(0);

        for (var vbo : vbos) {
            vbo.cleanUp();
        }

        unbind();

        if (id > 0) {
            glDeleteVertexArrays(id);
            LOGGER.debug("Vao {} was deleted.", id);
        }
    }

    //-------------------------------------------------
    // Add Vbo
    //-------------------------------------------------

    /**
     * Adds a new {@link Vbo} object.
     *
     * @return {@link Vbo}
     */
    public Vbo addVbo() {
        var vbo = new Vbo();
        vbos.add(vbo);

        return vbo;
    }

    /**
     * Adds a new {@link Vbo} object and copies vertices representing a 2D quad into the buffer's memory.
     */
    public void add2DQuadVbo() {
        // vertices of a quad
        var vertices = new float[] {
                // pos      // uv
                0.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 0.0f,

                0.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 0.0f
        };

        // bind this Vao
        bind();

        // create a new Vbo
        var vbo = addVbo();

        // bind the new Vbo
        vbo.bind();

        // store vertices
        FloatBuffer verticesBuffer = null;
        try {
            verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
            verticesBuffer.put(vertices).flip();

            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

            // enable location 0 (position)
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0L);

            // enable location 1 (uv)
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        } finally {
            if (verticesBuffer != null) {
                MemoryUtil.memFree(verticesBuffer);
            }
        }

        // unbind Vbo
        vbo.unbind();

        // unbind Vao
        unbind();

        // set draw count
        setDrawCount(6);
    }

    //-------------------------------------------------
    // Draw
    //-------------------------------------------------

    /**
     * Draw multiple instances with a single call.
     *
     * @param drawMode The OpenGL primitive type to draw.
     * @param first The starting index of the vertex array.
     * @param instances The number of instances to render.
     */
    public void drawInstanced(int drawMode, int first, int instances) {
        glDrawArraysInstanced(drawMode, first, drawCount, instances);
    }

    /**
     * Draw multiple instances with a single call.
     *
     * @param drawMode The OpenGL primitive type to draw.
     * @param instances The number of instances to render.
     */
    public void drawInstanced(int drawMode, int instances) {
        drawInstanced(drawMode, 0, instances);
    }

    /**
     * Draw multiple instances with a single call.
     *
     * @param instances The number of instances to render.
     */
    public void drawInstanced(int instances) {
        drawInstanced(GL_TRIANGLES, 0, instances);
    }

    /**
     * Draws primitives using the currently active shader.
     *
     * @param drawMode The OpenGL primitive type to draw.
     * @param first The starting index of the vertex array.
     */
    public void drawPrimitives(int drawMode, int first) {
        glDrawArrays(drawMode, first, drawCount);
    }

    /**
     * Draws primitives using the currently active shader.
     *
     * @param drawMode The OpenGL primitive type to draw.
     */
    public void drawPrimitives(int drawMode) {
        drawPrimitives(drawMode, 0);
    }

    /**
     * Draws primitives using the currently active shader.
     */
    public void drawPrimitives() {
        drawPrimitives(GL_TRIANGLES, 0);
    }
}
