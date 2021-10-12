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

package de.sg.benno.ecs.components;

import de.sg.benno.ecs.core.Component;
import de.sg.benno.renderer.Zoom;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.HashMap;

/**
 * Represents a PositionComponent.
 */
public class PositionComponent implements Component {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The position in world space.
     */
    public Vector2i worldPosition = new Vector2i();

    /**
     * The positions in screen space.
     */
    public HashMap<Zoom, Vector2f> screenPositions = new HashMap<>();

    /**
     * The sizes.
     */
    public HashMap<Zoom, Vector2f> sizes = new HashMap<>();
}
