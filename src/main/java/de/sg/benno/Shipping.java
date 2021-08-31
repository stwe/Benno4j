/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno;

import de.sg.benno.chunk.Ship4;
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.chunk.WorldData;
import de.sg.benno.file.BshFile;
import de.sg.benno.input.Camera;
import de.sg.benno.renderer.TileGraphicRenderer;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;

public class Shipping {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * Provides data for drawing.
     */
    private final WorldData provider;

    /**
     * The {@link Context} object.
     */
    private final Context context;

    /**
     * References to the needed Bsh files for convenience.
     */
    private final HashMap<Zoom, BshFile> shipBshFiles = new HashMap<>();

    /**
     * Ships {@link TileGraphic} objects for each {@link Zoom} level.
     */
    private final HashMap<Zoom, ArrayList<TileGraphic>> shipTileGraphics = new HashMap<>();

    /**
     * A {@link TileGraphicRenderer} object.
     */
    private TileGraphicRenderer tileGraphicRenderer;

    /**
     * The current selected {@link Ship4}.
     */
    private Ship4 currentShip;

    /**
     * The current target position of the {@link #currentShip} in world space.
     */
    private Vector2i target;

    /**
     * The current search path of the {@link #currentShip}.
     */
    private ArrayList<Node> path;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Shipping} object.
     *
     * @param provider A {@link WorldData} object (e.g. {@link de.sg.benno.file.GamFile}).
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public Shipping(WorldData provider, Context context) throws Exception {
        LOGGER.debug("Creates Shipping object.");

        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");

        init();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #provider}.
     *
     * @return {@link #provider}
     */
    public WorldData getProvider() {
        return provider;
    }

    public HashMap<Zoom, ArrayList<TileGraphic>> getShipTileGraphics() {
        return shipTileGraphics;
    }

    /**
     * Get {@link #currentShip}.
     *
     * @return {@link #currentShip}.
     */
    public Ship4 getCurrentShip() {
        return currentShip;
    }

    /**
     * Get current {@link #target}.
     *
     * @return {@link #target}.
     */
    public Vector2i getTarget() {
        return target;
    }

    /**
     * Get current {@link #path}.
     *
     * @return {@link #path}
     */
    public ArrayList<Node> getPath() {
        return path;
    }

    //-------------------------------------------------
    // Setter
    //-------------------------------------------------

    /**
     * Set {@link #currentShip}.
     *
     * @param currentShip A {@link Ship4}
     */
    public void setCurrentShip(Ship4 currentShip) {
        this.currentShip = currentShip;
    }

    /**
     * Set current {@link #target}.
     *
     * @param target A {@link Vector2i}
     */
    public void setTarget(Vector2i target) {
        this.target = target;
    }

    /**
     * Set current {@link #path}.
     *
     * @param path A {@link ArrayList<Node>}
     */
    public void setPath(ArrayList<Node> path) {
        this.path = path;
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Updates all ships.
     *
     * @param dt The delta time.
     */
    public void update(float dt) {
        /*
        dt *= 16.0f;
        if (target != null) {
            var t = TileUtil.worldToScreen(target.x, target.y, Zoom.GFX.defaultTileWidthHalf, Zoom.GFX.defaultTileHeightHalf);
            for (var p : shipTileGraphics.get(Zoom.GFX)) {
                if (Math.abs(p.screenPosition.x - t.x) > dt || Math.abs(p.screenPosition.y - t.y) > dt) {
                    p.screenPosition.x += Math.max(-dt, Math.min(dt, t.x - p.screenPosition.x)); // move to destination clamping speed;
                    p.screenPosition.y += Math.max(-dt, Math.min(dt, t.y - p.screenPosition.y));
                    //p.atDest = false;  // flag the player is on the way
                } else {
                    // player directly over a till and not moving;
                    p.screenPosition.x = t.x; // ensure the player positioned correctly;
                    p.screenPosition.y = t.y;
                    //p.atDest = true;  // flag the player has arrived
                }
            }
        }
        */
    }

    /**
     * Render all ships.
     *
     * @param camera {@link Camera} object
     * @param zoom The current {@link Zoom}
     */
    public void render(Camera camera, Zoom zoom) {
        for (var ship : shipTileGraphics.get(zoom)) {
            tileGraphicRenderer.render(camera, ship, shipBshFiles.get(zoom));
        }
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Generates a {@link TileGraphic} object for each {@link de.sg.benno.chunk.Ship4}.
     *
     * @throws Exception If an error is thrown.
     */
    void init() throws Exception {
        // create renderer
        tileGraphicRenderer = new TileGraphicRenderer(context);

        // crate tile graphics
        for (var zoom : Zoom.values()) {
            LOGGER.debug("Create ship tiles for {}.", zoom.toString());

            var tileGraphics = new ArrayList<TileGraphic>();

            for (var ship : provider.getShips4List()) {
                // x: 154, y: 177
                LOGGER.debug("Create ship graphic tile on x: {}, y: {}.", ship.xPos, ship.yPos);

                var xWorldPos = ship.xPos + 1; // todo
                var yWorldPos = ship.yPos - 1;
                var gfx = ship.getCurrentGfx();

                var shipBshFile = context.bennoFiles.getShipBshFile(zoom);
                shipBshFiles.put(zoom, shipBshFile);

                var shipBshTexture = shipBshFile.getBshTextures().get(gfx);

                var tileGraphic = new TileGraphic();
                tileGraphic.gfx = gfx;
                tileGraphic.tileHeight = TileGraphic.TileHeight.SEA_LEVEL;
                tileGraphic.worldPosition.x = xWorldPos;
                tileGraphic.worldPosition.y = yWorldPos;

                var screenPosition = TileUtil.worldToScreen(xWorldPos, yWorldPos, zoom.defaultTileWidthHalf, zoom.defaultTileHeightHalf);
                var adjustHeight = TileUtil.adjustHeight(zoom.defaultTileHeightHalf, tileGraphic.tileHeight.value, zoom.elevation);
                screenPosition.y += adjustHeight;
                screenPosition.x -= shipBshTexture.getWidth();
                screenPosition.y -= shipBshTexture.getHeight();
                tileGraphic.screenPosition = new Vector2f(screenPosition);
                tileGraphic.screenPosition.x -= zoom.defaultTileWidthHalf * 0.5f;
                tileGraphic.screenPosition.y -= zoom.defaultTileHeightHalf * 0.5f;

                tileGraphic.size = new Vector2f(shipBshTexture.getWidth(), shipBshTexture.getHeight());
                tileGraphic.color = new Vector3f();

                tileGraphics.add(tileGraphic);
            }

            shipTileGraphics.put(zoom, tileGraphics);
        }
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the Shipping.");

        tileGraphicRenderer.cleanUp();
    }
}
