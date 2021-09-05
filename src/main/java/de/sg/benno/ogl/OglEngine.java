/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl;

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

        imGuiGlfw.init(window.getWindowHandle(), true);
        imGuiGl3.init("#version 130");

        application.init();
    }

    /**
     * Get the input.
     */
    private void input() {
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
        application.cleanUp();
    }
}
