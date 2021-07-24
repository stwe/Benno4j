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
import de.sg.benno.debug.DebugUi;
import de.sg.benno.file.GamFile;
import de.sg.benno.renderer.Zoom;
import de.sg.ogl.input.KeyInput;
import de.sg.ogl.state.ApplicationState;
import de.sg.ogl.state.StateMachine;

import java.nio.file.Path;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.*;

public class GameState extends ApplicationState {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    private GamFile gamFile;

    public Camera camera;

    private boolean wireframe = false;

    public Zoom currentZoom = Zoom.GFX;

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

        var path = params[0];
        if (path instanceof Path) {
            loadSavegame((Path)path);
        } else {
            throw new BennoRuntimeException("Invalid parameter type.");
        }

        camera = new Camera(-2, -2, currentZoom);

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

    }

    @Override
    public void renderImGui() {
        debugUi.render();
    }

    @Override
    public void cleanUp() {
        LOGGER.debug("Start clean up for the GameState.");

        gamFile.cleanUp();
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    private void loadSavegame(Path path) throws Exception {
        gamFile = new GamFile(path, (Context)getStateMachine().getStateContext());
    }
}
