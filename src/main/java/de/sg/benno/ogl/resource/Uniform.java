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

package de.sg.benno.ogl.resource;

/**
 * Represents a Uniform to pass data from the CPU to the shaders on the GPU.
 */
public class Uniform {

    /**
     * If it a set of Uniform variables.
     */
    public boolean isUniformBlock = false;

    /**
     * The data type.
     */
    public String type;

    /**
     * The name of the Uniform.
     */
    public String name;
}
