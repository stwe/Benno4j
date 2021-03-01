/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

import de.sg.benno.BennoConfig;
import de.sg.benno.file.BennoFiles;
import de.sg.benno.state.Context;
import de.sg.benno.state.GameMenuState;
import de.sg.benno.state.GameState;
import de.sg.benno.state.MainMenuState;
import de.sg.ogl.*;
import de.sg.ogl.state.StateMachine;

import java.io.IOException;

import static de.sg.ogl.Log.LOGGER;

public class BennoApp extends SgOglApplication {

    private BennoFiles bennoFiles;
    private StateMachine stateMachine;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public BennoApp() throws IOException, IllegalAccessException {
        LOGGER.debug("Creates BennoApp object.");

        ConfigLoader.load(BennoConfig.class, "/benno.properties");
        LOGGER.debug("Benno configuration loaded successfully.");
        LOGGER.debug("Title: {}", BennoConfig.ROOT_PATH);
        LOGGER.debug("Width: {}", BennoConfig.SAVEGAME_PATH);
    }

    //-------------------------------------------------
    // Override
    //-------------------------------------------------

    @Override
    public void init() throws Exception {
        LOGGER.debug("Starts initializing BennoApp ...");

        this.bennoFiles = new BennoFiles(BennoConfig.ROOT_PATH);

        Context stateContext = new Context();
        stateContext.engine = getEngine();
        stateContext.filesystem = this.bennoFiles;

        this.stateMachine = new StateMachine(stateContext);
        this.stateMachine.add("main_menu", new MainMenuState(stateMachine));
        this.stateMachine.add("game_menu", new GameMenuState(stateMachine));
        this.stateMachine.add("game", new GameState(stateMachine));
        this.stateMachine.change("main_menu");

        LOGGER.debug("BennoApp successfully initialized.");
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
        bennoFiles.cleanUp();

        // clean up current state
        stateMachine.cleanUp();
    }
}
