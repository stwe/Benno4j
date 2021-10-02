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

package de.sg.benno.debug;

import de.sg.benno.content.Shipping;
import de.sg.benno.content.Water;
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.file.ImageFile;
import de.sg.benno.input.Camera;
import de.sg.benno.ogl.renderer.RenderUtil;
import de.sg.benno.ogl.renderer.SpriteRenderer;
import de.sg.benno.ogl.resource.Texture;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;

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
    private static final String RED_TILE_FILE = "/debug/red.png";

    /**
     * To select tiles.
     */
    private static final String CORNER_FILE = "debug/corner.png";

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link Context} object.
     */
    private final Context context;

    /**
     * Renders a texture.
     */
    private final SpriteRenderer spriteRenderer;

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
     * The current {@link TileGraphic} under mouse;
     */
    private TileGraphic currentTileGraphic;

    /**
     * Either the picker can determine water tiles or the higher drawing island tiles.
     */
    private final TileGraphic.TileHeight searchMode;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link MousePicker} object.
     *
     * @param context {@link Context}
     * @param water {@link Water}
     * @param searchMode Initializes the search mode.
     *                   Either the picker can determine water tiles or the higher drawing island tiles.
     * @throws Exception If an error is thrown.
     */
    public MousePicker(Context context, Water water, TileGraphic.TileHeight searchMode) throws Exception {
        LOGGER.debug("Creates MousePicker object.");

        this.context = Objects.requireNonNull(context, "context must not be null");
        this.spriteRenderer = new SpriteRenderer(context.engine);

        this.water = Objects.requireNonNull(water, "water must not be null");

        this.searchMode = searchMode;

        init(context);
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
        rectangleTexture = context.engine.getResourceManager().getTextureResource(CELL_FILE);
        tileTexture = context.engine.getResourceManager().getTextureResource(RED_TILE_FILE);

        var cornerGfxImageFile = new ImageFile(CORNER_FILE);

        var cornerMgfxImage = ImageFile.resizeImage(
                cornerGfxImageFile.getImage(),
                Zoom.MGFX.getTileWidth(), Zoom.MGFX.getTileHeight()
        );

        var cornerSgfxImage = ImageFile.resizeImage(
                cornerGfxImageFile.getImage(),
                Zoom.SGFX.getTileWidth(), Zoom.SGFX.getTileHeight()
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
     * @param camera The {@link Camera} object.
     * @param zoom The current {@link Zoom}.
     */
    public void update(Camera camera, Zoom zoom) {
        var mouseInput = context.engine.getMouseInput();
        if (mouseInput.isInWindow()) {

            // select ship with left mouse button
            if (mouseInput.isLeftButtonPressed()) {
                // todo: selected evtl. besser mit Aabb's ausarbeiten

                // get world position of the tile under mouse
                var selected = getTileUnderMouse(camera, zoom);

                // todo: brute force searching - use a HashMap later
                /*
                for (var ship : shipping.getProvider().getShips4List()) {
                    if (ship.xPos == selected.x && ship.yPos == selected.y) {
                        shipping.setCurrentShip(ship);
                    } else {
                        shipping.setCurrentShip(null);
                        shipping.setTarget(null);
                        shipping.setPath(null);
                    }
                }
                */
            }

            // set target with right mouse button
            if (mouseInput.isRightButtonPressed()) {
                // get world position of the tile under mouse
                var selected = getTileUnderMouse(camera, zoom);

                /*
                if (shipping.getCurrentShip() != null) {
                    shipping.setTarget(selected);

                    var path = Astar.findPathToTarget(
                            shipping.getCurrentShip(),
                            selected,
                            terrain.getPassableArea()
                    );

                    shipping.setPath(path);
                } else {
                    shipping.setTarget(null);
                    shipping.setPath(null);
                }
                */
            }
        }
    }

    /**
     * Highlight the current tile.
     *
     * @param camera The {@link Camera} object.
     * @param zoom The current {@link Zoom}
     */
    public void render(Camera camera, Zoom zoom) {
        if (context.engine.getMouseInput().isInWindow()) {
            // get world position of the tile under mouse
            var worldPosition = getTileUnderMouse(camera, zoom);

            // get the tile graphic
            var tileGraphicOptional = water.getWaterTileGraphic(zoom, worldPosition.x, worldPosition.y);

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
        spriteRenderer.render(camera.getViewMatrix(), tileTexture, currentTileGraphic.getModelMatrix());
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
            pos.y -= (float)TileGraphic.TileHeight.CLIFF.value / zoom.getElevation();
        }

        spriteRenderer.render(new Matrix4f(), rectangleTexture, RenderUtil.createModelMatrix(pos, scale));
        spriteRenderer.render(new Matrix4f(), tileTexture, RenderUtil.createModelMatrix(pos, scale));
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
        var width = zoom.getTileWidth();
        var height = zoom.getTileHeightHalf() * 2;

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

        var mouseInput = context.engine.getMouseInput();

        var cell = new Vector2i();
        cell.x = (int)mouseInput.getX() / wh.x;

        if (searchMode == TileGraphic.TileHeight.CLIFF) {
            cell.y = ((int)mouseInput.getY() + zoom.getTileHeightHalf()) / wh.y;
        } else {
            cell.y = (int)mouseInput.getY() / wh.y;
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

        var mouseInput = context.engine.getMouseInput();

        var offset = new Vector2i();
        offset.x = (int)mouseInput.getX() % wh.x;

        if (searchMode == TileGraphic.TileHeight.CLIFF) {
            offset.y = ((int)mouseInput.getY() + zoom.getTileHeightHalf()) % wh.y;
        } else {
            offset.y = (int)mouseInput.getY() % wh.y;
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

        spriteRenderer.cleanUp();
    }
}
