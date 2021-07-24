/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.debug;

import de.sg.benno.state.GameState;
import de.sg.ogl.input.MouseInput;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.internal.ImGui;

/**
 * An ImGui for debug output.
 */
public class DebugUi {

    /**
     * The parent {@link GameState}.
     */
    private final GameState gameState;

    private static int corner = 0;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link DebugUi} object.
     *
     * @param gameState The parent {@link GameState}.
     */
    public DebugUi(GameState gameState) {
        this.gameState = gameState;
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Render debug menu.
     */
    public void render() {
        ImGui.setNextWindowSize(250, 300, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 1024 - 250, ImGui.getMainViewport().getPosY(), ImGuiCond.Once);

        final int windowFlags = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus | ImGuiWindowFlags.NoBackground;

        ImGui.begin("Debug", windowFlags);

        ImGui.text("Camera position");
        ImGui.text("Camera screen space x: " + gameState.camera.position.x);
        ImGui.text("Camera screen space y: " + gameState.camera.position.y);
        ImGui.text("Camera origin x: " + gameState.camera.origin.x);
        ImGui.text("Camera origin y: " + gameState.camera.origin.y);

        ImGui.separator();
        ImGui.text("Zoom");
        ImGui.text("Current zoom: " + gameState.currentZoom);

        ImGui.separator();
        ImGui.text("Mouse position");
        ImGui.text("Mouse x: " + MouseInput.getX());
        ImGui.text("Mouse y: " + MouseInput.getY());

        ImGui.end();
    }
}
