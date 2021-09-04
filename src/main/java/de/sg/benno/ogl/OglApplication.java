/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl;

import java.io.IOException;
import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents an OglApplication.
 */
public abstract class OglApplication implements Application {

    /**
     * The parent {@link OglEngine}.
     */
    private OglEngine engine;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link OglApplication} object.
     *
     * @throws IOException If an I/O error is thrown.
     * @throws IllegalAccessException If an error is thrown.
     */
    public OglApplication() throws IOException, IllegalAccessException {
        LOGGER.debug("Creates OglApplication object and load config.");

        ConfigLoader.load(Config.class, "/config.properties");
        LOGGER.debug("Configuration loaded successfully.");
        LOGGER.debug("Title: {}", Config.TITLE);
        LOGGER.debug("Width: {}", Config.WIDTH);
        LOGGER.debug("Height: {}", Config.HEIGHT);
        LOGGER.debug("VSync: {}", Config.V_SYNC);
        LOGGER.debug("Fov: {}", Config.FOV);
        LOGGER.debug("Near: {}", Config.NEAR);
        LOGGER.debug("Far: {}", Config.FAR);
        LOGGER.debug("FPS: {}", Config.FPS);
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #engine}.
     *
     * @return {@link #engine}
     */
    public OglEngine getEngine() {
        return engine;
    }

    //-------------------------------------------------
    // Setter
    //-------------------------------------------------

    /**
     * Set {@link #engine}.
     *
     * @param engine {@link OglEngine}
     */
    public void setEngine(OglEngine engine) {
        this.engine = Objects.requireNonNull(engine, "engine must not be null");
    }
}
