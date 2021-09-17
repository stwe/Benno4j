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

package de.sg.benno.input;

import de.sg.benno.ogl.OglEngine;
import de.sg.benno.ogl.camera.OrthographicCamera;
import de.sg.benno.renderer.Zoom;

/**
 * Represents a camera.
 */
public class Camera extends OrthographicCamera {

    /**
     * Constructs a new {@link OrthographicCamera} object.
     *
     * @param x The start x position in tile units.
     * @param y The start y position in tile units.
     * @param engine The parent {@link OglEngine} object.
     * @param zoom The curreent {@link Zoom}
     * @throws Exception If an error is thrown.
     */
    public Camera(int x, int y, OglEngine engine, Zoom zoom) throws Exception {
        super(x, y, engine, zoom);
    }
}
