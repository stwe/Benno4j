/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl;

import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Contains the game loop code.
 */
public class OglEngine implements Runnable {

    /**
     * The parent {@link OglApplication}.
     */
    private final OglApplication application;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link OglEngine} object.
     *
     * @param application The parent {@link OglApplication}.
     */
    public OglEngine(OglApplication application) {
        LOGGER.debug("Creates OglEngine object.");

        this.application = Objects.requireNonNull(application, "application must not be null");
        this.application.setEngine(this);
    }

    //-------------------------------------------------
    // Implement Runnable
    //-------------------------------------------------

    @Override
    public void run() {
        LOGGER.debug("Running OglEngine.");

        try {
            init();
            gameLoop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanUp();
        }

        LOGGER.debug("Goodbye ...");
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Initializing engine.
     *
     * @throws Exception If an error is thrown.
     */
    private void init() throws Exception {
        LOGGER.debug("Initializing OglEngine.");

        application.init();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * The game loop.
     *
     * @throws Exception If an error is thrown.
     */
    private void gameLoop() throws Exception {
        LOGGER.debug("Starting the game loop.");
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    private void cleanUp() {
        LOGGER.debug("Clean up OglEngine.");

        application.cleanUp();
    }
}
