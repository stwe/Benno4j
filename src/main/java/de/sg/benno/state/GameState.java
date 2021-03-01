/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.state;

import de.sg.benno.BennoRuntimeException;
import de.sg.ogl.state.ApplicationState;
import de.sg.ogl.state.StateMachine;

import java.nio.file.Path;

public class GameState extends ApplicationState {

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public GameState(StateMachine stateMachine) {
        super(stateMachine);
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

    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    private void loadSavegame(Path path) {

    }
}
