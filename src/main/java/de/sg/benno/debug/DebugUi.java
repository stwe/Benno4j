/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.debug;

import de.sg.benno.World;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.GameState;
import de.sg.ogl.Config;
import de.sg.ogl.input.MouseInput;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.internal.ImGui;
import imgui.type.ImBoolean;

import static de.sg.benno.MiniMap.MINIMAP_HEIGHT;
import static de.sg.benno.MiniMap.MINIMAP_WIDTH;

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
    private static final int WIDTH = 260;

    /**
     * The menu height.
     */
    private static final int HEIGHT = 700;

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The parent {@link GameState}.
     */
    private final GameState gameState;

    /**
     * The texture id of the minimap.
     */
    private int miniMapId = 0;

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
        ImGui.text("Camera screen space x: " + gameState.getWorld().getCamera().position.x + " (" + gameState.getWorld().getCamera().positionInTileUnits.x+")");
        ImGui.text("Camera screen space y: " + gameState.getWorld().getCamera().position.y + " (" + gameState.getWorld().getCamera().positionInTileUnits.y+")");

        cameraSlider();

        ImGui.separator();
        ImGui.text("Zoom");
        if (ImGui.beginCombo("", gameState.getWorld().getCurrentZoom().toString(), 0)) {
            for (var zoom : Zoom.values()) {
                ImBoolean isSelect = new ImBoolean();
                if (ImGui.selectable(zoom.toString(), isSelect, 0)) {
                    gameState.getWorld().setCurrentZoom(zoom);
                    gameState.getWorld().getCamera().resetPosition(zoom);
                }
            }

            ImGui.endCombo();
        }

        ImGui.separator();
        ImGui.text("Mouse position in Benno4j");
        ImGui.text("Mouse x: " + MouseInput.getX());
        ImGui.text("Mouse y: " + MouseInput.getY());

        var selTile = gameState.getWorld().getMousePicker().getTileUnderMouse(
                gameState.getWorld().getCamera(),
                gameState.getWorld().getCurrentZoom()
        );
        ImGui.separator();
        ImGui.text("Tile under mouse");
        ImGui.text("Tile x: " + selTile.x);
        ImGui.text("Tile y: " + selTile.y);

        //ImGui.separator();
        //showMiniMap();

        ImGui.separator();
        showImGuiInfo();

        ImGui.end();
    }

    /**
     * Add camera slider for faster movement.
     */
    private void cameraSlider() {
        ImGui.separator();
        ImGui.text("Camera fast screen space change");

        int[] camX = new int[1];
        camX[0] = gameState.getWorld().getCamera().positionInTileUnits.x;
        if (ImGui.sliderInt("x", camX, -World.WORLD_WIDTH, World.WORLD_WIDTH)) {
            camX[0] += camX[0] % gameState.getWorld().getCurrentZoom().speedFactor;
            gameState.getWorld().getCamera().positionInTileUnits.x = camX[0];
            gameState.getWorld().getCamera().resetPosition(gameState.getWorld().getCurrentZoom());
        }

        int[] camY = new int[1];
        camY[0] = gameState.getWorld().getCamera().positionInTileUnits.y;
        if (ImGui.sliderInt("y", camY, -World.WORLD_HEIGHT, World.WORLD_HEIGHT)) {
            camY[0] += camY[0] % gameState.getWorld().getCurrentZoom().speedFactor;
            gameState.getWorld().getCamera().positionInTileUnits.y = camY[0];
            gameState.getWorld().getCamera().resetPosition(gameState.getWorld().getCurrentZoom());
        }
    }

    /**
     * Shows the minimap.
     */
    private void showMiniMap() {
        if (miniMapId == 0) {
            miniMapId = gameState.world.getMiniMap().getMiniMapTexture().getId();
        }

        ImGui.text("For debug only");
        ImGui.text("Next image pos x: " + ImGui.getCursorScreenPosX());
        ImGui.text("Next image pos y: " + ImGui.getCursorScreenPosY());

        ImGui.image(miniMapId,
                MINIMAP_WIDTH * 0.5f, MINIMAP_HEIGHT * 0.5f,
                0, 0, 1, 1
        );
    }

    /**
     * Info
     */
    private void showImGuiInfo() {
        ImGui.text("Mouse position in OS");
        ImGui.text("OS Mouse x: " + ImGui.getMousePosX());
        ImGui.text("OS Mouse y: " + ImGui.getMousePosY());

        ImGui.separator();
        var io = ImGui.getIO();
        ImGui.text("ImGui main viewport");
        ImGui.text("main x: " + ImGui.getMainViewport().getPosX());
        ImGui.text("main y: " + ImGui.getMainViewport().getPosY());

        ImGui.separator();
        ImGui.text("Benno4j display size");
        ImGui.text("width: " + io.getDisplaySizeX());
        ImGui.text("height: " + io.getDisplaySizeY());
    }
}
