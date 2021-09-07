/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl.resource;

/**
 * Represents a Uniform to pass data from the CPU to the shaders on the GPU.
 */
public class Uniform {

    /**
     * If it a set of Uniform variables.
     */
    public boolean isUniformBlock = false;

    /**
     * The data type.
     */
    public String type;

    /**
     * The name of the Uniform.
     */
    public String name;
}
