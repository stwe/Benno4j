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

package de.sg.benno.ecs.core;

import java.util.BitSet;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents the Entity Component System.
 */
public class Ecs {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link EntityManager} object.
     */
    private final EntityManager entityManager;

    /**
     * The {@link SystemManager} object.
     */
    private final SystemManager systemManager;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Ecs} object.
     */
    public Ecs() {
        LOGGER.debug("Creates Ecs object.");

        this.entityManager = new EntityManager();
        this.systemManager = new SystemManager();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #entityManager}.
     *
     * @return {@link #entityManager}
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Get {@link #systemManager}.
     *
     * @return {@link #systemManager}
     */
    public SystemManager getSystemManager() {
        return systemManager;
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Handle input for all the systems.
     */
    public void input() {
        systemManager.input();
    }

    /**
     * Updates the systems.
     */
    public void update() {
        systemManager.update();
    }

    /**
     * Renders the systems.
     */
    public void render() {
        systemManager.render();
    }

    /**
     * Clean up systems.
     */
    public void cleanUp() {
        LOGGER.debug("Clean up Ecs.");

        systemManager.cleanUp();
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * {@link BitSet} to {@link String}.
     *
     * @param bits A {@link BitSet}.
     *
     * @return String
     */
    public static String getBitsString (BitSet bits) {
        var stringBuilder = new StringBuilder();

        var numBits = bits.length();
        for (var i = 0; i < numBits; i++) {
            stringBuilder.append(bits.get(i) ? "1" : "0");
        }

        return stringBuilder.toString();
    }
}
