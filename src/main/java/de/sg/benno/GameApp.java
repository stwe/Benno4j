/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021, stwe <https://github.com/stwe/Benno4j>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.sg.benno;

import de.sg.benno.debug.SandboxState;
import de.sg.benno.editor.EditorState;
import de.sg.benno.file.BennoFiles;
import de.sg.benno.file.GamFile;
import de.sg.benno.ogl.ConfigLoader;
import de.sg.benno.ogl.OglApplication;
import de.sg.benno.ogl.state.ApplicationState;
import de.sg.benno.ogl.state.StateMachine;
import de.sg.benno.state.Context;
import de.sg.benno.util.Util;

import java.io.IOException;

import static de.sg.benno.BennoConfig.GAME_STATE_NAME;
import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents the main class of the game.
 */
public class GameApp extends OglApplication {

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
     * Constructs a new {@link GameApp} object.
     *
     * @throws IOException If an I/O error is thrown.
     * @throws IllegalAccessException If an illegal access exception is thrown.
     */
    public GameApp() throws IOException, IllegalAccessException {
        LOGGER.debug("Creates GameApp object.");

        ConfigLoader.load(BennoConfig.class, "/benno.properties");
        LOGGER.debug("Benno configuration loaded successfully.");
        LOGGER.debug("Root path: {}", BennoConfig.ROOT_PATH);
        LOGGER.debug("Savegame path: {}", BennoConfig.SAVEGAME_PATH);
    }

    //-------------------------------------------------
    // Override
    //-------------------------------------------------

    @Override
    public void init(Object... params) throws Exception {
        LOGGER.debug("Starts initializing GameApp ...");

        bennoFiles = new BennoFiles();

        Context stateContext = new Context();
        stateContext.engine = getEngine();
        stateContext.bennoFiles = bennoFiles;

        stateMachine = new StateMachine(stateContext);

        addGameState();
        loadGamFileAndChangeToState(stateContext);

        LOGGER.debug("GameApp successfully initialized.");
    }

    @Override
    public void input() {
        stateMachine.input();
    }

    @Override
    public void update() {
        stateMachine.update();
    }

    @Override
    public void render() {
        stateMachine.render();
    }

    @Override
    public void renderImGui() throws Exception {
        stateMachine.renderImGui();
    }

    @Override
    public void cleanUp() {
        if (bennoFiles != null) {
            bennoFiles.cleanUp();
        }

        stateMachine.cleanUp();

        if (gamFile != null) {
            gamFile.cleanUp();
        }
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Add a game state.
     */
    private void addGameState() {
        ApplicationState state = null;

        if (GAME_STATE_NAME.equals("sandbox")) {
            state = new SandboxState(stateMachine);
        }

        if (GAME_STATE_NAME.equals("editor")) {
            state = new EditorState(stateMachine);
        }

        if (state != null) {
            stateMachine.add(GAME_STATE_NAME, state);
        } else {
            throw new BennoRuntimeException("Invalid game state name.");
        }
    }

    /**
     * Load a GAM file and change to a {@link de.sg.benno.ogl.state.State}.
     */
    private void loadGamFileAndChangeToState(Context context) throws Exception {
        if (!bennoFiles.getSavegameFilePaths().isEmpty()) {
            if (!BennoConfig.SAVEGAME.isEmpty()) {
                // try to load the savegame from resources
                gamFile = new GamFile(Util.getFileFromResourceAsStream(BennoConfig.SAVEGAME), context);
            } else {
                // try to load the first savegame
                gamFile = new GamFile(bennoFiles.getSavegameFilePaths().get(0), context);
            }

            stateMachine.change(GAME_STATE_NAME, gamFile);
        } else {
            throw new BennoRuntimeException("No savegame found.");
        }
    }
}
