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
import de.sg.ogl.gui.Gui;
import de.sg.ogl.gui.event.GuiEvent;
import de.sg.ogl.gui.event.GuiListener;
import de.sg.ogl.gui.widget.GuiButton;
import de.sg.ogl.gui.widget.GuiPanel;
import de.sg.ogl.state.StateMachine;
import de.sg.ogl.text.TextRenderer;
import org.joml.Vector2f;

import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;
import static java.awt.Font.MONOSPACED;
import static java.awt.Font.PLAIN;

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
    private final TextRenderer textRenderer;

    private Gui gameMenuGui;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public GameMenu(SgOglEngine engine, BshFile startBshFile, StateMachine stateMachine) throws Exception {
        LOGGER.debug("Creates GameMenu object.");

        this.engine = Objects.requireNonNull(engine, "engine must not be null");
        this.startBshFile = Objects.requireNonNull(startBshFile, "startBshFile must not be null");
        this.stateMachine = Objects.requireNonNull(stateMachine, "stateMachine must not be null");
        this.textRenderer = new TextRenderer(this.engine, new java.awt.Font(MONOSPACED, PLAIN, 10));

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

        // panels

        var bgPanel = new GuiPanel(
                new Vector2f(0.0f, 0.0f),
                (float)backgroundTexture.getWidth(),
                (float)backgroundTexture.getHeight(),
                backgroundTexture
        );

        var listPanel = new GuiPanel(
                new Vector2f(500.0f, 359.0f),
                (float)listTexture.getWidth(),
                (float)listTexture.getHeight(),
                listTexture
        );

        gameMenuGui.getMainPanel().add(bgPanel);
        gameMenuGui.getMainPanel().add(listPanel);

        // new game button

        var newGameBtn = new GuiButton(
                new Vector2f(113.0f, 362.0f),
                (float)newGameTexture.getWidth(),
                (float)newGameTexture.getHeight(),
                newGameTexture
        );

        bgPanel.add(newGameBtn);

        newGameBtn.addListener(new GuiListener<>() {
            @Override
            public void onClick(GuiEvent<GuiButton> event) {
                Log.LOGGER.debug("click NewGameButton");
            }

            @Override
            public void onHover(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(newGameTextureSelect);
            }

            @Override
            public void onRelease(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(newGameTexture);
            }
        });

        // load game button

        var loadGameBtn = new GuiButton(
                new Vector2f(113.0f, 415.0f),
                (float)loadGameTexture.getWidth(),
                (float)loadGameTexture.getHeight(),
                loadGameTexture
        );

        bgPanel.add(loadGameBtn);

        loadGameBtn.addListener(new GuiListener<>(){
            @Override
            public void onClick(GuiEvent<GuiButton> event) {
                Log.LOGGER.debug("click LoadGameButton");
                // todo
                // todo: panel mit scrollbarem Text
                textRenderer.render(
                        "Lorem ipsum dolor sit amet",
                        510.0f, 365.0f,
                        Color.RED
                );
            }

            @Override
            public void onHover(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(loadGameTextureSelect);
            }

            @Override
            public void onRelease(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(loadGameTexture);
            }
        });

        // continue game button

        var continueGameBtn = new GuiButton(
                new Vector2f(113.0f, 469.0f),
                (float)continueGameTexture.getWidth(),
                (float)continueGameTexture.getHeight(),
                continueGameTexture
        );

        bgPanel.add(continueGameBtn);

        continueGameBtn.addListener(new GuiListener<>() {
            @Override
            public void onClick(GuiEvent<GuiButton> event) {
                Log.LOGGER.debug("click ContinueGameButton");
            }

            @Override
            public void onHover(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(continueGameTextureSelect);
            }

            @Override
            public void onRelease(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(continueGameTexture);
            }
        });

        // main menu game button

        var mainMenuGameBtn = new GuiButton(
                new Vector2f(113.0f, 523.0f),
                mainMenuTexture.getWidth(),
                mainMenuTexture.getHeight(),
                mainMenuTexture
        );

        bgPanel.add(mainMenuGameBtn);

        mainMenuGameBtn.addListener(new GuiListener<>() {
            @Override
            public void onClick(GuiEvent<GuiButton> event) {
                // todo
                try {
                    loadMainMenu();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onHover(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(mainMenuTextureSelect);
            }

            @Override
            public void onRelease(GuiEvent<GuiButton> event) {
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
