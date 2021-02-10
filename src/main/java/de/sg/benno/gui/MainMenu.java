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
import org.joml.Vector2f;

import java.util.Objects;

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

    private Gui mainMenuGui;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public MainMenu(SgOglEngine engine, BshFile startBshFile) throws Exception {
        this.engine = Objects.requireNonNull(engine, "engine must not be null");
        this.startBshFile = Objects.requireNonNull(startBshFile, "startBshFile must not be null");;

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

        var backgroundPanel = mainMenuGui.addPanel(
                Anchor.TOP_LEFT,
                new Vector2f(0.0f, 0.0f),
                backgroundTexture.getWidth(),
                backgroundTexture.getHeight(),
                Color.WHITE,
                backgroundTexture
        );

        var shipPanel = mainMenuGui.addPanel(
                Anchor.TOP_LEFT,
                new Vector2f(500.0f, 359.0f),
                shipTexture.getWidth(),
                shipTexture.getHeight(),
                Color.WHITE,
                shipTexture
        );

        var singleplayerButton = backgroundPanel.addButton(
                Anchor.TOP_LEFT,
                new Vector2f(113.0f, 362.0f),
                singleplayerTexture.getWidth(),
                singleplayerTexture.getHeight(),
                Color.WHITE,
                singleplayerTexture
        );

        // singleplayer button listener
        singleplayerButton.addListener(new GuiButtonAdapter() {
            @Override
            public void onClick(GuiButtonEvent event) {
                Log.LOGGER.debug("On Click SinglePlayerButton");
            }

            @Override
            public void onHover(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(singleplayerSelectTexture);
            }

            @Override
            public void onRelease(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(singleplayerTexture);
            }
        });

        var multiplayerButton = backgroundPanel.addButton(
                Anchor.TOP_LEFT,
                new Vector2f(113.0f, 415.0f),
                multiplayerTexture.getWidth(),
                multiplayerTexture.getHeight(),
                Color.WHITE,
                multiplayerTexture
        );

        // multiplayer button listener
        multiplayerButton.addListener(new GuiButtonAdapter() {
            @Override
            public void onClick(GuiButtonEvent event) {
                Log.LOGGER.debug("On Click MultiPlayerButton");
            }

            @Override
            public void onHover(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(multiplayerSelectTexture);
            }

            @Override
            public void onRelease(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(multiplayerTexture);
            }
        });

        var optionsButton = backgroundPanel.addButton(
                Anchor.TOP_LEFT,
                new Vector2f(113.0f, 469.0f),
                optionsTexture.getWidth(),
                optionsTexture.getHeight(),
                Color.WHITE,
                optionsTexture
        );

        // options button listener
        optionsButton.addListener(new GuiButtonAdapter() {
            @Override
            public void onClick(GuiButtonEvent event) {
                Log.LOGGER.debug("On Click OptionsButton");
            }

            @Override
            public void onHover(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(optionsSelectTexture);
            }

            @Override
            public void onRelease(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(optionsTexture);
            }
        });

        var creditsButton = backgroundPanel.addButton(
                Anchor.TOP_LEFT,
                new Vector2f(113.0f, 523.0f),
                creditsTexture.getWidth(),
                creditsTexture.getHeight(),
                Color.WHITE,
                creditsTexture
        );

        // credits button listener
        creditsButton.addListener(new GuiButtonAdapter() {
            @Override
            public void onClick(GuiButtonEvent event) {
                Log.LOGGER.debug("On Click CreditsButton");
            }

            @Override
            public void onHover(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(creditsSelectTexture);
            }

            @Override
            public void onRelease(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(creditsTexture);
            }
        });

        var introButton = backgroundPanel.addButton(
                Anchor.TOP_LEFT,
                new Vector2f(113.0f, 574.0f),
                introTexture.getWidth(),
                introTexture.getHeight(),
                Color.WHITE,
                introTexture
        );

        // intro button listener
        introButton.addListener(new GuiButtonAdapter() {
            @Override
            public void onClick(GuiButtonEvent event) {
                Log.LOGGER.debug("On Click IntroButton");
            }

            @Override
            public void onHover(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(introSelectTexture);
            }

            @Override
            public void onRelease(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(introTexture);
            }
        });

        var exitButton = backgroundPanel.addButton(
                Anchor.TOP_LEFT,
                new Vector2f(113.0f, 630.0f),
                exitTexture.getWidth(),
                exitTexture.getHeight(),
                Color.WHITE,
                exitTexture
        );

        // exit button listener
        exitButton.addListener(new GuiButtonAdapter() {
            @Override
            public void onClick(GuiButtonEvent event) {
                glfwSetWindowShouldClose(engine.getWindow().getWindowHandle(), true);
            }

            @Override
            public void onHover(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(exitSelectTexture);
            }

            @Override
            public void onRelease(GuiButtonEvent event) {
                var source = (GuiButton)event.getSource();
                source.setTexture(exitTexture);
            }
        });
    }
}
