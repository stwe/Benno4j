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

import de.sg.benno.ogl.input.MouseInput;
import de.sg.benno.ogl.resource.ResourceManager;
import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;

/**
 * Contains the game loop code.
 */
public class OglEngine implements Runnable {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * Nanoseconds per frame.
     */
    private final double NANO_PER_FRAME = 1000000000.0 / Config.FPS;

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The parent {@link OglApplication}.
     */
    private final OglApplication application;

    /**
     * A {@link Window} object.
     */
    private final Window window;

    /**
     * A {@link MouseInput} object for mouse access.
     */
    private final MouseInput mouseInput;

    /**
     * The {@link ResourceManager} object.
     */
    private final ResourceManager resourceManager;

    /**
     * An {@link ImGuiImplGlfw} object.
     */
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();

    /**
     * An {@link ImGuiImplGl3} object.
     */
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link OglEngine} object.
     *
     * @param application The parent {@link OglApplication}.
     */
    public OglEngine(OglApplication application) {
        LOGGER.debug("Creates OglEngine object.");

        this.application = Objects.requireNonNull(application, "application must not be null");
        this.application.setEngine(this);

        this.window = new Window();
        this.mouseInput = new MouseInput();
        this.resourceManager = new ResourceManager();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link Window}.
     *
     * @return {@link Window}
     */
    public Window getWindow() {
        return window;
    }

    /**
     * Get {@link MouseInput}.
     *
     * @return {@link MouseInput}
     */
    public MouseInput getMouseInput() {
        return mouseInput;
    }

    /**
     * Get {@link ResourceManager}.
     *
     * @return {@link ResourceManager}
     */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    //-------------------------------------------------
    // Implement Runnable
    //-------------------------------------------------

    @Override
    public void run() {
        LOGGER.debug("Running OglEngine.");

        try {
            init();
            gameLoop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanUp();
        }

        LOGGER.debug("Goodbye ...");
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Initializing engine.
     *
     * @throws Exception If an error is thrown.
     */
    private void init() throws Exception {
        LOGGER.debug("Initializing OglEngine.");

        window.init();
        mouseInput.init(window);

        imGuiGlfw.init(window.getWindowHandle(), true);
        imGuiGl3.init("#version 130");

        application.init();
    }

    /**
     * Get the input.
     */
    private void input() {
        mouseInput.input();
        application.input();
    }

    /**
     * Update the game state.
     */
    private void update() {
        application.update();
    }

    /**
     * Render game data.
     *
     * @throws Exception If an error is thrown.
     */
    private void render() throws Exception {
        startFrame();
        frame();
        endFrame();
    }

    /**
     * Clear the screen.
     */
    private void startFrame() {
        OpenGL.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        OpenGL.clear();
    }

    /**
     * Render application and ImGui stuff.
     *
     * @throws Exception If an error is thrown.
     */
    private void frame() throws Exception {
        application.render();

        imGuiGlfw.newFrame();
        ImGui.newFrame();

        application.renderImGui();
        ImGui.render();
    }

    /**
     * Swap buffers.
     */
    private void endFrame() {
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupWindowPtr = glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            glfwMakeContextCurrent(backupWindowPtr);
        }

        window.update();
    }

    //-------------------------------------------------
    // Game loop
    //-------------------------------------------------

    /**
     * The game loop.
     *
     * @throws Exception If an error is thrown.
     */
    private void gameLoop() throws Exception {
        LOGGER.debug("Starting the game loop.");

        var lastTime = System.nanoTime();
        var resetTimer = System.nanoTime();

        var dt = 0.0;
        var fps = 0;
        var updates = 0;

        while(!window.windowShouldClose()) {
            var now = System.nanoTime();
            dt += (now - lastTime) / NANO_PER_FRAME;
            lastTime = now;

            input();

            while (dt >= 1.0) {
                update();
                updates++;
                dt--;
            }

            render();
            fps++;

            if (System.nanoTime() - resetTimer > 1000000000) {
                resetTimer += 1000000000;
                window.setTitle(window.getTitle() + "  |  " + fps + " frames  |  " + updates + " updates");
                updates = 0;
                fps = 0;
            }
        }
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    private void cleanUp() {
        LOGGER.debug("Clean up OglEngine.");

        imGuiGl3.dispose();
        imGuiGlfw.dispose();

        window.cleanUp();
        resourceManager.cleanUp();

        application.cleanUp();
    }
}
