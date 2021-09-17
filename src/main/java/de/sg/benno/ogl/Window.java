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

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryStack;

import java.util.Objects;

import imgui.*;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Encapsulate all the window initialization code.
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
     * Initializing class.
     */
    public void init() {
        initGlfw();
        initImGui();
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
     * Initializing ImGui.
     */
    private void initImGui() {
        LOGGER.debug("Initializing ImGui.");

        ImGui.createContext();

        final var io = ImGui.getIO();

        io.setIniFilename(null);                                // We don't want to save .ini file
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);  // Enable Keyboard Controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);      // Enable Docking
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);    // Enable Multi-Viewport / Platform Windows
        io.setConfigViewportsNoTaskBarIcon(true);

        if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final var style = ImGui.getStyle();
            style.setWindowRounding(0.0f);
            style.setColor(ImGuiCol.WindowBg, ImGui.getColorU32(ImGuiCol.WindowBg, 1));
        }
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

    /**
     * Encapsulates glfwWindowShouldClose.
     *
     * @return boolean
     */
    public boolean windowShouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    /**
     * Swaps the front and back buffers and checks if any events are triggered.
     */
    public void update() {
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    /**
     * Update projection matrix.
     */
    public void updateProjectionMatrix() {
        projectionMatrix.setPerspective(Config.FOV, (float) width / height, Config.NEAR, Config.FAR);
    }

    /**
     * Update orthographic projection matrix.
     */
    public void updateOrthographicProjectionMatrix() {
        /*
        Transforms all x coordinates between 0 and width to -1 and 1,
        and all y coordinates between 0 and height to -1 and 1.
        We specified that the top of the frustum has a y coordinate of 0,
        while the bottom has a y coordinate of height. The result is that
        the top-left coordinate of the scene will be at (0,0) and the
        bottom-right part of the screen is at coordinate (width, height),
        just like screen coordinates; the world-space coordinates directly
        correspond to the resulting pixel coordinates.


        World/Screen space coordinates

        ---------------
        | 0, 0        |
        |             |
        |             |
        |        w, h |
        ---------------

        To normalized device coordinates

        ---------------
        | -1, -1      |
        |             |
        |             |
        |        1, 1 |
        ---------------
        */

        orthographicProjectionMatrix.setOrtho(0.0f, width, height, 0.0f, -1.0f, 1.0f);
    }

    //-------------------------------------------------
    // Input
    //-------------------------------------------------

    /**
     * Poll the key status.
     *
     * @param keyCode A constant such as GLFW_KEY_W or GLFW_KEY_SPACE
     *
     * @return true if GLFW_PRESS
     */
    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
    }

    /**
     * Close if Esc key was pressed.
     */
    public void closeIfEscKeyPressed() {
        if (isKeyPressed(GLFW_KEY_ESCAPE)) {
            glfwSetWindowShouldClose(windowHandle, true);
        }
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Clean up Window.");

        // Clean up ImGui.
        ImGui.destroyContext();

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
