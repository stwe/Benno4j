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

import java.util.*;

import static de.sg.benno.ecs.core.EntityTodo.TodoType.UPDATE_SYSTEMS;
import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents an Entity, which is a simple container of {@link Component} objects.
 */
public class Entity {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The parent {@link EntityManager} object.
     */
    private final EntityManager entityManager;

    /**
     * Provides access to {@link Component} objects based on its type.
     */
    private final HashMap<Class<? extends Component>, Component> components = new HashMap<>();

    /**
     * The {@link BitSet} signature for added {@link Component} objects.
     */
    private final BitSet componentsBitSet = new BitSet();

    /**
     * A name for debug output.
     */
    public String debugName;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Entity} object.
     *
     * @param entityManager The parent {@link EntityManager} object.
     */
    public Entity(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager must not be null");
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
        return new ArrayList<>(components.values());
    }

    /**
     * Get {@link #componentsBitSet}.
     *
     * @return {@link #componentsBitSet}
     */
    public BitSet getComponentsBitSet() {
        return componentsBitSet;
    }

    //-------------------------------------------------
    // Get component
    //-------------------------------------------------

    /**
     * Returns the {@link Component} object of the specified class if this {@link Entity} has one added,
     * empty {@link Optional} if it doesn't.
     *
     * @param componentClass The specified class.
     *
     * @return The {@link Component} object of the specified class or an empty {@link Optional}.
     */
    public <T extends Component> Optional<T> getComponent(Class<T> componentClass) {
        if (hasComponent(componentClass)) {
            return Optional.of(componentClass.cast(components.get(componentClass)));
        }

        return Optional.empty();
    }

    /**
     * Tells if this {@link Entity} has a {@link Component} of the specified class.
     *
     * @param componentClass The specified class.
     *
     * @return <i>true</i> if this {@link Entity} has this component added to it.
     */
    public boolean hasComponent(Class<? extends Component> componentClass) {
        return components.containsKey(componentClass);
    }

    //-------------------------------------------------
    // Add component
    //-------------------------------------------------

    /**
     * Adds the {@link Component} object of the specified class to this {@link Entity}.
     *
     * @param componentClass The specified class.
     *
     * @return The {@link Component} object of the specified class or an empty {@link Optional}.
     * @throws Exception If an error is thrown.
     */
    public <T extends Component> Optional<T> addComponent(Class<T> componentClass) throws Exception {
        // each component can only be added once
        if (hasComponent(componentClass)) {
            LOGGER.warn("Component {} already exist.", componentClass.getSimpleName());

            return Optional.empty();
        }

        // new component instance
        var component = newInstance(componentClass);

        // add component to list
        components.put(componentClass, component);

        // update component bits
        componentsBitSet.set(EcsSettings.getComponentIndex(componentClass));

        // add an UPDATE_SYSTEMS entityTodo
        entityManager.addEntityTodo(new EntityTodo(this, UPDATE_SYSTEMS));

        // return newly created component
        return Optional.of(component);
    }

    /**
     * Create and initialize a new instance of the specified class.
     *
     * @param componentClass The specified class.
     *
     * @return A new {@link Component} object created by calling the constructor.
     * @throws Exception If an error is thrown.
     */
    private static <T extends Component> T newInstance(Class<T> componentClass) throws Exception {
        return componentClass.getConstructor().newInstance();
    }

    //-------------------------------------------------
    // Remove component
    //-------------------------------------------------

    /**
     * Removes the {@link Component} of the specified class from this {@link Entity}.
     *
     * @param componentClass The specified class.
     */
    public void removeComponent(Class<? extends Component> componentClass) {
        // use getComponent() instead hasComponent(), because we need the component object for remove
        var componentOptional = getComponent(componentClass);
        if (componentOptional.isPresent()) {
            // remove component from list
            components.remove(componentClass);

            // update component bits
            componentsBitSet.clear(EcsSettings.getComponentIndex(componentClass));

            // add an UPDATE_SYSTEMS entityTodo
            entityManager.addEntityTodo(new EntityTodo(this, UPDATE_SYSTEMS));
        } else {
            LOGGER.warn("The component {} no longer exists and may have already been removed.",
                    componentClass.getSimpleName());
        }
    }
}
