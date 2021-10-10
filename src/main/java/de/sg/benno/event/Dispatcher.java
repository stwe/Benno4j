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

package de.sg.benno.event;

import de.sg.benno.ecs.core.Entity;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a Dispatcher.
 */
public class Dispatcher {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * A list of {@link Listener} objects.
     */
    private static final ArrayList<Listener> listeners = new ArrayList<>();

    //-------------------------------------------------
    // Add
    //-------------------------------------------------

    /**
     * Adds a {@link Listener} object.
     *
     * @param listener The {@link Listener} object to add.
     */
    public static void addListener(Listener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener must not be null"));
    }

    //-------------------------------------------------
    // Notify
    //-------------------------------------------------

    /**
     * Notify all {@link Listener} objects.
     *
     * @param entity The {@link Entity} object.
     * @param event The {@link Event} object.
     */
    public static void notify(Entity entity, Event event) {
        for (var listener : listeners) {
            listener.handleEvent(entity, event);
        }
    }

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    private Dispatcher() {
    }
}
