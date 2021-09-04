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

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The parent {@link OglApplication}.
     */
    private final OglApplication application;

    /**
     * A {@link Window} object.
     */
    private final Window window;

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

        this.window = new Window();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link Window}.
     *
     * @return {@link Window}
     */
    public Window getWindow() {
        return window;
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

        window.init();
        application.init();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    private void input() {
        application.input();
    }

    private void update(float dt) {
        application.update(dt);
    }

    private void render() throws Exception {
        startFrame();
        frame();
        endFrame();
    }

    private void startFrame() {
        OpenGL.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        OpenGL.clear();
    }

    private void frame() throws Exception {
        application.render();
    }

    private void endFrame() {
        window.update();
    }

    //-------------------------------------------------
    // Game loop
    //-------------------------------------------------

    /**
     * The game loop.
     *
     * @throws Exception If an error is thrown.
     */
    private void gameLoop() throws Exception {
        LOGGER.debug("Starting the game loop.");

        var lastTime = System.nanoTime();
        var timer = System.currentTimeMillis();
        final var frameTime = 1000000000.0 / Config.FPS;
        final var frameTimeS = 1.0f / (float)Config.FPS;
        var dt = 0.0;
        var fps = 0;
        var updates = 0;

        while(!window.windowShouldClose()) {
            var now = System.nanoTime();
            dt += (now - lastTime) / frameTime;
            lastTime = now;

            input();

            while (dt >= 1.0) {
                update(frameTimeS);
                updates++;
                dt--;
            }

            render();
            fps++;

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                window.setTitle(window.getTitle() + "  |  " + fps + " frames  |  " + updates + " updates");
                updates = 0;
                fps = 0;
            }

            // todo vsync
        }
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    private void cleanUp() {
        LOGGER.debug("Clean up OglEngine.");

        window.cleanUp();
        application.cleanUp();
    }
}
