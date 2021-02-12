/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

import de.sg.benno.file.BennoFiles;
import de.sg.benno.state.Context;
import de.sg.benno.state.MainMenuState;
import de.sg.ogl.Color;
import de.sg.ogl.OpenGL;
import de.sg.ogl.SgOglApplication;
import de.sg.ogl.state.StateMachine;

import java.io.IOException;

public class BennoApp extends SgOglApplication {

    private StateMachine stateMachine;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public BennoApp() throws IOException, IllegalAccessException {
    }

    //-------------------------------------------------
    // Override
    //-------------------------------------------------

    @Override
    public void init() throws Exception {
        Context stateContext = new Context();
        stateContext.engine = getEngine();
        stateContext.filesystem = new BennoFiles("E:\\Anno");

        stateMachine = new StateMachine(stateContext);
        stateMachine.add("main_menu", new MainMenuState(stateMachine));
        stateMachine.change("main_menu");
    }

    @Override
    public void input() {
        // the current state input method
        stateMachine.input();
    }

    @Override
    public void update(float dt) {
        // update current state
        stateMachine.update(dt);
    }

    @Override
    public void render() {
        OpenGL.setClearColor(Color.CORNFLOWER_BLUE);
        OpenGL.clear();

        // render current state
        stateMachine.render();
    }

    @Override
    public void renderImGui() {

    }

    @Override
    public void cleanUp() {
        // clean up current state
        stateMachine.cleanUp();
    }
}
