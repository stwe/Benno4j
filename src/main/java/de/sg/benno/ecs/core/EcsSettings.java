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
import java.util.Arrays;

/**
 * Represents the Settings for the {@link Ecs}.
 */
public class EcsSettings {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * A list of all {@link Component} types used in the {@link Ecs}.
     */
    private static final ArrayList<Class<? extends Component>> ALL_COMPONENT_TYPES = new ArrayList<>();

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #ALL_COMPONENT_TYPES}.
     *
     * @return {@link #ALL_COMPONENT_TYPES}
     */
    public static ArrayList<Class<? extends Component>> getAllComponentTypes() {
        return ALL_COMPONENT_TYPES;
    }

    /**
     * Returns the index in {@link #ALL_COMPONENT_TYPES} of the given {@link Component} type.
     *
     * @param componentType The component type.
     *
     * @return The index.
     */
    public static int getComponentIndex(Class<? extends Component> componentType) {
        return ALL_COMPONENT_TYPES.indexOf(componentType);
    }

    //-------------------------------------------------
    // Setter
    //-------------------------------------------------

    /**
     * Set {@link #ALL_COMPONENT_TYPES}.
     *
     * @param allComponentTypes The component types.
     */
    @SafeVarargs
    public static void setAllComponentTypes(Class<? extends Component>... allComponentTypes) {
        EcsSettings.ALL_COMPONENT_TYPES.clear();
        EcsSettings.ALL_COMPONENT_TYPES.addAll(Arrays.asList(allComponentTypes));
    }
}
