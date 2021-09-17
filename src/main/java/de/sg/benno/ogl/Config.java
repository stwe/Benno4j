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

package de.sg.benno.ogl;

/**
 * Screen/Window config.
 */
public class Config {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    public static String TITLE;
    public static int WIDTH;
    public static int HEIGHT;
    public static boolean V_SYNC;

    public static float FOV;
    public static float NEAR;
    public static float FAR;

    public static double FPS;

    public static String AABB_DEBUG_TEXTURE_PATH;

    public static String SHADER_PROGRAMS_PATH;
    public static String SPRITE_RENDERER_SHADER_FOLDER;

    public static String VERTEX_SHADER_FILE_NAME;
    public static String TESSELLATION_CONTROL_SHADER_FILE_NAME;
    public static String TESSELLATION_EVALUATION_SHADER_FILE_NAME;
    public static String GEOMETRY_SHADER_FILE_NAME;
    public static String FRAGMENT_SHADER_FILE_NAME;
}
