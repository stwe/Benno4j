/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.input;

import de.sg.benno.*;
import de.sg.benno.chunk.Island5;
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.file.ImageFile;
import de.sg.benno.renderer.TileGraphicRenderer;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import de.sg.ogl.input.MouseInput;
import de.sg.ogl.renderer.TileRenderer;
import de.sg.ogl.resource.Texture;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2;

/**
 * Represents a MousePicker
 */
public class MousePicker {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * Highlights a cell.
     */
    private static final String CELL_FILE = "/debug/frame.png";

    /**
     * Highlights a tile.
     */
    private static final String TILE_FILE = "/debug/red.png";

    /**
     * To select tiles.
     */
    private static final String CORNER_FILE = "debug/corner.png";

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * Renders a highlighted tile.
     * The {@link TileRenderer} is used for debugging.
     */
    private final TileRenderer tileRenderer;

    /**
     * Renders a single texture.
     * The {@link TileGraphicRenderer} is used to highlight the current tile under the mouse.
     */
    private final TileGraphicRenderer tileGraphicRenderer;

    /**
     * Highlights a cell.
     */
    private Texture rectangleTexture;

    /**
     * Highlights a tile.
     */
    private Texture tileTexture;

    /**
     * Corner png files to select tiles.
     */
    private final HashMap<Zoom, ImageFile> corners = new HashMap<>();

    /**
     * The {@link Water} object.
     */
    private final Water water;

    /**
     * The {@link Terrain} object.
     */
    private final Terrain terrain;

    /**
     * The {@link Shipping} object.
     */
    private final Shipping shipping;

    /**
     * The current {@link TileGraphic} under mouse;
     */
    private TileGraphic currentTileGraphic;

    /**
     * Either the picker can determine water tiles or the higher drawing island tiles.
     */
    private TileGraphic.TileHeight searchMode;

