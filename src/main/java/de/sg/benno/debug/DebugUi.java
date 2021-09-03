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

        mousePosition();
        tileUnderMouse();
        currentShip();

        ImGui.end();
    }

    //-------------------------------------------------
    // Widgets
    //-------------------------------------------------

    private void mousePosition() {
        ImGui.separator();
        ImGui.text("Mouse position in BennoApp");
        ImGui.text("Mouse x: " + MouseInput.getX());
        ImGui.text("Mouse y: " + MouseInput.getY());
    }

    private void tileUnderMouse() {
        var selTile = sandboxState.getSandbox().getMousePicker().getTileUnderMouse(
                sandboxState.getSandbox().getCamera(),
                sandboxState.getSandbox().getCurrentZoom()
        );
        ImGui.separator();
        ImGui.text("Tile under mouse");
        ImGui.text("Tile x: " + selTile.x);
        ImGui.text("Tile y: " + selTile.y);
    }

    private void currentShip() {
        ImGui.separator();

        ImGui.text("Current ship");

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

        var target = sandboxState.getSandbox().getShipping().getTarget();
        if (target != null) {
            ImGui.text("Target x: " + target.x);
            ImGui.text("Target y: " + target.y);
        } else {
            ImGui.text("Target: none");
        }
    }
}
