/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.state;

import de.sg.benno.BennoRuntimeException;
import de.sg.benno.Camera;
import de.sg.benno.World;
import de.sg.benno.chunk.WorldData;
import de.sg.benno.debug.DebugUi;
import de.sg.benno.renderer.Zoom;
import de.sg.ogl.input.KeyInput;
import de.sg.ogl.state.ApplicationState;
import de.sg.ogl.state.StateMachine;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.*;

public class GameState extends ApplicationState {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    private World world;

    public Camera camera;

    public Zoom currentZoom = Zoom.GFX;

    private boolean wireframe = false;

    private DebugUi debugUi;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public GameState(StateMachine stateMachine) {
        super(stateMachine);

        LOGGER.debug("Creates GameState object.");
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
            camera = new Camera(0, 0, currentZoom);
            world = new World((WorldData)params[0], (Context)getStateMachine().getStateContext(), camera);
            world.init();
        } else {
            throw new BennoRuntimeException("Invalid world data provider type.");
        }

        debugUi = new DebugUi(this);
    }

    @Override
    public void input() {
        // exit game
        if (KeyInput.isKeyPressed(GLFW_KEY_ESCAPE)) {
            var context = (Context)getStateMachine().getStateContext();
            glfwSetWindowShouldClose(context.engine.getWindow().getWindowHandle(), true);
        }

        // wireframe flag
        if (KeyInput.isKeyPressed(GLFW_KEY_G)) {
            wireframe = !wireframe;
        }

        // change zoom
        if (KeyInput.isKeyPressed(GLFW_KEY_1)) {
            currentZoom = Zoom.SGFX;
            camera.resetPosition(currentZoom);
        }

        if (KeyInput.isKeyPressed(GLFW_KEY_2)) {
            currentZoom = Zoom.MGFX;
            camera.resetPosition(currentZoom);
        }

        if (KeyInput.isKeyPressed(GLFW_KEY_3)) {
            currentZoom = Zoom.GFX;
            camera.resetPosition(currentZoom);
        }
    }

    @Override
    public void update(float dt) {
        camera.update(currentZoom);
    }

    @Override
    public void render() {
        world.render(wireframe, currentZoom);
    }

    @Override
    public void renderImGui() {
        debugUi.render();
    }

    @Override
    public void cleanUp() {
        LOGGER.debug("Start clean up for the GameState.");

        world.cleanUp();
    }
}
