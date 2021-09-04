/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno;

import de.sg.benno.debug.SandboxState;
import de.sg.benno.file.BennoFiles;
import de.sg.benno.file.GamFile;
import de.sg.benno.state.Context;
import de.sg.benno.state.GameState;
import de.sg.ogl.*;
import de.sg.ogl.state.ApplicationState;
import de.sg.ogl.state.StateMachine;

import java.io.IOException;

import static de.sg.benno.BennoConfig.GAME_STATE_NAME;
import static de.sg.ogl.Log.LOGGER;

/**
 * Represents the main class of the game.
 */
public class BennoApp extends SgOglApplication {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * A {@link BennoFiles} object.
     */
    private BennoFiles bennoFiles;

    /**
     * A {@link StateMachine} object.
     */
    private StateMachine stateMachine;

    /**
     * A {@link GamFile} object.
     */
    private GamFile gamFile;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link BennoApp} object.
     */
    public BennoApp() throws IOException, IllegalAccessException {
        LOGGER.debug("Creates BennoApp object.");

        ConfigLoader.load(BennoConfig.class, "/benno.properties");
        LOGGER.debug("Benno configuration loaded successfully.");
        LOGGER.debug("Root path: {}", BennoConfig.ROOT_PATH);
        LOGGER.debug("Savegame path: {}", BennoConfig.SAVEGAME_PATH);
    }

    //-------------------------------------------------
    // Override
    //-------------------------------------------------

    @Override
    public void init() throws Exception {
        LOGGER.debug("Starts initializing BennoApp ...");

        this.bennoFiles = new BennoFiles();

        Context stateContext = new Context();
        stateContext.engine = getEngine();
        stateContext.bennoFiles = this.bennoFiles;

        this.stateMachine = new StateMachine(stateContext);
        //this.stateMachine.add("main_menu", new MainMenuState(stateMachine));
        //this.stateMachine.add("game_menu", new GameMenuState(stateMachine));

        ApplicationState state = null;
        if (GAME_STATE_NAME.equals("game")) {
            state = new GameState(stateMachine);
        }

        if (GAME_STATE_NAME.equals("sandbox")) {
            state = new SandboxState(stateMachine);
        }

        if (state != null) {
            this.stateMachine.add(GAME_STATE_NAME, state);
        } else {
            throw new BennoRuntimeException("Invalid game state name.");
        }

        //this.stateMachine.change("main_menu");

        if (!bennoFiles.getSavegameFilePaths().isEmpty()) {
            // the GAM file is the data provider for the world, which the GameState is created
            if (!BennoConfig.SAVEGAME.isEmpty()) {
                // try to load the savegame from resources
                gamFile = new GamFile(Util.getFileFromResourceAsStream(BennoConfig.SAVEGAME), stateContext);
            } else {
                // try to load the first savegame
                gamFile = new GamFile(bennoFiles.getSavegameFilePaths().get(0), stateContext);
            }
            this.stateMachine.change(GAME_STATE_NAME, gamFile);
        } else {
            throw new BennoRuntimeException("No savegame found.");
        }

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
    public void renderImGui() throws Exception {

        // render ImGui current state
        stateMachine.renderImGui();
    }

    @Override
    public void cleanUp() {
        if (bennoFiles != null) {
            bennoFiles.cleanUp();
        }

        // clean up current state
        stateMachine.cleanUp();

        if (gamFile != null) {
            gamFile.cleanUp();
        }
    }
}
