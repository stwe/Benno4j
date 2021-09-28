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

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import org.joml.Vector2f;
import org.joml.Vector2i;

public class PositionComponent implements Component, Pool.Poolable {

    /**
     * The position in world space.
     */
    public Vector2i worldPosition = new Vector2i();

    /**
     * The position in screen space.
     */
    public Vector2f screenPosition = new Vector2f();

    /**
     * The tile size.
     */
    public Vector2f size = new Vector2f();

    @Override
    public void reset() {
        worldPosition = new Vector2i();
        screenPosition = new Vector2f();
        size = new Vector2f();
    }
}
