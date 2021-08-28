/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.debug;

import de.sg.benno.chunk.Ship4;
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import de.sg.benno.state.GameState;
import de.sg.ogl.Config;
import de.sg.ogl.input.MouseInput;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.internal.ImGui;
import imgui.type.ImBoolean;

import java.io.IOException;

import static de.sg.benno.World.WORLD_HEIGHT;
import static de.sg.benno.World.WORLD_WIDTH;

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
    private static final int WIDTH = 270;

    /**
     * The menu height.
     */
    private static final int HEIGHT = 550;

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
    public void render() throws IOException {
        ImGui.setNextWindowSize(WIDTH, HEIGHT, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + Config.WIDTH - WIDTH, ImGui.getMainViewport().getPosY(), ImGuiCond.Once);

        final int windowFlags =
                ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus |
                ImGuiWindowFlags.NoNavFocus;
                //ImGuiWindowFlags.NoBackground;

        ImGui.begin("Debug", windowFlags);

        cameraPosition();
        zoom();
        mousePosition();
        tileUnderMouse();
        currentShip();

        ImGui.end();
    }

    //-------------------------------------------------
    // Widgets
    //-------------------------------------------------

    private void cameraPosition() {
        ImGui.text("Camera position");
        ImGui.text("Camera screen space x: " + gameState.getWorld().getCamera().position.x + " (" + gameState.getWorld().getCamera().positionInTileUnits.x+")");
        ImGui.text("Camera screen space y: " + gameState.getWorld().getCamera().position.y + " (" + gameState.getWorld().getCamera().positionInTileUnits.y+")");
    }

    private void cameraSlider() {
        ImGui.separator();
        ImGui.text("Camera fast screen space change");

        int[] camX = new int[1];
        camX[0] = gameState.getWorld().getCamera().positionInTileUnits.x;
        if (ImGui.sliderInt("x", camX, -WORLD_WIDTH, WORLD_WIDTH)) {
            camX[0] += camX[0] % gameState.getWorld().getCurrentZoom().speedFactor;
            gameState.getWorld().getCamera().positionInTileUnits.x = camX[0];
            gameState.getWorld().getCamera().resetPosition(gameState.getWorld().getCurrentZoom());
        }

        int[] camY = new int[1];
        camY[0] = gameState.getWorld().getCamera().positionInTileUnits.y;
        if (ImGui.sliderInt("y", camY, -WORLD_HEIGHT, WORLD_HEIGHT)) {
            camY[0] += camY[0] % gameState.getWorld().getCurrentZoom().speedFactor;
            gameState.getWorld().getCamera().positionInTileUnits.y = camY[0];
            gameState.getWorld().getCamera().resetPosition(gameState.getWorld().getCurrentZoom());
        }
    }

    private void zoom() {
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
    }

    private void mousePosition() {
        ImGui.separator();
        ImGui.text("Mouse position in BennoApp");
        ImGui.text("Mouse x: " + MouseInput.getX());
        ImGui.text("Mouse y: " + MouseInput.getY());
    }

    private void tileUnderMouse() throws IOException {
        var selTile = gameState.getWorld().getMousePicker().getTileUnderMouse(
                gameState.getWorld().getCamera(),
                gameState.getWorld().getCurrentZoom()
        );
        ImGui.separator();
        ImGui.text("Tile under mouse");
        ImGui.text("Tile x: " + selTile.x);
        ImGui.text("Tile y: " + selTile.y);

        ImGui.separator();
        ImGui.text("Tile select mode");
        if (ImGui.beginCombo("mode", gameState.getWorld().getMousePicker().getSearchMode().toString(), 0)) {
            for (var tileHeight : TileGraphic.TileHeight.values()) {
                ImBoolean isSelect = new ImBoolean();
                if (ImGui.selectable(tileHeight.toString(), isSelect, 0)) {
                    gameState.getWorld().getMousePicker().setSearchMode(tileHeight);
                }
            }

            ImGui.endCombo();
        }

        /*
        if (gameState.getWorld().getMousePicker().getCurrentTileGraphic() != null) {
            var tileGraphic = gameState.getWorld().getMousePicker().getCurrentTileGraphic();
            ImGui.separator();
            ImGui.text("Graphic Id: " + tileGraphic.parentTile.graphicId);
            ImGui.text("Start gfx on gpu: " + tileGraphic.gfx);

            var context = (Context)gameState.getStateMachine().getStateContext();
            var bshFiles = context.bennoFiles.getStadtfldBshFile(Zoom.GFX);
            var bshTexture = bshFiles.getBshTextures().get(tileGraphic.gfx);
            var glTexture = bshTexture.getTexture();

            ImGui.text("GFX Texture");

            ImGui.image(glTexture.getId(),
                    glTexture.getWidth(), glTexture.getHeight(),
                    0, 0, 1, 1
            );
        } else {
            ImGui.text("Graphic Id: none");
            ImGui.text("Start gfx on gpu: none");
        }
        */
    }

    private void currentShip() {
        ImGui.separator();
        ImGui.text("Current ship");
        var currentShip = gameState.getWorld().getShipping().getCurrentShip();
        if (currentShip != null) {
            ImGui.text("Name: " + currentShip.name);
            ImGui.text("X: " + currentShip.xPos);
            ImGui.text("Y: " + currentShip.yPos);
            ImGui.text("Health: " + currentShip.health);
            ImGui.text("Cannons: " + currentShip.numberOfCannons);
            ImGui.text("Type: " + Ship4.ShipType.valueOfType(currentShip.type));
            ImGui.text("Direction: " + currentShip.direction);
        } else {
            ImGui.text("Ship: none");
        }

        var target = gameState.getWorld().getShipping().getTarget();
        if (target != null) {
            ImGui.text("Target x: " + target.x);
            ImGui.text("Target y: " + target.y);
        } else {
            ImGui.text("Target: none");
        }
    }

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
