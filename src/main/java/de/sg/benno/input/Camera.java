/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.input;

import de.sg.benno.ogl.OglEngine;
import de.sg.benno.ogl.camera.OrthographicCamera;
import de.sg.benno.ogl.state.StateContext;
import de.sg.benno.renderer.Zoom;

/**
 * Represents a camera.
 */
public class Camera extends OrthographicCamera {

    /**
     * Constructs a new {@link OrthographicCamera} object.
     *
     * @param x The start x position in tile units.
     * @param y The start y position in tile units.
     * @param engine The parent {@link OglEngine} object.
     * @param zoom The curreent {@link Zoom}
     * @throws Exception If an error is thrown.
     */
    public Camera(int x, int y, OglEngine engine, Zoom zoom) throws Exception {
        super(x, y, engine, zoom);
    }
}
