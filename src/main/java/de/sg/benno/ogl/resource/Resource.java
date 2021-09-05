/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl.resource;

public interface Resource {
    void load() throws Exception;

    void createId();
    void bind();

    void cleanUp();
}
