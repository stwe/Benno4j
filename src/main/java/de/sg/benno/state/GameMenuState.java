/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.state;

import de.sg.benno.file.BennoFiles;
import de.sg.benno.gui.GameMenu;
import de.sg.ogl.state.ApplicationState;
import de.sg.ogl.state.StateMachine;

import java.nio.file.Path;
import java.util.ArrayList;

import static de.sg.ogl.Log.LOGGER;

public class GameMenuState extends ApplicationState {

    private GameMenu gameMenu;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public GameMenuState(StateMachine stateMachine) {
        super(stateMachine);

        LOGGER.debug("Creates GameMenuState object.");
    }

    //-------------------------------------------------
    // Implement State
    //-------------------------------------------------

    @Override
    public void init(Object... params) throws Exception {
        var context = (Context)getStateMachine().getStateContext();

        gameMenu = new GameMenu(
                context.engine,
                context.filesystem.getBshFile(BennoFiles.InterfaceBshFileName.START),
                createLabels(context.filesystem.getSavegameFilePaths()),
                context.filesystem.getSavegameFilePaths(),
                this.getStateMachine()
        );
    }

    @Override
    public void input() {
        gameMenu.getGameMenuGui().input();
    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void render() {
        gameMenu.getGameMenuGui().render();
    }

    @Override
    public void cleanUp() {
        LOGGER.debug("Start clean up for the GameMenuState.");

        gameMenu.getGameMenuGui().cleanUp();
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    private ArrayList<String> createLabels(ArrayList<Path> values) {
        var labels = new ArrayList<String>();
        for (var value : values) {
            labels.add(value.getFileName().toString());
        }

        return labels;
    }
}
