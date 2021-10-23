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

import de.sg.benno.BennoRuntimeException;
import de.sg.benno.chunk.WorldData;
import de.sg.benno.ogl.state.ApplicationState;
import de.sg.benno.ogl.state.StateMachine;
import de.sg.benno.state.Context;

import static de.sg.benno.ogl.Log.LOGGER;

public class SandboxState extends ApplicationState {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The game world.
     */
    private Sandbox sandbox;

    /**
     * ImGui to show some debug output.
     */
    private DebugUi debugUi;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link SandboxState} object.
     *
     * @param stateMachine The parent {@link StateMachine}.
     */
    public SandboxState(StateMachine stateMachine) {
        super(stateMachine);

        LOGGER.debug("Creates SandboxState object.");
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #sandbox}.
     *
     * @return {@link #sandbox}
     */
    public Sandbox getSandbox() {
        return sandbox;
    }

    //-------------------------------------------------
    // Implement State
    //-------------------------------------------------

    @Override
    public void init(Object... params) throws Exception {
        if (params.length != 1) {
            throw new BennoRuntimeException("Wrong total number of params.");
        }

        if (params[0] instanceof WorldData) {
            sandbox = new Sandbox((WorldData)params[0], (Context)getStateMachine().getStateContext());
        } else {
            throw new BennoRuntimeException("Invalid world data provider type.");
        }

        debugUi = new DebugUi(this);
    }

    @Override
    public void input() {
        var context = (Context)getStateMachine().getStateContext();
        context.engine.getWindow().closeIfEscKeyPressed();

        sandbox.input();
    }

    @Override
    public void update() {
        sandbox.update();
    }

    @Override
    public void render() {
        sandbox.render();
    }

    @Override
    public void renderImGui() throws Exception {
        debugUi.render();
    }

    @Override
    public void cleanUp() {
        LOGGER.debug("Start clean up for the SandboxState.");

        sandbox.cleanUp();
    }
}
