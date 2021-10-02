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
import java.util.BitSet;
import java.util.HashMap;
import java.util.Optional;

/**
 * Represents an Entity, which is a simple container of {@link Component} objects.
 */
public class Entity {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The parent {@link EntityManager}.
     */
    private final EntityManager entityManager;

    /**
     * The list of {@link Component} objects.
     */
    private final ArrayList<Component> components = new ArrayList<>();

    /**
     * Provides fast access to {@link Component} objects based on its type.
     */
    private final HashMap<Class<? extends Component>, Component> componentsCache = new HashMap<>();

    /**
     * The {@link BitSet} signature for added {@link Component} objects.
     */
    private final BitSet signature = new BitSet();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Entity} object.
     *
     * @param entityManager The parent {@link EntityManager}.
     */
    public Entity(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #components}.
     *
     * @return {@link #components}
     */
    public ArrayList<Component> getComponents() {
        return components;
    }

    /**
     * Get {@link #signature}.
     *
     * @return {@link #signature}
     */
    public BitSet getSignatureBitSet() {
        return signature;
    }

    //-------------------------------------------------
    // Get component
    //-------------------------------------------------

    /**
     * Returns the {@link Component} object of the specified class if this {@link Entity} has one added,
     * empty Optional if it doesn't.
     *
     * @param componentClass The specified class.
     * @return The {@link Component} object of the specified class.
     */
    public <T extends Component> Optional<T> getComponent(Class<T> componentClass) {
        var component = componentsCache.get(componentClass);
        if (component != null) {
            return Optional.of(componentClass.cast(component));
        }

        return Optional.empty();
    }

    //-------------------------------------------------
    // Add component
    //-------------------------------------------------

    /**
     * Adds the given {@link Component} object to this {@link Entity}.
     *
     * @param component The {@link Component} object to add.
     */
    public void addComponent(Component component) {
        // add to list
        components.add(component);

        // add to cache
        componentsCache.put(component.getClass(), component);

        // update signature bits
        signature.set(entityManager.getEcs().getComponentIndex(component.getClass()));
    }

    /**
     * Adds and returns the given {@link Component} object to this {@link Entity}.
     *
     * @param component The {@link Component} object to add.
     * @return The added {@link Component} object.
     */
    public Component addAndReturn(Component component) {
        addComponent(component);
        return component;
    }

    //-------------------------------------------------
    // Remove component
    //-------------------------------------------------

    /**
     * Remove the given {@link Component} object from this {@link Entity}.
     *
     * @param component The {@link Component} object to remove.
     */
    public void removeComponent(Component component) {
        // update signature bits
        signature.clear(entityManager.getEcs().getComponentIndex(component.getClass()));

        // remove from list
        components.remove(component);

        // remove from cache
        componentsCache.remove(component.getClass());
    }

    //-------------------------------------------------
    // Has component
    //-------------------------------------------------

    /**
     * Tells if this {@link Entity} has a {@link Component} of the specified type.
     *
     * @param componentClass The class of the component.
     * @return <i>true</i> if this {@link Entity} has this component added to it.
     */
    public boolean hasComponent(Class<? extends Component> componentClass) {
        return componentsCache.containsKey(componentClass);
    }
}
