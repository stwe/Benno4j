/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021, stwe <https://github.com/stwe/Benno4j>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.sg.benno.ogl;

import java.io.IOException;
import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents an OglApplication.
 */
public abstract class OglApplication implements Application {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * The file path to the app config file.
     */
    private static final String APPLICATION_CONFIG_FILE = "/config.properties";

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

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
     * @throws IllegalAccessException If an access error is thrown.
     */
    public OglApplication() throws IOException, IllegalAccessException {
        LOGGER.debug("Creates OglApplication object and load config.");

        ConfigLoader.load(Config.class, APPLICATION_CONFIG_FILE);
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
