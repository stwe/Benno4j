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

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.function.ToIntFunction;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glVertexAttribIPointer;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

/**
 * Represents a Vertex Buffer Object.
 */
public class Vbo implements Buffer {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

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
    // Store data by calling glBufferData
    //-------------------------------------------------

    /**
     * Stores {@link Matrix4f} objects in this {@link Vbo} by calling
     * <i>glBufferData</i>, which allocates a piece of GPU memory and
     * adds data into this memory
     *
     * @param matrices A list of {@link Matrix4f} objects.
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
     * Stores {@link Matrix4f} objects in this {@link Vbo} by calling
     * <i>glBufferData</i>, which allocates a piece of GPU memory and
     * adds data into this memory
     *
     * @param matrices A list of {@link Matrix4f} objects.
     */
    public void storeMatrix4f(ArrayList<Matrix4f> matrices) {
        storeMatrix4f(matrices, GL_STATIC_DRAW);
    }

    /**
     * Stores {@link Float} objects in this {@link Vbo} by calling
     * <i>glBufferData</i>, which allocates a piece of GPU memory and
     * adds data into this memory
     *
     * @param data A list of {@link Float} objects.
     * @param usage Specifies the expected usage pattern of the data store
     *              (GL_STATIC_DRAW or GL_DYNAMIC_DRAW).
     */
    public void storeFloat(ArrayList<Float> data, int usage) {
        if (usage != GL_STATIC_DRAW && usage != GL_DYNAMIC_DRAW) {
            throw new OglRuntimeException("Invalid usage given.");
        }

        bind();

        var values = new float[data.size()];
        for (var i = 0; i < values.length; i++) {
            values[i] = data.get(i);
        }

        glBufferData(GL_ARRAY_BUFFER, values, usage);

        unbind();
    }

    /**
     * Stores {@link Integer} objects in this {@link Vbo} by calling
     * <i>glBufferData</i>, which allocates a piece of GPU memory and
     * adds data into this memory
     *
     * @param mapper A function that produces an int-valued result.
     * @param data A list of {@link Integer} objects.
     * @param usage Specifies the expected usage pattern of the data store
     *              (GL_STATIC_DRAW or GL_DYNAMIC_DRAW).
     */
    public void storeInteger(ToIntFunction<Integer> mapper, ArrayList<Integer> data, int usage) {
        if (usage != GL_STATIC_DRAW && usage != GL_DYNAMIC_DRAW) {
            throw new OglRuntimeException("Invalid usage given.");
        }

        bind();

        var ib = BufferUtils.createIntBuffer(data.size());
        ib.put(data.stream().mapToInt(mapper).toArray());
        ib.flip();

        glBufferData(GL_ARRAY_BUFFER, ib, usage);

        unbind();
    }

    /**
     * Stores {@link Integer} objects in this {@link Vbo} by calling
     * <i>glBufferData</i>, which allocates a piece of GPU memory and
     * adds data into this memory
     *
     * @param data A list of {@link Integer} objects.
     * @param usage Specifies the expected usage pattern of the data store
     *              (GL_STATIC_DRAW or GL_DYNAMIC_DRAW).
     */
    public void storeInteger(ArrayList<Integer> data, int usage) {
        storeInteger(i -> i, data, usage);
    }

    /**
     * Stores {@link Integer} objects in this {@link Vbo} by calling
     * <i>glBufferData</i>, which allocates a piece of GPU memory and
     * adds data into this memory
     *
     * @param data A list of {@link Integer} objects.
     */
    public void storeInteger(ArrayList<Integer> data) {
        storeInteger(data, GL_STATIC_DRAW);
    }

    //-------------------------------------------------
    // Store data by calling glBufferSubData
    //-------------------------------------------------

    /**
     * Fill specific regions of the buffer by calling <i>glBufferSubData</i>.
     *
     * @param offset An offset that specifies from where we want to fill the buffer.
     * @param ib The new data.
     */
    public void storeSubData(long offset, IntBuffer ib) {
        bind();
        glBufferSubData(GL_ARRAY_BUFFER, offset, ib);
        unbind();
    }

    /**
     * Fill specific regions of the buffer by calling <i>glBufferSubData</i>.
     *
     * @param offset An offset that specifies from where we want to fill the buffer.
     * @param data The new data.
     */
    public void storeSubData(long offset, float[] data) {
        bind();
        glBufferSubData(GL_ARRAY_BUFFER, offset, data);
        unbind();
    }

    /**
     * Fill specific regions of the buffer by calling <i>glBufferSubData</i>.
     *
     * @param data The new data.
     */
    public void storeSubData(float[] data) {
        storeSubData(0, data);
    }

    //-------------------------------------------------
    // Attributes
    //-------------------------------------------------

    /**
     * Specified how OpenGL should interpret the vertex data.
     *
     * @param index The location of the vertex attribute.
     * @param nrOfFloatComponents The size of the vertex attribute.
     * @param nrOfAllFloats The space between consecutive vertex attributes.
     * @param startPoint The offset of where the position data begins in the buffer.
     * @param instancedRendering Call <i>glVertexAttribDivisor</i> if true.
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
     * Specified how OpenGL should interpret the vertex data.
     *
     * @param index The location of the vertex attribute.
     * @param nrOfFloatComponents The size of the vertex attribute.
     * @param nrOfAllFloats The space between consecutive vertex attributes.
     * @param startPoint The offset of where the position data begins in the buffer.
     */
    public void addFloatAttribute(
            int index,
            int nrOfFloatComponents,
            int nrOfAllFloats,
            int startPoint
    ) {
        addFloatAttribute(index, nrOfFloatComponents, nrOfAllFloats, startPoint, false);
    }

    /**
     * Specified how OpenGL should interpret the vertex data.
     *
     * @param index The location of the vertex attribute.
     * @param nrOfIntComponents The size of the vertex attribute.
     * @param nrOfAllInts The space between consecutive vertex attributes.
     * @param startPoint The offset of where the position data begins in the buffer.
     * @param instancedRendering Call <i>glVertexAttribDivisor</i> if true.
     */
    public void addIntAttribute(
            int index,
            int nrOfIntComponents,
            int nrOfAllInts,
            int startPoint,
            boolean instancedRendering
    ) {
        bind();

        glEnableVertexAttribArray(index);
        glVertexAttribIPointer(index, nrOfIntComponents, GL_INT, nrOfAllInts * Integer.BYTES, (long) startPoint * Integer.BYTES);

        if (instancedRendering) {
            glVertexAttribDivisor(index, 1);
        }

        unbind();
    }

    /**
     * Specified how OpenGL should interpret the vertex data.
     *
     * @param index The location of the vertex attribute.
     * @param nrOfIntComponents The size of the vertex attribute.
     * @param nrOfAllInts The space between consecutive vertex attributes.
     * @param startPoint The offset of where the position data begins in the buffer.
     */
    public void addIntAttribute(
            int index,
            int nrOfIntComponents,
            int nrOfAllInts,
            int startPoint
    ) {
        addIntAttribute(index, nrOfIntComponents, nrOfAllInts, startPoint, false);
    }
}
