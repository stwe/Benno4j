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

/**
 * Represents an EntityTodo.
 * These jobs are created while iterating over all entities.
 * This means that entities can be removed from the EntityManager or the systems after the loop.
 * Most of the time this is the case when a component has been added or removed from the entity.
 */
public class EntityTodo {

    //-------------------------------------------------
    // Types
    //-------------------------------------------------

    /**
     * REMOVE = remove entity from entity manager
     * UPDATE_SYSTEMS = add / remove entity from systems
     */
    public enum TodoType {
        REMOVE, UPDATE_SYSTEMS
    }

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * An {@link Entity} object.
     */
    public Entity entity;

    /**
     * A {@link TodoType}.
     */
    public TodoType todoType;

    /**
     * The job is done or not done.
     */
    public boolean pending;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link EntityTodo} object.
     *
     * @param entity An {@link Entity} object.
     * @param todoType A {@link TodoType}.
     * @param pending The job is done or not done.
     */
    public EntityTodo(Entity entity, TodoType todoType, boolean pending) {
        this.entity = entity;
        this.todoType = todoType;
        this.pending = pending;
    }

    /**
     * Constructs a new {@link EntityTodo} object.
     *
     * @param entity An {@link Entity} object.
     * @param todoType A {@link TodoType}.
     */
    public EntityTodo(Entity entity, TodoType todoType) {
        this(entity, todoType, true);
    }
}
