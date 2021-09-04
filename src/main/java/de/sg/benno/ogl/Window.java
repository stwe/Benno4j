/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryStack;

import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Encapsulate all the GLFW Window initialization code.
 */
public class Window {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * The default min width of the window.
     */
    private static final int MIN_WIDTH = 640;

    /**
     * The default min height of the window.
     */
    private static final int MIN_HEIGHT = 480;

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The title of the window.
     */
    private final String title;

    /**
     * The width of the window.
     */
    private int width;

    /**
     * The height of the window.
     */
    private int height;

    /**
     * Use v-sync or not.
     */
    private final boolean vSync;

    /**
     * The projection matrix.
     */
    private final Matrix4f projectionMatrix;

    /**
     * The orthographic projection matrix.
     */
    private final Matrix4f orthographicProjectionMatrix;

    /**
     * The glfw window handle.
     */
    private long windowHandle;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Window} object.
     */
    public Window() {
        LOGGER.debug("Creates Window object.");

        this.title = Objects.requireNonNull(Config.TITLE, "title must not be null");
        this.width = Config.WIDTH;
        this.height = Config.HEIGHT;
        this.vSync = Config.V_SYNC;

        if (width < MIN_WIDTH || height < MIN_HEIGHT) {
            this.width = MIN_WIDTH;
            this.height = MIN_HEIGHT;
            LOGGER.warn("Invalid width or height value given. The default values are now used.");
        }

        projectionMatrix = new Matrix4f();
        orthographicProjectionMatrix = new Matrix4f();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #title}.
     *
     * @return {@link #title}
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get {@link #width}.
     *
     * @return {@link #width}
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get {@link #height}.
     *
     * @return {@link #height}
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get {@link #projectionMatrix}.
     *
     * @return {@link #projectionMatrix}
     */
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    /**
     * Get {@link #orthographicProjectionMatrix}.
     *
     * @return {@link #orthographicProjectionMatrix}
     */
    public Matrix4f getOrthographicProjectionMatrix() {
        return orthographicProjectionMatrix;
    }

    /**
     * Get {@link #windowHandle}.
     *
     * @return {@link #windowHandle}
     */
    public long getWindowHandle() {
        return windowHandle;
    }

    //-------------------------------------------------
    // Setter
    //-------------------------------------------------

    /**
     * Set {{@link #title}}
     *
     * @param title {@link String}
     */
    public void setTitle(String title) {
        glfwSetWindowTitle(windowHandle, title);
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Init class.
     */
    public void init() {
        initGlfw();
        initProjectionMatrix();
    }

    /**
     * The GLFW Window initialization code.
     */
    private void initGlfw() {
        LOGGER.debug("Initializing Window.");

        // Setup an error callback.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW.
        LOGGER.debug("Configuring GLFW.");
        if (!glfwInit()) {
            throw new OglRuntimeException("Unable to initialize GLFW.");
        }

        // Configure GLFW.
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        // The size of the window can not be changed during runtime.
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 4);

        // Create the window.
        LOGGER.debug("Initializing a {}x{} window.", width, height);
        windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (windowHandle == NULL) {
            throw new OglRuntimeException("Failed to create the GLFW window.");
        }

        // Setup resize callback.
        glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> {
            this.width = width;
            this.height = height;
        });

        // Get the thread stack and push a new frame.
        try (MemoryStack stack = stackPush()) {
            var pWidth = stack.mallocInt(1);
            var pHeight = stack.mallocInt(1);

            // Get the window size passed to glfwCreateWindow.
            glfwGetWindowSize(windowHandle, pWidth, pHeight);

            // Get the resolution of the primary monitor.
            var vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (vidmode == null) {
                throw new OglRuntimeException("Failed to get the current video mode.");
            }

            // Center the window.
            glfwSetWindowPos(
                    windowHandle,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        // Determines what window is the opengl context.
        glfwMakeContextCurrent(windowHandle);

        if (vSync) {
            // Enable v-sync.
            glfwSwapInterval(GLFW_TRUE);
            LOGGER.info("V-Sync is enabled.");
        }

        // Make the window visible.
        glfwShowWindow(windowHandle);

        // Makes the OpenGL bindings available for use.
        OpenGL.init();
    }

    /**
     * Init projection matrices.
     */
    private void initProjectionMatrix() {
        updateProjectionMatrix();
        updateOrthographicProjectionMatrix();
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    public boolean windowShouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    public void update() {
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    public void updateProjectionMatrix() {
        projectionMatrix.setPerspective(Config.FOV, (float) width / height, Config.NEAR, Config.FAR);
    }

    public void updateOrthographicProjectionMatrix() {
        /*
        ---------------
        | 0, 0        |
        |             |
        |             |
        |        w, h |
        ---------------
        */

        orthographicProjectionMatrix.setOrtho(0.0f, width, height, 0.0f, 1.0f, -1.0f);
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Clean up Window.");

        // Frees callbacks associated with the window.
        glfwFreeCallbacks(windowHandle);

        // Destroy the window.
        glfwDestroyWindow(windowHandle);

        // Terminate GLFW.
        glfwTerminate();

        // Non-window callbacks must be reset and freed separately.
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }
}
