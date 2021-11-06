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

import de.sg.benno.ogl.state.ApplicationState;
import de.sg.benno.ogl.state.StateMachine;
import de.sg.benno.state.Context;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents an editor to create or change a world.
 */
public class EditorState extends ApplicationState {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The game world.
     */
    private Map map;

    /**
     * ImGui stuff.
     */
    private EditorUi editorUi;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link EditorState} object.
     *
     * @param stateMachine The parent {@link StateMachine}.
     */
    public EditorState(StateMachine stateMachine) {
        super(stateMachine);

        LOGGER.debug("Creates EditorState object.");
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #map}.
     *
     * @return {@link #map}
     */
    public Map getMap() {
        return map;
    }

    //-------------------------------------------------
    // Implement State
    //-------------------------------------------------

    @Override
    public void init(Object... params) throws Exception {
        map = new Map((Context)getStateMachine().getStateContext());
        editorUi = new EditorUi(this);
    }

    @Override
    public void input() {
        var context = (Context)getStateMachine().getStateContext();
        context.engine.getWindow().closeIfEscKeyPressed();

        map.input();
    }

    @Override
    public void update() {
        map.update();
    }

    @Override
    public void render() {
        map.render();
    }

    @Override
    public void renderImGui() throws Exception {
        editorUi.render();
    }

    @Override
    public void cleanUp() {
        LOGGER.debug("Start clean up for the EditorState.");

        map.cleanUp();
    }
}
