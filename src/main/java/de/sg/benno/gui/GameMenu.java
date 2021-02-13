/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.gui;

import de.sg.benno.file.BshFile;
import de.sg.ogl.Color;
import de.sg.ogl.Log;
import de.sg.ogl.SgOglEngine;
import de.sg.ogl.gui.Anchor;
import de.sg.ogl.gui.Gui;
import de.sg.ogl.gui.GuiButton;
import de.sg.ogl.gui.event.GuiButtonAdapter;
import de.sg.ogl.gui.event.GuiButtonEvent;
import de.sg.ogl.state.StateMachine;
import org.joml.Vector2f;

import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;

public class GameMenu {

    private static final int BACKGROUND = 0;
    private static final int LIST = 1;

    private static final int NEW_GAME = 15;
    private static final int LOAD_GAME = 16;
    private static final int CONTINUE_GAME = 17;
    private static final int MAIN_MENU = 18;

    private static final int NEW_GAME_SELECT = 19;
    private static final int LOAD_GAME_SELECT = 20;
    private static final int CONTINUE_GAME_SELECT = 21;
    private static final int MAIN_MENU_SELECT = 22;

    private final SgOglEngine engine;
    private final BshFile startBshFile;
    private final StateMachine stateMachine;

    private Gui gameMenuGui;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public GameMenu(SgOglEngine engine, BshFile startBshFile, StateMachine stateMachine) throws Exception {
        LOGGER.debug("Creates GameMenu object.");

        this.engine = Objects.requireNonNull(engine, "engine must not be null");
        this.startBshFile = Objects.requireNonNull(startBshFile, "startBshFile must not be null");
        this.stateMachine = Objects.requireNonNull(stateMachine, "stateMachine must not be null");

        create();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    public Gui getGameMenuGui() {
        return gameMenuGui;
    }

    //-------------------------------------------------
    // Create
    //-------------------------------------------------

    private void create() throws Exception {
        LOGGER.debug("Creates Game Menu Gui.");

        gameMenuGui = new Gui(engine);

        var backgroundTexture = startBshFile.getBshTextures().get(BACKGROUND).getTexture();
        var listTexture = startBshFile.getBshTextures().get(LIST).getTexture();

        var newGameTexture = startBshFile.getBshTextures().get(NEW_GAME).getTexture();
        var loadGameTexture = startBshFile.getBshTextures().get(LOAD_GAME).getTexture();
        var continueGameTexture = startBshFile.getBshTextures().get(CONTINUE_GAME).getTexture();
        var mainMenuTexture = startBshFile.getBshTextures().get(MAIN_MENU).getTexture();

        var newGameTextureSelect = startBshFile.getBshTextures().get(NEW_GAME_SELECT).getTexture();
        var loadGameTextureSelect = startBshFile.getBshTextures().get(LOAD_GAME_SELECT).getTexture();
        var continueGameTextureSelect = startBshFile.getBshTextures().get(CONTINUE_GAME_SELECT).getTexture();
        var mainMenuTextureSelect = startBshFile.getBshTextures().get(MAIN_MENU_SELECT).getTexture();

        var backgroundPanel = gameMenuGui.addPanel(
                Anchor.TOP_LEFT,
                new Vector2f(0.0f, 0.0f),
                backgroundTexture.getWidth(),
                backgroundTexture.getHeight(),
                Color.WHITE,
                backgroundTexture
        );

        var listPanel = gameMenuGui.addPanel(
                Anchor.TOP_LEFT,
                new Vector2f(500.0f, 359.0f),
                listTexture.getWidth(),
                listTexture.getHeight(),
                Color.WHITE,
                listTexture
        );

        var newGameButton = backgroundPanel.addButton(
                Anchor.TOP_LEFT,
                new Vector2f(113.0f, 362.0f),
                newGameTexture.getWidth(),
                newGameTexture.getHeight(),
                Color.WHITE,
                newGameTexture
        );

        newGameButton.addListener(new GuiButtonAdapter() {
            @Override
            public void onClick(GuiButtonEvent event) {
                Log.LOGGER.debug("click NewGameButton");
            }

            @Override
            public void onHover(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(newGameTextureSelect);
            }

            @Override
            public void onRelease(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(newGameTexture);
            }
        });

        var loadGameButton = backgroundPanel.addButton(
                Anchor.TOP_LEFT,
                new Vector2f(113.0f, 415.0f),
                loadGameTexture.getWidth(),
                loadGameTexture.getHeight(),
                Color.WHITE,
                loadGameTexture
        );

        loadGameButton.addListener(new GuiButtonAdapter() {
            @Override
            public void onClick(GuiButtonEvent event) {
                Log.LOGGER.debug("click LoadGameButton");
            }

            @Override
            public void onHover(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(loadGameTextureSelect);
            }

            @Override
            public void onRelease(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(loadGameTexture);
            }
        });

        var continueGameButton = backgroundPanel.addButton(
                Anchor.TOP_LEFT,
                new Vector2f(113.0f, 469.0f),
                continueGameTexture.getWidth(),
                continueGameTexture.getHeight(),
                Color.WHITE,
                continueGameTexture
        );

        continueGameButton.addListener(new GuiButtonAdapter() {
            @Override
            public void onClick(GuiButtonEvent event) {
                Log.LOGGER.debug("click ContinueGameButton");
            }

            @Override
            public void onHover(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(continueGameTextureSelect);
            }

            @Override
            public void onRelease(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(continueGameTexture);
            }
        });

        var mainMenuGameButton = backgroundPanel.addButton(
                Anchor.TOP_LEFT,
                new Vector2f(113.0f, 523.0f),
                mainMenuTexture.getWidth(),
                mainMenuTexture.getHeight(),
                Color.WHITE,
                mainMenuTexture
        );

        mainMenuGameButton.addListener(new GuiButtonAdapter() {
            @Override
            public void onClick(GuiButtonEvent event) {
                // todo
                try {
                    loadMainMenu();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onHover(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(mainMenuTextureSelect);
            }

            @Override
            public void onRelease(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(mainMenuTexture);
            }
        });
    }

    //-------------------------------------------------
    // Change States
    //-------------------------------------------------

    private void loadMainMenu() throws Exception {
        stateMachine.change("main_menu");
    }
}
