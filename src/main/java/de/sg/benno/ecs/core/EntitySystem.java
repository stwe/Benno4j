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

/**
 * Abstract class for processing sets of {@link Entity} objects.
 */
public abstract class EntitySystem implements System {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The parent {@link Ecs}.
     */
    private final Ecs ecs;

    /**
     * Used to set the priority of a {@link System}. Lower means it'll get executed first.
     */
    private final int priority;

    /**
     * The {@link Signature} for this {@link System}.
     */
    private final Signature signature;

    /**
     * A list of {@link Entity} objects processed by this {@link System}.
     */
    private final ArrayList<Entity> entities;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link System}.
     *
     * @param ecs The parent {@link Ecs}.
     * @param priority the priority of a {@link System}.
     * @param signatureComponentTypes A list of {@link Component} objects to create the {@link Signature}.
     */
    @SafeVarargs
    public EntitySystem(Ecs ecs, int priority, Class<? extends Component>... signatureComponentTypes) {
        this.ecs = Objects.requireNonNull(ecs, "ecs must not be null");
        this.priority = priority;
        this.signature = new Signature(signatureComponentTypes);
        this.signature.initSignatureBitSet(ecs.getAllComponentTypes());
        entities = this.ecs.getEntityManager().getEntitiesBySignature(this.signature);
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
     * Get {@link #priority}.
     *
     * @return {@link #priority}
     */
    public int getPriority() {
        return priority;
    }

    //-------------------------------------------------
    // Implement System
    //-------------------------------------------------

    @Override
    public Signature getSignature() {
        return signature;
    }

    @Override
    public ArrayList<Entity> getEntities() {
        return entities;
    }

    @Override
    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    @Override
    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }
}
