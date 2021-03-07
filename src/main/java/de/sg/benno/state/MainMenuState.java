/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.state;

import de.sg.benno.file.BennoFiles;
import de.sg.benno.gui.MainMenu;
import de.sg.ogl.state.ApplicationState;
import de.sg.ogl.state.StateMachine;

import static de.sg.ogl.Log.LOGGER;

public class MainMenuState extends ApplicationState {

    private MainMenu mainMenu;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public MainMenuState(StateMachine stateMachine) {
        super(stateMachine);

        LOGGER.debug("Creates MainMenuState object.");
    }

    //-------------------------------------------------
    // Implement State
    //-------------------------------------------------

    @Override
    public void init(Object... params) throws Exception {
        var context = (Context)getStateMachine().getStateContext();

        mainMenu = new MainMenu(
                context.engine,
                context.bennoFiles.getBshFile(BennoFiles.InterfaceBshFileName.START),
                this.getStateMachine()
        );
    }

    @Override
    public void input() {
        mainMenu.getMainMenuGui().input();
    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void render() {
        mainMenu.getMainMenuGui().render();
    }

    @Override
    public void cleanUp() {
        LOGGER.debug("Start clean up for the MainMenuState.");

        mainMenu.getMainMenuGui().cleanUp();
    }
}
