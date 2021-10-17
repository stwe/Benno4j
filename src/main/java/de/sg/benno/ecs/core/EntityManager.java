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

import static de.sg.benno.ecs.core.EntityTodo.TodoType.REMOVE;
import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents an EntityManager.
 */
public class EntityManager {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

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
     */
    public EntityManager() {
        LOGGER.debug("Creates EntityManager object.");
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
     * Adds a new {@link Entity} to {@link #entities}.
     *
     * @return The newly created {@link Entity}.
     */
    public Entity createEntity() {
        var entity = new Entity(this);
        entities.add(entity);

        return entity;
    }

    /**
     * Removes an {@link Entity} from {@link #entities}.
     *
     * @param entity The {@link Entity} to remove.
     */
    public void removeEntity(Entity entity) {
        // add (only) an REMOVE entityTodo
        addEntityTodo(new EntityTodo(entity, REMOVE));
    }

    //-------------------------------------------------
    // Add / remove entityTodo
    //-------------------------------------------------

    /**
     * Adds an {@link EntityTodo} to {@link #entityTodos}.
     *
     * @param entityTodo The {@link EntityTodo} object to add.
     */
    public void addEntityTodo(EntityTodo entityTodo) {
        entityTodos.add(entityTodo);
    }

    /**
     * Removes an {@link EntityTodo} from {@link #entityTodos}.
     *
     * @param entityTodo The {@link EntityTodo} object to remove.
     */
    public void removeEntityTodo(EntityTodo entityTodo) {
        entityTodos.remove(entityTodo);
    }

    /**
     * Removes all finished {@link EntityTodo} objects from {@link #entityTodos}.
     */
    public void removeFinishedEntityTodos() {
        entityTodos.removeIf(entityTodo -> (!entityTodo.pending));
    }

    //-------------------------------------------------
    // Iterate
    //-------------------------------------------------

    /**
     * Get {@link Entity} objects from {@link #entities} matches a given {@link Signature}.
     *
     * @param signature The {@link Signature} object.
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
