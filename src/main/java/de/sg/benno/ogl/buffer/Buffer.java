/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl.buffer;

public interface Buffer {
    int getId();

    void createId();

    void bind();
    void unbind();

    void cleanUp();
}
