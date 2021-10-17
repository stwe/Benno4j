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

import java.util.ArrayList;
import java.util.Objects;

import static de.sg.benno.ecs.core.EntityManager.EntityTodo.TodoType.REMOVE;
import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents an EntityManager.
 */
public class EntityManager {

    //-------------------------------------------------
    // Types
    //-------------------------------------------------

    /**
     * Represents an EntityTodo.
     */
    public static class EntityTodo {
        public enum TodoType {
            REMOVE, UPDATE_SYSTEMS
        }

        public Entity entity;
        public TodoType todoType;
        public boolean pending;

        public EntityTodo(Entity entity, TodoType todoType, boolean pending) {
            this.entity = entity;
            this.todoType = todoType;
            this.pending = pending;
        }

        public EntityTodo(Entity entity, TodoType todoType) {
            this(entity, todoType, true);
        }
    }

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link SystemManager} object.
     */
    private final SystemManager systemManager;

    /**
     * A list with <i>all</i> the {@link Entity} objects.
     */
    private final ArrayList<Entity> entities = new ArrayList<>();

    /**
     * A list of {@link EntityTodo} objects.
     */
    private final ArrayList<EntityTodo> entityTodos = new ArrayList<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link EntityManager} object.
     *
     * @param systemManager The {@link SystemManager} object.
     */
    public EntityManager(SystemManager systemManager) {
        LOGGER.debug("Creates EntityManager object.");

        this.systemManager = Objects.requireNonNull(systemManager, "systemManager must not be null");
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #entities}.
     *
     * @return {@link #entities}
     */
    public ArrayList<Entity> getAllEntities() {
        return entities;
    }

    /**
     * Get {@link #entityTodos}.
     *
     * @return {@link #entityTodos}
     */
    public ArrayList<EntityTodo> getEntityTodos() {
        return entityTodos;
    }

    //-------------------------------------------------
    // Add / remove entities
    //-------------------------------------------------

    /**
     * Adds an new {@link Entity}.
     *
     * @return The newly created {@link Entity}.
     */
    public Entity createEntity() {
        var entity = new Entity(this);
        entities.add(entity);

        return entity;
    }

    /**
     * Removes an {@link Entity}.
     *
     * @param entity The {@link Entity} to remove.
     */
    public void removeEntity(Entity entity) {
        // add (only) an REMOVE entityTodo
        addEntityTodo(new EntityManager.EntityTodo(entity, REMOVE));
    }

    //-------------------------------------------------
    // Add / remove entityTodo objects
    //-------------------------------------------------

    /**
     * Adds an {@link EntityTodo}.
     *
     * @param entityTodo The {@link EntityTodo} object to add.
     */
    public void addEntityTodo(EntityTodo entityTodo) {
        entityTodos.add(entityTodo);
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Process entity todos.
     */
    public void processEntityTodos() {
        for (var entityTodo : entityTodos) {
            if (entityTodo.pending) {
                switch (entityTodo.todoType) {
                    case REMOVE:
                        // remove entity from all systems
                        removeEntityFromAllSystems(entityTodo.entity);
                        // remove entity from this manager
                        entities.remove(entityTodo.entity);
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

        // remove job
        entityTodos.removeIf(entityTodo -> (!entityTodo.pending));
    }

    //-------------------------------------------------
    // Add / remove to systems
    //-------------------------------------------------

    /**
     * Adds an {@link Entity} object to all {@link System} objects which matches system signature.
     *
     * @param entity An {@link Entity} object.
     */
    public void addEntityToSystems(Entity entity) {
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
     * @param entity An {@link Entity} object.
     */
    public void removeEntityFromAllSystems(Entity entity) {
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
    // Iterate
    //-------------------------------------------------

    /**
     * Get {@link Entity} objects from {@link #entities} matches a given {@link Signature}.
     *
     * @param signature {@link Signature}
     *
     * @return A list with the {@link Entity} objects.
     */
    public ArrayList<Entity> getEntitiesBySignature(Signature signature) {
        var result = new ArrayList<Entity>();

        for (var entity : entities) {
            if (Entity.matchesSignature(entity, signature)) {
                result.add(entity);
            }
        }

        return result;
    }
}
