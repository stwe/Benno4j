/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.debug;

import de.sg.benno.World;
import de.sg.benno.input.MousePicker;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.GameState;
import de.sg.ogl.Config;
import de.sg.ogl.input.MouseInput;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.internal.ImGui;
import imgui.type.ImBoolean;

/**
 * An ImGui for debug output.
 */
public class DebugUi {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * The menu width.
     */
    private static final int WIDTH = 320;

    /**
     * The menu height.
     */
    private static final int HEIGHT = 300;

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

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
        ImGui.setNextWindowSize(WIDTH, HEIGHT, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + Config.WIDTH - WIDTH, ImGui.getMainViewport().getPosY(), ImGuiCond.Once);

        final int windowFlags = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus | ImGuiWindowFlags.NoBackground;

        ImGui.begin("Debug", windowFlags);

        ImGui.text("Camera position");
        ImGui.text("Camera screen space x: " + gameState.camera.position.x + " (" + gameState.camera.positionInTileUnits.x+")");
        ImGui.text("Camera screen space y: " + gameState.camera.position.y + " (" + gameState.camera.positionInTileUnits.y+")");

        cameraSlider();

        ImGui.separator();
        ImGui.text("Zoom");
        ImGui.text("Current zoom: " + gameState.currentZoom);

        if (ImGui.beginCombo("Change zoom", gameState.currentZoom.toString(), 0)) {
            for (var zoom : Zoom.values()) {
                ImBoolean isSelect = new ImBoolean();
                if (ImGui.selectable(zoom.toString(), isSelect, 0)) {
                    gameState.currentZoom = zoom;
                    gameState.camera.resetPosition(zoom);
                }
            }

            ImGui.endCombo();
        }

        ImGui.separator();
        ImGui.text("Mouse position");
        ImGui.text("Mouse x: " + MouseInput.getX());
        ImGui.text("Mouse y: " + MouseInput.getY());

        var selTile = MousePicker.getSelectedTile(gameState.camera, gameState.currentZoom);
        ImGui.separator();
        ImGui.text("Tile under mouse");
        ImGui.text("Tile x: " + selTile.x);
        ImGui.text("Tile y: " + selTile.y);

        ImGui.end();
    }

    /**
     * Add camera slider for faster movement.
     */
    private void cameraSlider() {
        ImGui.separator();
        ImGui.text("Camera fast screen space change");

        int[] camX = new int[1];
        camX[0] = gameState.camera.positionInTileUnits.x;
        if (ImGui.sliderInt("x", camX, -World.WORLD_WIDTH, World.WORLD_WIDTH)) {
            camX[0] += camX[0] % gameState.currentZoom.speedFactor;
            gameState.camera.positionInTileUnits.x = camX[0];
            gameState.camera.resetPosition(gameState.currentZoom);
        }

        int[] camY = new int[1];
        camY[0] = gameState.camera.positionInTileUnits.y;
        if (ImGui.sliderInt("y", camY, -World.WORLD_HEIGHT, World.WORLD_HEIGHT)) {
            camY[0] += camY[0] % gameState.currentZoom.speedFactor;
            gameState.camera.positionInTileUnits.y = camY[0];
            gameState.camera.resetPosition(gameState.currentZoom);
        }
    }
}
