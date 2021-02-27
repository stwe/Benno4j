/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.gui;

import de.sg.benno.file.BshFile;
import de.sg.ogl.Log;
import de.sg.ogl.SgOglEngine;
import de.sg.ogl.gui.Gui;
import de.sg.ogl.gui.event.GuiEvent;
import de.sg.ogl.gui.event.GuiListener;
import de.sg.ogl.gui.widget.GuiButton;
import de.sg.ogl.gui.widget.GuiPanel;
import de.sg.ogl.state.StateMachine;
import org.joml.Vector2f;

import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class MainMenu {

    private static final int BACKGROUND = 0;
    private static final int SHIP = 14;

    private static final int SINGLEPLAYER = 2;
    private static final int MULTIPLAYER = 3;
    private static final int OPTIONS = 4;
    private static final int CREDITS = 5;
    private static final int INTRO = 6;
    private static final int EXIT = 7;

    private static final int SINGLEPLAYER_SELECT = 8;
    private static final int MULTIPLAYER_SELECT = 9;
    private static final int OPTIONS_SELECT = 10;
    private static final int CREDITS_SELECT = 11;
    private static final int INTRO_SELECT = 12;
    private static final int EXIT_SELECT = 13;

    private final SgOglEngine engine;
    private final BshFile startBshFile;
    private final StateMachine stateMachine;

    private Gui mainMenuGui;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public MainMenu(SgOglEngine engine, BshFile startBshFile, StateMachine stateMachine) throws Exception {
        LOGGER.debug("Creates MainMenu object.");

        this.engine = Objects.requireNonNull(engine, "engine must not be null");
        this.startBshFile = Objects.requireNonNull(startBshFile, "startBshFile must not be null");
        this.stateMachine = Objects.requireNonNull(stateMachine, "stateMachine must not be null");

        create();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    public Gui getMainMenuGui() {
        return mainMenuGui;
    }

    //-------------------------------------------------
    // Create
    //-------------------------------------------------

    private void create() throws Exception {
        LOGGER.debug("Creates Main Menu Gui.");

        mainMenuGui = new Gui(engine);

        var backgroundTexture = startBshFile.getBshTextures().get(BACKGROUND).getTexture();
        var shipTexture = startBshFile.getBshTextures().get(SHIP).getTexture();

        var singleplayerTexture = startBshFile.getBshTextures().get(SINGLEPLAYER).getTexture();
        var multiplayerTexture = startBshFile.getBshTextures().get(MULTIPLAYER).getTexture();
        var optionsTexture = startBshFile.getBshTextures().get(OPTIONS).getTexture();
        var creditsTexture = startBshFile.getBshTextures().get(CREDITS).getTexture();
        var introTexture = startBshFile.getBshTextures().get(INTRO).getTexture();
        var exitTexture = startBshFile.getBshTextures().get(EXIT).getTexture();

        var singleplayerSelectTexture = startBshFile.getBshTextures().get(SINGLEPLAYER_SELECT).getTexture();
        var multiplayerSelectTexture = startBshFile.getBshTextures().get(MULTIPLAYER_SELECT).getTexture();
        var optionsSelectTexture = startBshFile.getBshTextures().get(OPTIONS_SELECT).getTexture();
        var creditsSelectTexture = startBshFile.getBshTextures().get(CREDITS_SELECT).getTexture();
        var introSelectTexture = startBshFile.getBshTextures().get(INTRO_SELECT).getTexture();
        var exitSelectTexture = startBshFile.getBshTextures().get(EXIT_SELECT).getTexture();

        // panels

        var bgPanel = new GuiPanel(
                new Vector2f(0.0f, 0.0f),
                (float)backgroundTexture.getWidth(),
                (float)backgroundTexture.getHeight(),
                backgroundTexture
        );

        var shipPanel = new GuiPanel(
                new Vector2f(500.0f, 359.0f),
                (float)shipTexture.getWidth(),
                (float)shipTexture.getHeight(),
                shipTexture
        );

        mainMenuGui.getMainPanel().add(bgPanel);
        mainMenuGui.getMainPanel().add(shipPanel);

        // singleplayer button

        var spBtn = new GuiButton(
                new Vector2f(113.0f, 362.0f),
                (float)singleplayerTexture.getWidth(),
                (float)singleplayerTexture.getHeight(),
                singleplayerTexture
        );

        bgPanel.add(spBtn);

        spBtn.addListener(new GuiListener<>() {
            @Override
            public void onClick(GuiEvent<GuiButton> event) {
                // todo
                try {
                    loadGameMenu();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onHover(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(singleplayerSelectTexture);
            }

            @Override
            public void onRelease(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(singleplayerTexture);
            }
        });

        // multiplayer button

        var mpBtn = new GuiButton(
                new Vector2f(113.0f, 415.0f),
                (float)multiplayerTexture.getWidth(),
                (float)multiplayerTexture.getHeight(),
                multiplayerTexture
        );

        bgPanel.add(mpBtn);

        mpBtn.addListener(new GuiListener<>() {
            @Override
            public void onClick(GuiEvent<GuiButton> event) {
                Log.LOGGER.debug("click MultiPlayerButton");
            }

            @Override
            public void onHover(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(multiplayerSelectTexture);
            }

            @Override
            public void onRelease(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(multiplayerTexture);
            }
        });

        // options button

        var optionsBtn = new GuiButton(
                new Vector2f(113.0f, 469.0f),
                (float)optionsTexture.getWidth(),
                (float)optionsTexture.getHeight(),
                optionsTexture
        );

        bgPanel.add(optionsBtn);

        optionsBtn.addListener(new GuiListener<>() {
            @Override
            public void onClick(GuiEvent<GuiButton> event) {
                Log.LOGGER.debug("click OptionsButton");
            }

            @Override
            public void onHover(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(optionsSelectTexture);
            }

            @Override
            public void onRelease(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(optionsTexture);
            }
        });

        // credits button

        var creditsBtn = new GuiButton(
                new Vector2f(113.0f, 523.0f),
                (float)creditsTexture.getWidth(),
                (float)creditsTexture.getHeight(),
                creditsTexture
        );

        bgPanel.add(creditsBtn);

        creditsBtn.addListener(new GuiListener<>() {
            @Override
            public void onClick(GuiEvent<GuiButton> event) {
                Log.LOGGER.debug("click CreditsButton");
            }

            @Override
            public void onHover(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(creditsSelectTexture);
            }

            @Override
            public void onRelease(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(creditsTexture);
            }
        });

        // intro button

        var introBtn = new GuiButton(
                new Vector2f(113.0f, 574.0f),
                (float)introTexture.getWidth(),
                (float)introTexture.getHeight(),
                introTexture
        );

        bgPanel.add(introBtn);

        introBtn.addListener(new GuiListener<>() {
            @Override
            public void onClick(GuiEvent<GuiButton> event) {
                Log.LOGGER.debug("click IntroButton");
            }

            @Override
            public void onHover(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(introSelectTexture);
            }

            @Override
            public void onRelease(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(introTexture);
            }
        });

        // exit button

        var exitBtn = new GuiButton(
                new Vector2f(113.0f, 630.0f),
                (float)exitTexture.getWidth(),
                (float)exitTexture.getHeight(),
                exitTexture
        );

        bgPanel.add(exitBtn);

        exitBtn.addListener(new GuiListener<>() {
            @Override
            public void onClick(GuiEvent<GuiButton> event) {
                glfwSetWindowShouldClose(engine.getWindow().getWindowHandle(), true);
            }

            @Override
            public void onHover(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(exitSelectTexture);
            }

            @Override
            public void onRelease(GuiEvent<GuiButton> event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(exitTexture);
            }
        });
    }

    //-------------------------------------------------
    // Change States
    //-------------------------------------------------

    private void loadGameMenu() throws Exception {
        stateMachine.change("game_menu");
    }
}
