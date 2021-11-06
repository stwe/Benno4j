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

package de.sg.benno.editor;

import de.sg.benno.ogl.Config;
import imgui.ImColor;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.internal.ImGui;

import java.io.IOException;
import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * ImGui stuff.
 */
public class EditorUi {

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
     * The parent {@link EditorState}.
     */
    private final EditorState editorState;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link EditorState} object.
     *
     * @param editorState The parent {@link EditorState}.
     */
    public EditorUi(EditorState editorState) {
        LOGGER.debug("Creates EditorUi object.");

        this.editorState = Objects.requireNonNull(editorState, "editorState must not be null");;
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

        ImGui.begin("Editor", windowFlags);

        cameraPosition();
        currentZoom();

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
        ImGui.text("Camera screen space x: " + editorState.getMap().getCamera().position.x + " (" + editorState.getMap().getCamera().positionInTileUnits.x+")");
        ImGui.text("Camera screen space y: " + editorState.getMap().getCamera().position.y + " (" + editorState.getMap().getCamera().positionInTileUnits.y+")");
    }

    /**
     * Shows current zoom.
     */
    private void currentZoom() {
        ImGui.separator();

        ImGui.pushStyleColor(ImGuiCol.Text, ImColor.intToColor(0 ,255, 0));
        ImGui.text("Zoom");
        ImGui.popStyleColor();
        ImGui.text("Zoom: " + editorState.getMap().getCurrentZoom());
    }
}
