/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl;

/**
 * Represents a custom runtime exception.
 */
public class OglRuntimeException extends RuntimeException {

    public OglRuntimeException() {
        super();
    }

    public OglRuntimeException(String message) {
        super(message);
    }
}
