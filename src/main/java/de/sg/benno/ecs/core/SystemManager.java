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

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents the SystemManager.
 */
public class SystemManager {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * A list of all {@link System} objects used in this {@link Ecs}.
     */
    private final HashMap<Class<? extends System>, System> systems = new HashMap<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link SystemManager} object.
     */
    public SystemManager() {
        LOGGER.debug("Creates SystemManager object.");
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #systems}.
     *
     * @return {@link #systems}
     */
    public HashMap<Class<? extends System>, System> getSystems() {
        return systems;
    }

    /**
     * Returns the {@link System} object of the specified class.
     *
     * @param systemClass The specified class.
     *
     * @return The {@link System} object of the specified class or an empty {@link Optional}.
     */
    public <T extends System> Optional<T> getSystem(Class<T> systemClass) {
        if (systems.containsKey(systemClass)) {
            return Optional.of(systemClass.cast(systems.get(systemClass)));
        }

        return Optional.empty();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Handle input for all the systems.
     */
    public void input() {
        systems.forEach((k, v) -> v.input());
    }

    /**
     * Updates the systems.
     */
    public void update() {
        systems.forEach((k, v) -> v.update());
    }

    /**
     * Renders the systems.
     */
    public void render() {
        systems.forEach((k, v) -> v.render());
    }

    /**
     * Clean up systems.
     */
    public void cleanUp() {
        systems.forEach((k, v) -> v.cleanUp());
    }

    //-------------------------------------------------
    // Add / remove system
    //-------------------------------------------------

    /**
     * Adds a {@link System} object to {@link #systems}.
     *
     * @param system The {@link System} object to add.
     */
    public void addSystem(System system) {
        Objects.requireNonNull(system, "system must not be null");

        LOGGER.debug("Add {} to SystemManager.", system.getClass().getSimpleName());

        systems.put(system.getClass(), system);
    }

    /**
     * Removes the {@link System} object of the specified class.
     *
     * @param systemClass The specified class.
     */
    public void removeSystem(Class<? extends System> systemClass) {
        if (!systems.containsKey(systemClass)) {
            LOGGER.warn("The {} is not in SystemManager.", systemClass.getSimpleName());

            return;
        }

        LOGGER.debug("Remove {} from SystemManager.", systemClass.getSimpleName());

        systems.remove(systemClass);
    }

    /**
     * Removes a {@link System} object from {@link #systems}.
     *
     * @param system The {@link System} object to remove.
     */
    public void removeSystem(System system) {
        Objects.requireNonNull(system, "system must not be null");

        removeSystem(system.getClass());
    }
}
