/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
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
