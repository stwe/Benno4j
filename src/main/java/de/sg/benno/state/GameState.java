/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.state;

import de.sg.benno.BennoRuntimeException;
import de.sg.benno.World;
import de.sg.benno.chunk.WorldData;
import de.sg.benno.debug.DebugUi;
import de.sg.ogl.input.KeyInput;
import de.sg.ogl.state.ApplicationState;
import de.sg.ogl.state.StateMachine;

import java.io.IOException;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.*;

public class GameState extends ApplicationState {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link World}.
     */
    private World world;

    /**
     * An ImGui with some debug info.
     */
    private DebugUi debugUi;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link GameState} object.
     *
     * @param stateMachine The parent {@link StateMachine}.
     */
    public GameState(StateMachine stateMachine) {
        super(stateMachine);

        LOGGER.debug("Creates GameState object.");
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #world}.
     *
     * @return {@link #world}
     */
    public World getWorld() {
        return world;
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
            world = new World((WorldData)params[0], (Context)getStateMachine().getStateContext());
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

        world.input();
    }

    @Override
    public void update(float dt) {
        world.update(dt);
    }

    @Override
    public void render() {
        world.render();
    }

    @Override
    public void renderImGui() {
        // todo remove try catch
        try {
            debugUi.render();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanUp() {
        LOGGER.debug("Start clean up for the GameState.");

        world.cleanUp();
    }
}
