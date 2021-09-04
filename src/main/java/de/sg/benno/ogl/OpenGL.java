/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL32.GL_FIRST_VERTEX_CONVENTION;
import static org.lwjgl.opengl.GL32.glProvokingVertex;

public class OpenGL {

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    private OpenGL() {}

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    public static void init() {
        LOGGER.debug("Initializing OpenGL.");

        GL.createCapabilities();
        printContextInitInfo();

        // enabled by default on some drivers, but not all so always enable to make sure
        glEnable(GL_MULTISAMPLE);
    }

    //-------------------------------------------------
    // OpenGL states
    //-------------------------------------------------

    public static void setClearColor(float r, float g, float b, float a) {
        glClearColor(r, g, b, a);
    }

    public static void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    public static void enableDepthAndStencilTesting() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        glProvokingVertex(GL_FIRST_VERTEX_CONVENTION);
    }

    public static void enableAlphaBlending() {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void disableBlending() {
        glDisable(GL_BLEND);
    }

    public static void enableDepthTesting() {
        glEnable(GL_DEPTH_TEST);
    }

    public static void disableDepthTesting() {
        glDisable(GL_DEPTH_TEST);
    }

    public static void enableFaceCulling() {
        // On a freshly created OpenGL Context, the default front face is GL_CCW.
        // All the faces that are not front-faces are discarded.
        glFrontFace(GL_CCW);
        glCullFace(GL_BACK);
        glEnable(GL_CULL_FACE);
    }

    public static void disableFaceCulling() {
        glDisable(GL_CULL_FACE);
    }

    public static void enableWireframeMode() {
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    }

    public static void disableWireframeMode() {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    //-------------------------------------------------
    // OpenGL helper
    //-------------------------------------------------

    public static FloatBuffer getPixel(int x, int y) {
        var pixel = BufferUtils.createFloatBuffer(4);
        glReadPixels(x, y, 1, 1, GL_RGBA, GL_FLOAT, pixel);

        return pixel;
    }

    //-------------------------------------------------
    // Info
    //-------------------------------------------------

    private static void printContextInitInfo() {
        LOGGER.info("OpenGL version: {}", GL11.glGetString(GL11.GL_VERSION));
        LOGGER.info("GLSL version: {}", GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
        LOGGER.info("Vendor: {}", GL11.glGetString(GL11.GL_VENDOR));
        LOGGER.info("Renderer: {}", GL11.glGetString(GL11.GL_RENDERER));
    }
}
