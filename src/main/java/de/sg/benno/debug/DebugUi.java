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

public class DebugUi {

    private final GameState gameState;

    public DebugUi(GameState gameState) {
        this.gameState = gameState;
    }

    public void render() {
        ImGui.begin("Debug");

        ImGui.text("Mouse x: " + MouseInput.getX());
        ImGui.text("Mouse y: " + MouseInput.getY());
        ImGui.separator();
        ImGui.text("Map x: " + gameState.cell.x);
        ImGui.text("Map y: " + gameState.cell.y);
        ImGui.text("Map offset x: " + gameState.offset.x);
        ImGui.text("Map offset y: " + gameState.offset.y);
        ImGui.separator();
        ImGui.text("Info: " + gameState.debugText);

        ImGui.end();
    }
}
