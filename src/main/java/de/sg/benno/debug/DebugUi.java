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

package de.sg.benno.debug;

import de.sg.benno.chunk.Ship4;
import de.sg.benno.ecs.components.GfxIndexComponent;
import de.sg.benno.ecs.components.PositionComponent;
import de.sg.benno.ecs.components.Ship4Component;
import de.sg.benno.ecs.components.TargetComponent;
import de.sg.benno.ogl.Config;
import de.sg.benno.state.Context;
import imgui.ImColor;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.internal.ImGui;

import java.io.IOException;
import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;

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
        entityInfo();

        ImGui.end();
    }

    //-------------------------------------------------
    // Widgets
    //-------------------------------------------------

    /**
     * Shows current position.
     */
    private void cameraPosition() {
        ImGui.pushStyleColor(ImGuiCol.Text, ImColor.intToColor(0 ,255, 0));
        ImGui.text("Camera position");
        ImGui.popStyleColor();
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

        var context = (Context)sandboxState.getStateMachine().getStateContext();
        ImGui.text("Screen x: " + context.engine.getMouseInput().getX());
        ImGui.text("Screen y: " + context.engine.getMouseInput().getY());
    }

    /**
     * Shows entity components.
     */
    private void entityInfo() {
        ImGui.separator();
        for (var entity : sandboxState.getSandbox().getEcs().getEntityManager().getAllEntities()) {
            // entity debug name
            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.intToColor(0 ,255, 0));
            ImGui.text("Entity");
            ImGui.popStyleColor();
            ImGui.text("Debug name: " + entity.debugName);

            // list components
            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.intToColor(0 ,255, 0));
            ImGui.text("Components");
            ImGui.popStyleColor();
            for (var component : entity.getComponents()) {
                ImGui.text("Component: " + component.getClass().getSimpleName());
            }

            // gfx componenent
            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.intToColor(0 ,255, 0));
            ImGui.text("Gfx component data");
            ImGui.popStyleColor();
            var gfxIndexComponentOptional = entity.getComponent(GfxIndexComponent.class);
            gfxIndexComponentOptional.ifPresent(
                    gfxIndexComponent -> ImGui.text("Gfx index: " + gfxIndexComponent.gfxIndex)
            );

            // position component
            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.intToColor(0 ,255, 0));
            ImGui.text("Position component data");
            ImGui.popStyleColor();
            var positionComponentOptional = entity.getComponent(PositionComponent.class);
            positionComponentOptional.ifPresent(
                    positionComponent -> {
                        ImGui.text("World position x: " + positionComponent.worldPosition.x);
                        ImGui.text("World position y: " + positionComponent.worldPosition.y);
                    }
            );

            // target component
            var targetComponentOptional = entity.getComponent(TargetComponent.class);
            targetComponentOptional.ifPresent(
                    targetComponent -> {
                        ImGui.pushStyleColor(ImGuiCol.Text, ImColor.intToColor(0 ,255, 0));
                        ImGui.text("Target component data");
                        ImGui.popStyleColor();
                        ImGui.text("Target world position x: " + targetComponent.targetWorldPosition.x);
                        ImGui.text("Target world position y: " + targetComponent.targetWorldPosition.y);
                    }
            );

            // ship4 component
            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.intToColor(0 ,255, 0));
            ImGui.text("Ship4 component data");
            ImGui.popStyleColor();
            var shipComponentOptional = entity.getComponent(Ship4Component.class);
            if (shipComponentOptional.isPresent()) {
                var ship = shipComponentOptional.get().ship4;
                ImGui.text("Name: " + ship.name);
                ImGui.text("X: " + ship.xPos);
                ImGui.text("Y: " + ship.yPos);
                ImGui.text("Health: " + ship.health);
                ImGui.text("Cannons: " + ship.numberOfCannons);
                ImGui.text("Type: " + Ship4.ShipType.valueOfType(ship.type));
                ImGui.text("Direction: " + ship.direction);
            }
        }
    }
}
