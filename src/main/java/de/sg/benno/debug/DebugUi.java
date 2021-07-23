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
import imgui.internal.ImGui;

/**
 * An ImGui for debug output.
 */
public class DebugUi {

    /**
     * The parent {@link GameState}.
     */
    private final GameState gameState;

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
        ImGui.begin("Debug");

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

        ImGui.separator();
        ImGui.text("Calc selected tile");
        ImGui.text("Screen space cell x: " + gameState.cell.x);
        ImGui.text("Screen space cell y: " + gameState.cell.y);
        ImGui.text("Cell offset x: " + gameState.offset.x);
        ImGui.text("Cell offset y: " + gameState.offset.y);
        ImGui.text("Selected tile x: " + gameState.selected.x);
        ImGui.text("Selected tile y: " + gameState.selected.y);

        ImGui.separator();
        ImGui.text("Other");
        ImGui.text("Info: " + gameState.debugText);

        ImGui.end();
    }
}
