/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.state;

import de.sg.benno.file.BennoFiles;
import de.sg.benno.file.BshFile;
import de.sg.benno.gui.MainMenu;
import de.sg.ogl.state.ApplicationState;
import de.sg.ogl.state.StateMachine;

public class MainMenuState extends ApplicationState {

    private MainMenu mainMenu;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public MainMenuState(StateMachine stateMachine) {
        super(stateMachine);
    }

    //-------------------------------------------------
    // Implement State
    //-------------------------------------------------

    @Override
    public void init() throws Exception {
        var context = (Context)getStateMachine().getStateContext();
        var startBshFile = new BshFile(
                context.filesystem.getInterfaceBshFilePath(BennoFiles.InterfaceBshFile.START),
                context.paletteFile.getPalette(),
                false
        );

        mainMenu = new MainMenu(context.engine, startBshFile);
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
        mainMenu.getMainMenuGui().cleanUp();
    }
}
