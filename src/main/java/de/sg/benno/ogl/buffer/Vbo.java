/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl.buffer;

import de.sg.benno.ogl.OglRuntimeException;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.util.ArrayList;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

/**
 * Represents a Vertex Buffer Object.
 */
public class Vbo implements Buffer {

    /**
     * Stores the handle of the Vbo.
     */
    private int id;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Vbo} object.
     */
    public Vbo() {
        LOGGER.debug("Creates Vbo object.");

        createId();
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
        id = glGenBuffers();
        if (id == 0) {
            throw new OglRuntimeException("Vbo creation has failed.");
        }

        LOGGER.debug("A new Vbo was created. The Id is {}.", id);
    }

    @Override
    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, id);
    }

    @Override
    public void unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void cleanUp() {
        LOGGER.debug("Clean up Vbo.");

        unbind();

        if (id > 0) {
            glDeleteBuffers(id);
            LOGGER.debug("Vbo {} was deleted.", id);
        }
    }

    //-------------------------------------------------
    // Store data
    //-------------------------------------------------

    /**
     * Stores {@link Matrix4f} objects in this {@link Vbo}.
     *
     * @param matrices The {@link Matrix4f} objects.
     * @param usage Specifies the expected usage pattern of the data store
     *              (GL_STATIC_DRAW or GL_DYNAMIC_DRAW).
     */
    public void storeMatrix4f(ArrayList<Matrix4f> matrices, int usage) {
        if (usage != GL_STATIC_DRAW && usage != GL_DYNAMIC_DRAW) {
            throw new OglRuntimeException("Invalid usage given.");
        }

        bind();

        var fb = BufferUtils.createFloatBuffer(matrices.size() * 16);
        for (var matrix : matrices) {
            float[] t = new float[16];
            fb.put(matrix.get(t));
        }
        fb.flip();

        glBufferData(GL_ARRAY_BUFFER, fb, usage);

        unbind();
    }

    /**
     * Stores {@link Matrix4f} objects in this {@link Vbo}.
     *
     * @param matrices The {@link Matrix4f} objects.
     */
    public void storeMatrix4f(ArrayList<Matrix4f> matrices) {
        storeMatrix4f(matrices, GL_STATIC_DRAW);
    }

    //-------------------------------------------------
    // Attributes
    //-------------------------------------------------

    /**
     *
     * @param index
     * @param nrOfFloatComponents
     * @param nrOfAllFloats
     * @param startPoint
     * @param instancedRendering
     */
    public void addFloatAttribute(
            int index,
            int nrOfFloatComponents,
            int nrOfAllFloats,
            int startPoint,
            boolean instancedRendering
    ) {
        bind();

        glEnableVertexAttribArray(index);
        glVertexAttribPointer(
                index,
                nrOfFloatComponents,
                GL_FLOAT,
                false,
                nrOfAllFloats * Float.BYTES,
                (long) startPoint * Float.BYTES
        );

        if (instancedRendering) {
            glVertexAttribDivisor(index, 1);
        }

        unbind();
    }

    /**
     *
     * @param index
     * @param nrOfFloatComponents
     * @param nrOfAllFloats
     * @param startPoint
     */
    public void addFloatAttribute(
            int index,
            int nrOfFloatComponents,
            int nrOfAllFloats,
            int startPoint
    ) {
        addFloatAttribute(index, nrOfFloatComponents, nrOfAllFloats, startPoint, false);
    }
}
