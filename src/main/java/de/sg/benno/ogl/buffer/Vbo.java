/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl.buffer;

import de.sg.benno.ogl.OglRuntimeException;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;

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
}
