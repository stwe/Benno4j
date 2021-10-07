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

/**
 * Represents an EntityManager.
 */
public class EntityManager {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The parent {@link Ecs}.
     */
    private final Ecs ecs;

    /**
     * A list with <i>all</i> the {@link Entity} objects.
     */
    private final ArrayList<Entity> entities = new ArrayList<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link EntityManager} object.
     *
     * @param ecs The parent {@link Ecs} object.
     */
    public EntityManager(Ecs ecs) {
        this.ecs = ecs;
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #ecs}.
     *
     * @return {@link #ecs}
     */
    public Ecs getEcs() {
        return ecs;
    }

    /**
     * Get {@link #entities}.
     *
     * @return {@link #entities}
     */
    public ArrayList<Entity> getEntities() {
        return entities;
    }

    //-------------------------------------------------
    // Add / remove
    //-------------------------------------------------

    /**
     * Adds an new {@link Entity}.
     *
     * @return The newly created {@link Entity}.
     */
    public Entity createEntity() {
        var entity = new Entity(this);
        entities.add(entity);

        // todo update Systems bei Aufruf von addComponent

        return entity;
    }

    /**
     * Removes an {@link Entity}.
     *
     * @param entity The {@link Entity} to remove.
     */
    public void removeEntity(Entity entity) {

        // todo update Systems bei Aufruf von removeComponent
        // todo clean up components, update systems

        entities.remove(entity);
    }

    //-------------------------------------------------
    // Iterate
    //-------------------------------------------------

    // todo: System method

    /**
     * Get {@link Entity} objects matches a given {@link Signature}.
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
