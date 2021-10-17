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

        this.systemManager = new SystemManager();
        this.entityManager = new EntityManager();
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
        processEntityTodos();
        systemManager.update();
    }

    /**
     * Renders the systems.
     */
    public void render() {
        processEntityTodos();
        systemManager.render();
    }

    /**
     * Clean up systems.
     */
    public void cleanUp() {
        LOGGER.debug("Clean up Ecs.");

        systemManager.cleanUp();
    }

    /**
     * Removes entities from the {@link EntityManager} and
     * adds and removes entities from all {@link System} objects.
     */
    public void processEntityTodos() {
        for (var entityTodo : entityManager.getEntityTodos()) {
            if (entityTodo.pending) {
                switch (entityTodo.todoType) {
                    case REMOVE:
                        // remove entity from all systems
                        removeEntityFromAllSystems(entityTodo.entity);
                        // remove entity from entity manager
                        entityManager.getAllEntities().remove(entityTodo.entity);
                        entityTodo.pending = false;
                        break;
                    case UPDATE_SYSTEMS:
                        // a component was added or removed
                        removeEntityFromAllSystems(entityTodo.entity);
                        addEntityToSystems(entityTodo.entity);
                        entityTodo.pending = false;
                        break;
                    default:
                }
            }
        }

        // remove all finished entityTodo objects
        entityManager.removeFinishedEntityTodos();
    }

    //-------------------------------------------------
    // Add / remove entities to systems
    //-------------------------------------------------

    /**
     * Adds an {@link Entity} object to all {@link System} objects which matches system signature.
     *
     * @param entity The {@link Entity} object to add.
     */
    private void addEntityToSystems(Entity entity) {
        systemManager.getSystems().forEach(
            (k, v) -> {
                if (Entity.matchesSignature(entity, v.getSignature())) {
                    if (!v.getEntities().contains(entity)) {
                        v.addEntity(entity);
                        LOGGER.debug("Entity {} added to System {}.", entity.debugName, k.getSimpleName());
                    } else {
                        LOGGER.debug("Entity {} to be add is already in System {}.", entity.debugName, k.getSimpleName());
                    }
                }
            }
        );
    }

    /**
     * Removes an {@link Entity} object from all {@link System} objects.
     *
     * @param entity The {@link Entity} object to remove.
     */
    private void removeEntityFromAllSystems(Entity entity) {
        systemManager.getSystems().forEach(
            (k, v) -> {
                if (v.getEntities().contains(entity)) { // check for debug log output
                    v.removeEntity(entity);
                    LOGGER.debug("Entity {} removed from System {}.", entity.debugName, k.getSimpleName());
                }
            }
        );
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
