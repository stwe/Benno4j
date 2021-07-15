/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.state;

import de.sg.benno.BennoRuntimeException;
import de.sg.benno.file.BennoFiles;
import de.sg.benno.file.BshFile;
import de.sg.benno.file.GamFile;
import de.sg.benno.renderer.DeepWaterRenderer;
import de.sg.benno.renderer.Zoom;
import de.sg.ogl.camera.OrthographicCamera;
import de.sg.ogl.input.KeyInput;
import de.sg.ogl.state.ApplicationState;
import de.sg.ogl.state.StateMachine;

import java.io.IOException;
import java.nio.file.Path;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class GameState extends ApplicationState {

    private GamFile gamFile;

    private DeepWaterRenderer tileRenderer;

    private BshFile bshFile;

    private OrthographicCamera camera;

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

        var context = (Context)getStateMachine().getStateContext();
        tileRenderer = new DeepWaterRenderer(context.engine);

        bshFile = gamFile.getBennoFiles().getBshFile(gamFile.getBennoFiles().getZoomableBshFilePath(
                Zoom.SGFX, BennoFiles.ZoomableBshFileName.STADTFLD_BSH
        ));

        camera = new OrthographicCamera();
        camera.setCameraVelocity(1000.0f);
    }

    @Override
    public void input() {
        if (KeyInput.isKeyPressed(GLFW_KEY_ESCAPE)) {
            var context = (Context)getStateMachine().getStateContext();
            glfwSetWindowShouldClose(context.engine.getWindow().getWindowHandle(), true);
        }

        // todo
        camera.update(0.016f);
    }

    @Override
    public void update(float dt) {
        //camera.update(dt);
    }

    @Override
    public void render() {
        for (var tile : gamFile.getDeepWaterTiles(Zoom.SGFX)) {
            tileRenderer.renderTile(bshFile.getBshTextures().get(tile.tileGfxInfo.gfxIndex), tile.screenPosition, camera);
        }
    }

    @Override
    public void cleanUp() {
        LOGGER.debug("Start clean up for the GameState.");

        tileRenderer.cleanUp();
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    private void loadSavegame(Path path) throws IOException {
        var context = (Context)getStateMachine().getStateContext();
        gamFile = new GamFile(path, context.bennoFiles);
    }
}
