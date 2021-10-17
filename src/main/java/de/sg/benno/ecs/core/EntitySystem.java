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
 * Abstract class for processing sets of {@link Entity} objects.
 */
public abstract class EntitySystem implements System {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link Signature} for this {@link System}.
     */
    private final Signature signature;

    /**
     * A list of {@link Entity} objects processed by this {@link System}.
     */
    private final ArrayList<Entity> entities = new ArrayList<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link System}.
     *
     * @param signatureComponentTypes A list of {@link Component} objects to create the {@link Signature}.
     */
    @SafeVarargs
    public EntitySystem(Class<? extends Component>... signatureComponentTypes) {
        this.signature = new Signature(signatureComponentTypes);
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