    /**
     * A* path finding.
     */
    private final Astar astar;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link MousePicker} object.
     *
     * @param context {@link Context}
     * @param water {@link Water}
     * @param terrain {@link Terrain}
     * @param shipping {@link Shipping}
     * @param searchMode Initializes the search mode.
     *                   Either the picker can determine water tiles or the higher drawing island tiles.
     * @throws Exception If an error is thrown.
     */
    public MousePicker(Context context, Water water, Terrain terrain, Shipping shipping, TileGraphic.TileHeight searchMode) throws Exception {
        LOGGER.debug("Creates MousePicker object.");

        Objects.requireNonNull(context, "context must not be null");
        this.tileRenderer = new TileRenderer(context.engine);
        this.tileGraphicRenderer = new TileGraphicRenderer(context);

        this.water = Objects.requireNonNull(water, "water must not be null");
        this.terrain = Objects.requireNonNull(terrain, "terrain must not be null");
        this.shipping = Objects.requireNonNull(shipping, "shipping must not be null");

        this.searchMode = searchMode;

        this.astar = new Astar();

        init(context);
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #currentTileGraphic}.
     *
     * @return {@link #currentTileGraphic}
     */
    public TileGraphic getCurrentTileGraphic() {
        return currentTileGraphic;
    }

    /**
     * Get {@link #searchMode}.
     *
     * @return {@link #searchMode}
     */
    public TileGraphic.TileHeight getSearchMode() {
        return searchMode;
    }

    //-------------------------------------------------
    // Setter
    //-------------------------------------------------

    /**
     * Set {@link #searchMode}.
     *
     * @param searchMode The search mode: either the picker can determine water tiles
     *                   or the higher drawing island tiles.
     */
    public void setSearchMode(TileGraphic.TileHeight searchMode) {
        this.searchMode = searchMode;
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Load png files for highlighting.
     *
     * @param context {@link Context}
     * @throws Exception If an error is thrown.
     */
    private void init(Context context) throws Exception {
        rectangleTexture = context.engine.getResourceManager().loadResource(Texture.class, CELL_FILE);
        tileTexture = context.engine.getResourceManager().loadResource(Texture.class, TILE_FILE);

        var cornerGfxImageFile = new ImageFile(CORNER_FILE);

        var cornerMgfxImage = ImageFile.resizeImage(
                cornerGfxImageFile.getImage(),
                Zoom.MGFX.defaultTileWidth, Zoom.MGFX.defaultTileHeight
        );

        var cornerSgfxImage = ImageFile.resizeImage(
                cornerGfxImageFile.getImage(),
                Zoom.SGFX.defaultTileWidth, Zoom.SGFX.defaultTileHeight
        );

        corners.put(Zoom.GFX, cornerGfxImageFile);
        corners.put(Zoom.MGFX, new ImageFile(cornerMgfxImage));
        corners.put(Zoom.SGFX, new ImageFile(cornerSgfxImage));
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Handle mouse clicks.
     *
     * @param dt The delta time.
     * @param camera The {@link Camera} object.
     * @param zoom The current {@link Zoom}.
     */
    public void update(float dt, Camera camera, Zoom zoom) {
        if (MouseInput.isMouseInWindow()) {

            // select ship
            if (MouseInput.isMouseButtonDown(GLFW_MOUSE_BUTTON_1)) {
                // todo: selected evtl. besser mit Aabb's ausarbeiten

                // get tile under mouse
                var selected = getTileUnderMouse(camera, zoom);

                // todo: brute force searching - use a HashMap later
                for (var ship : shipping.getProvider().getShips4List()) {
                    if (ship.xPos == selected.x && ship.yPos == selected.y) {
                        shipping.setCurrentShip(ship);
                    } else {
                        shipping.setCurrentShip(null);
                        shipping.setTarget(null);
                        shipping.setPath(null);
                    }
                }
            }

            // set target
            if (MouseInput.isMouseButtonDown(GLFW_MOUSE_BUTTON_2)) {
                // get tile under mouse
                var selected = getTileUnderMouse(camera, zoom);

                if (shipping.getCurrentShip() != null) {
                    shipping.setTarget(selected);

                    var path = astar.findPathToMapPosition(
                            new Vector2i(shipping.getCurrentShip().xPos, shipping.getCurrentShip().yPos),
                            selected
                    );

                    shipping.setPath(path);
                } else {
                    shipping.setTarget(null);
                    shipping.setPath(null);
                }
            }
        }
    }

    private void oldHandleMouseClick(Camera camera, Zoom zoom) {
        var updated = false;

        // get tile under mouse
        var selected = getTileUnderMouse(camera, zoom);

        // try to get an island
        var island5Optional = Island5.isIsland5OnPosition(
                selected.x,
                selected.y,
                terrain.getProvider().getIsland5List()
        );

        // when an island is found
        if (island5Optional.isPresent()) {
            updated = terrain.updateSelectedTile(island5Optional.get(), selected);
        }

        // if no island is found, try to update a water tile
        if (!updated) {
            updated = water.updateSelectedWaterTile(selected);
        }
    }

    /**
     * Highlight the current tile and read out the {@link #currentTileGraphic}.
     *
     * @param camera The {@link Camera} object.
     * @param zoom The current {@link Zoom}
     */
    public void render(Camera camera, Zoom zoom) {
        if (MouseInput.isMouseInWindow()) {
            // get tile under mouse
            var worldPosition = getTileUnderMouse(camera, zoom);

            // try to get an island
            var island5Optional = Island5.isIsland5OnPosition(
                    worldPosition.x,
                    worldPosition.y,
                    terrain.getProvider().getIsland5List()
            );

            Optional<TileGraphic> tileGraphicOptional;

            // when an island is found
            if (island5Optional.isPresent()) {
                tileGraphicOptional = terrain.getTileGraphic(zoom, island5Optional.get(), worldPosition.x, worldPosition.y);
            } else {
                // or a water tile
                tileGraphicOptional = water.getWaterTileGraphic(zoom, worldPosition.x, worldPosition.y);
            }

            // render highlighting
            if (tileGraphicOptional.isPresent()) {
                currentTileGraphic = tileGraphicOptional.get();
                renderHighlighting(camera);
            } else {
                currentTileGraphic = null;
            }

            renderDebug(zoom);
        }
    }

    /**
     * Render highlighting texture.
     *
     * @param camera The {@link Camera} object.
     */
    private void renderHighlighting(Camera camera) {
        tileGraphicRenderer.render(camera, tileTexture, currentTileGraphic.getModelMatrix());
    }

    /**
     * Highlights cell and tile without corners.
     *
     * @param zoom The current {@link Zoom}
     */
    private void renderDebug(Zoom zoom) {
        var size = getTileWidthAndHeight(zoom);
        var cell = getActiveCell(zoom);

        var pos = new Vector2f(cell.x * size.x, cell.y * size.y);
        var scale = new Vector2f(size.x, size.y);

        if (searchMode == TileGraphic.TileHeight.CLIFF) {
            pos.y -= (float)TileGraphic.TileHeight.CLIFF.value / zoom.elevation;
        }

        tileRenderer.render(rectangleTexture.getId(), pos, scale);
        tileRenderer.render(tileTexture.getId(), pos, scale);
    }

    //-------------------------------------------------
    // Utils
    //-------------------------------------------------

    /**
     * Get width and height of a tile.
     *
     * @param zoom The current {@link Zoom}
     *
     * @return The width and height as {@link Vector2i}
     */
    private static Vector2i getTileWidthAndHeight(Zoom zoom) {
        var width = zoom.defaultTileWidth;

        var height = zoom.defaultTileHeight;
        if (height == 31) {
            height = 32;
        }

        return new Vector2i(width, height);
    }

    /**
     * Work out active cell in screen space.
     *
     * @param zoom The current {@link Zoom}
     *
     * @return The screen space coordinates as {@link Vector2i}
     */
    private Vector2i getActiveCell(Zoom zoom) {
        var wh = getTileWidthAndHeight(zoom);

        var cell = new Vector2i();
        cell.x = (int)MouseInput.getX() / wh.x;

        if (searchMode == TileGraphic.TileHeight.CLIFF) {
            cell.y = ((int) MouseInput.getY() + zoom.defaultTileHeightHalf) / wh.y;
        } else {
            cell.y = (int) MouseInput.getY() / wh.y;
        }

        return cell;
    }

    /**
     * Work out mouse offset into cell in screen space.
     *
     * @param zoom The current {@link Zoom}
     *
     * @return The offset coordinates as {@link Vector2i}
     */
    private Vector2i getCellOffset(Zoom zoom) {
        var wh = getTileWidthAndHeight(zoom);

        var offset = new Vector2i();
        offset.x = (int)MouseInput.getX() % wh.x;

        if (searchMode == TileGraphic.TileHeight.CLIFF) {
            offset.y = ((int) MouseInput.getY() + zoom.defaultTileHeightHalf) % wh.y;
        } else {
            offset.y = (int) MouseInput.getY() % wh.y;
        }

        return offset;
    }

    /**
     * Work out cell under mouse in world space.
     *
     * @param camera {@link Camera}
     * @param zoom The current {@link Zoom}
     *
     * @return The world space coordinates as {@link Vector2i}
     */
    private Vector2i getCellUnderMouse(Camera camera, Zoom zoom) {
        var wh = getTileWidthAndHeight(zoom);

        var origin = new Vector2i();
        origin.x = (int)camera.position.x / wh.x;
        origin.y = (int)camera.position.y / wh.y;

        var cell = getActiveCell(zoom);
        var selected = new Vector2i();
        selected.x = (cell.y + origin.y) + (cell.x + origin.x);
        selected.y = (cell.y + origin.y) - (cell.x + origin.x);

        selected.x += 1;
        selected.y -= 1;

        return selected;
    }

    /**
     * Work out tile under mouse in world space.
     *
     * @param camera {@link Camera}
     * @param zoom The current {@link Zoom}
     *
     * @return The world space coordinates as {@link Vector2i}
     */
    public Vector2i getTileUnderMouse(Camera camera, Zoom zoom) {
        var cell = getCellUnderMouse(camera, zoom);
        var offset = getCellOffset(zoom);

        // "Bodge" selected cell by sampling corners
        var pixel = corners.get(zoom).getFastRGB(offset.x, offset.y);
        if (pixel[0] == 255 && pixel[1] == 0 && pixel[2] == 0) {
            cell.x -= 1;
        } else if (pixel[0] == 0 && pixel[1] == 255 && pixel[2] == 0) {
            cell.y -= 1;
        } else  if (pixel[0] == 0 && pixel[1] == 0 && pixel[2] == 255) {
            cell.y += 1;
        } else if (pixel[0] == 255 && pixel[1] == 255 && pixel[2] == 0) {
            cell.x += 1;
        }

        return cell;
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the MousePicker.");

        corners.forEach((k, v) -> v.cleanUp());

        tileRenderer.cleanUp();
        tileGraphicRenderer.cleanUp();
    }
}
