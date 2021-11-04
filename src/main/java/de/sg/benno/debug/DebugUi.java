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
import de.sg.benno.ecs.components.*;
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
    private static final int HEIGHT = 590;

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

        /*
        ImGui::Begin("test");
        if (ImGui::TreeNode("Loxel Entities"))
        {
            if (ImGui::TreeNode("Base"))
            {
                ImGui::Indent();
                ImGui::Text("Num Slots");
                ImGui::Text("Count");
                ImGui::Unindent();
                ImGui::TreePop();
            }
            if (ImGui::TreeNode("Slots"))
            {
                ImGui::TreePop();
            }
            ImGui::TreePop();
        }
        ImGui::Indent();
        ImGui::Text("Previous Modifications");
        ImGui::Text("Debug Ticks");
        ImGui::Unindent();
        ImGui::End();
        */

        if (ImGui.treeNode("Entities")) {
            for (var entity : sandboxState.getSandbox().getEcs().getEntityManager().getAllEntities()) {
                if (ImGui.treeNode(entity.debugName)) {
                    if (ImGui.treeNode("Components")) {
                        ImGui.indent();
                        for (var component : entity.getComponents()) {
                            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.intToColor(0 ,255, 0));
                            ImGui.text(component.getClass().getSimpleName());
                            ImGui.popStyleColor();
                            if (component.getClass() == Ship4Component.class) {
                                ship4Data((Ship4Component) component);
                            }
                            if (component.getClass() == TargetComponent.class) {
                                targetData((TargetComponent) component);
                            }
                            if (component.getClass() == VelocityComponent.class) {
                                velocityData((VelocityComponent) component);
                            }
                            if (component.getClass() == GfxIndexComponent.class) {
                                gfxData((GfxIndexComponent) component);
                            }
                            if (component.getClass() == PositionComponent.class) {
                                positionData((PositionComponent) component);
                            }
                        }
                        ImGui.unindent();
                        ImGui.treePop();
                    }

                    ImGui.treePop();
                }
            }

            ImGui.treePop();
        }

        /*
        cameraPosition();
        currentZoom();
        screenSpacePosition();
        */

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
     * Shows current zoom.
     */
    private void currentZoom() {
        ImGui.separator();

        ImGui.pushStyleColor(ImGuiCol.Text, ImColor.intToColor(0 ,255, 0));
        ImGui.text("Zoom");
        ImGui.popStyleColor();
        ImGui.text("Zoom: " + sandboxState.getSandbox().getCurrentZoom());
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

    //-------------------------------------------------
    // Component data info
    //-------------------------------------------------

    private void ship4Data(Ship4Component ship4Component) {
        var ship = ship4Component.ship4;
        ImGui.bulletText("Name: " + ship.name);
        ImGui.bulletText("X: " + ship.xPos);
        ImGui.bulletText("Y: " + ship.yPos);
        ImGui.bulletText("Health: " + ship.health);
        ImGui.bulletText("Cannons: " + ship.numberOfCannons);
        ImGui.bulletText("Type: " + Ship4.ShipType.valueOfType(ship.type));
        ImGui.bulletText("Direction: " + ship.direction);
    }

    private void velocityData(VelocityComponent velocityComponent) {
        ImGui.bulletText("Velocity: " + velocityComponent.velocity);
    }

    private void targetData(TargetComponent targetComponent) {
        ImGui.bulletText("Target world position x: " + targetComponent.targetWorldPosition.x);
        ImGui.bulletText("Target world position y: " + targetComponent.targetWorldPosition.y);
        ImGui.bulletText("Target path size: " + targetComponent.path.size());
        ImGui.bulletText("Target index: " + targetComponent.nodeIndex);
    }

    private void gfxData(GfxIndexComponent gfxIndexComponent) {
        ImGui.bulletText("Gfx index: " + gfxIndexComponent.gfxIndex);
    }

    private void positionData(PositionComponent positionComponent) {
        ImGui.bulletText("World position x: " + positionComponent.worldPosition.x);
        ImGui.bulletText("World position y: " + positionComponent.worldPosition.y);
    }
}
