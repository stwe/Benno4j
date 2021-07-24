/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno;

import de.sg.benno.chunk.WorldData;

import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;

/**
 * Represents a complete game world.
 */
public class World {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * The width of the world in tiles.
     */
    public static final int WORLD_WIDTH = 500;

    /**
     * The height of the world in tiles.
     */
    public static final int WORLD_HEIGHT = 350;

    /*
    Member Listen:
       Bebauung
       Animations
       Inseln
       Kontore
       Schiffe
       Soldaten
       Spieler
       etc...
    Methods:
       Listen mit Daten füllen
       über die Listen iter. und animieren, darstellen etc.
    */

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * Provides data for drawing. For example from a loaded game.
     */
    private final WorldData provider;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public World(WorldData provider) {
        LOGGER.debug("Creates World object from provider class {}.", provider.getClass());

        this.provider = Objects.requireNonNull(provider, "provider must not be null");
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------


    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    public void init() {

    }

    public void update(float dt) {

    }

    public void render() {

    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------


    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    public void cleanUp() {
        LOGGER.debug("Start clean up for the World.");

        provider.cleanUp();
    }
}
