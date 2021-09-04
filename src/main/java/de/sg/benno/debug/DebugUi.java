/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.debug;

import de.sg.benno.chunk.Ship4;
import de.sg.ogl.Config;
import de.sg.ogl.input.MouseInput;
import imgui.ImColor;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.internal.ImGui;

import java.io.IOException;
import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;

/**
 * An ImGui.
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
     * The parent {@link SandboxState}.
     */
    private final SandboxState sandboxState;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link DebugUi} object.
     *
     * @param sandboxState The parent {@link SandboxState}.
     */
    public DebugUi(SandboxState sandboxState) {
        LOGGER.debug("Creates DebugUi object.");

        this.sandboxState = Objects.requireNonNull(sandboxState, "sandboxState must not be null");
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Render widgets.
     *
     * @throws IOException If an I/O error is thrown.
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
        screenSpacePosition();
        worldSpacePosition();
        currentShip();
        targetWorldSpacePosition();
        targetDirection();

        ImGui.end();
    }

    //-------------------------------------------------
    // Widgets
    //-------------------------------------------------

    /**
     * Shows current position.
     */
    private void cameraPosition() {
        ImGui.text("Camera position");
        ImGui.text("Camera screen space x: " + sandboxState.getSandbox().getCamera().position.x + " (" + sandboxState.getSandbox().getCamera().positionInTileUnits.x+")");
        ImGui.text("Camera screen space y: " + sandboxState.getSandbox().getCamera().position.y + " (" + sandboxState.getSandbox().getCamera().positionInTileUnits.y+")");
    }

    /**
     * Shows the current mouse position in screen space.
     */
    private void screenSpacePosition() {
        ImGui.separator();

        ImGui.pushStyleColor(ImGuiCol.Text, ImColor.intToColor(0 ,255, 0));
        ImGui.text("Mouse position in screen space");
        ImGui.popStyleColor();

        ImGui.text("Screen x: " + MouseInput.getX());
        ImGui.text("Screen y: " + MouseInput.getY());
    }

    /**
     * Shows the current mouse position in world space.
     */
    private void worldSpacePosition() {
        var selTile = sandboxState.getSandbox().getMousePicker().getTileUnderMouse(
                sandboxState.getSandbox().getCamera(),
                sandboxState.getSandbox().getCurrentZoom()
        );

        ImGui.separator();

        ImGui.pushStyleColor(ImGuiCol.Text, ImColor.intToColor(0 ,255, 0));
        ImGui.text("Mouse position in world space");
        ImGui.popStyleColor();

        ImGui.text("Tile x: " + selTile.x);
        ImGui.text("Tile y: " + selTile.y);
    }

    /**
     * Shows info about the current ship.
     */
    private void currentShip() {
        ImGui.separator();

        ImGui.pushStyleColor(ImGuiCol.Text, ImColor.intToColor(0 ,255, 0));
        ImGui.text("Current ship");
        ImGui.popStyleColor();

        // a ship was clicked with the left mouse button
        var currentShip = sandboxState.getSandbox().getShipping().getCurrentShip();
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
    }

    /**
     * The current target position in world space.
     */
    private void targetWorldSpacePosition() {
        ImGui.separator();

        ImGui.pushStyleColor(ImGuiCol.Text, ImColor.intToColor(0 ,255, 0));
        ImGui.text("Current target");
        ImGui.popStyleColor();

        // a target was selected with the right mouse button
        var target = sandboxState.getSandbox().getShipping().getTarget();
        if (target != null) {
            ImGui.text("Target x: " + target.x);
            ImGui.text("Target y: " + target.y);
        } else {
            ImGui.text("Target: none");
        }
    }

    /**
     * The current direction and angle to the target.
     */
    private void targetDirection() {
        var d = sandboxState.getSandbox().getShipping().getCurrentTargetDirection();
        if (d != null) {
            ImGui.text("dir x: " + d.x + " dir y: " + d.y);
            ImGui.text("angle: " + d.z);
        }
    }
}
