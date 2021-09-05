/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl;

/**
 * Encapsulates the game logic.
 */
public interface Application {
    void init(Object... params) throws Exception;
    void input();
    void update();
    void render();
    void renderImGui() throws Exception;
    void cleanUp();
}
