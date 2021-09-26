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

package de.sg.benno;

public class BennoConfig {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    public static String ROOT_PATH;
    public static String SAVEGAME_PATH;

    public static int GFX_CAMERA_SPEED;
    public static int MGFX_CAMERA_SPEED;
    public static int SGFX_CAMERA_SPEED;

    public static int DEEP_WATER_BUILDING_ID;

    public static String PNG_OUT_PATH;

    public static boolean CREATE_STADTFLD_GFX_PNG;
    public static boolean CREATE_STADTFLD_MGFX_PNG;
    public static boolean CREATE_STADTFLD_SGFX_PNG;

    public static boolean CREATE_SHIP_GFX_PNG;
    public static boolean CREATE_SHIP_MGFX_PNG;
    public static boolean CREATE_SHIP_SGFX_PNG;

    public static boolean SHOW_ISLAND5_AABBS;
    public static boolean AABB_COLLISION_DETECTION;

    public static boolean SHOW_GRID;

    public static boolean CREATE_ATLAS_IMAGES;
    public static String ATLAS_OUT_PATH;
    public static String ATLAS_SGFX_PATH;
    public static String ATLAS_MGFX_PATH;
    public static String ATLAS_GFX_PATH;

    public static String GAME_STATE_NAME;

    public static int CAMERA_START_X;
    public static int CAMERA_START_Y;

    public static int ZOOM_START;

    public static String WATER_RENDERER_SHADER_FOLDER;

    public static String SAVEGAME;
}
