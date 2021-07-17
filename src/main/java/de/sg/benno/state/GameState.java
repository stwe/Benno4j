/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.state;

import de.sg.benno.BennoRuntimeException;
import de.sg.benno.file.GamFile;
import de.sg.benno.renderer.Zoom;
import de.sg.ogl.camera.OrthographicCamera;
import de.sg.ogl.input.KeyInput;
import de.sg.ogl.state.ApplicationState;
import de.sg.ogl.state.StateMachine;

import java.nio.file.Path;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.*;

public class GameState extends ApplicationState {

    private GamFile gamFile;

    private OrthographicCamera camera;

    private boolean wireframe = false;

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

        camera = new OrthographicCamera();
        camera.setCameraVelocity(1000.0f);
    }

    @Override
    public void input() {
        if (KeyInput.isKeyPressed(GLFW_KEY_ESCAPE)) {
            var context = (Context)getStateMachine().getStateContext();
            glfwSetWindowShouldClose(context.engine.getWindow().getWindowHandle(), true);
        }

        if (KeyInput.isKeyPressed(GLFW_KEY_G)) {
            wireframe = !wireframe;
        }

        // todo
        camera.update(0.016f);
    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void render() {
        gamFile.render(camera, wireframe, Zoom.MGFX);
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
