/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.debug;

import de.sg.benno.BennoRuntimeException;
import de.sg.benno.chunk.WorldData;
import de.sg.benno.state.Context;
import de.sg.ogl.input.KeyInput;
import de.sg.ogl.state.ApplicationState;
import de.sg.ogl.state.StateMachine;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class SandboxState extends ApplicationState {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    private Sandbox sandbox;

    private DebugUi debugUi;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public SandboxState(StateMachine stateMachine) {
        super(stateMachine);

        LOGGER.debug("Creates SandboxState object.");
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

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
        if (KeyInput.isKeyPressed(GLFW_KEY_ESCAPE)) {
            var context = (Context)getStateMachine().getStateContext();
            glfwSetWindowShouldClose(context.engine.getWindow().getWindowHandle(), true);
        }

        sandbox.input();
    }

    @Override
    public void update(float dt) {
        sandbox.update(dt);
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
