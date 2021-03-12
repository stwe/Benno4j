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
import de.sg.benno.file.GamFile;
import de.sg.benno.renderer.Zoom;
import de.sg.ogl.state.ApplicationState;
import de.sg.ogl.state.StateMachine;

import java.io.IOException;
import java.nio.file.Path;

import static de.sg.ogl.Log.LOGGER;

public class GameState extends ApplicationState {

    private GamFile gamFile;

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
    }

    @Override
    public void input() {

    }

    @Override
    public void update(float v) {

    }

    @Override
    public void render() {

    }

    @Override
    public void cleanUp() {
        LOGGER.debug("Start clean up for the GameState.");
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    private void loadSavegame(Path path) throws IOException {
        var context = (Context)getStateMachine().getStateContext();

        var buildings = context.bennoFiles.getDataFiles().getBuildings();
        var stadtfldBsh = context.bennoFiles.getBshFile(context.bennoFiles.getZoomableBshFilePath(
                Zoom.MGFX, BennoFiles.ZoomableBshFileName.STADTFLD_BSH
        ));

        gamFile = new GamFile(path, buildings, stadtfldBsh, Zoom.MGFX);
    }
}
