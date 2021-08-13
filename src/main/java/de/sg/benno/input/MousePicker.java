/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.input;

import de.sg.benno.file.ImageFile;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import de.sg.ogl.input.MouseInput;
import de.sg.ogl.renderer.TileRenderer;
import de.sg.ogl.resource.Texture;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.HashMap;

import static de.sg.ogl.Log.LOGGER;

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
     */
    private final TileRenderer tileRenderer;

    /**
     * Highlights a cell.
     */
    private Texture rectangle;

    /**
     * Highlights a tile.
     */
    private Texture highlight;

    /**
     * Corner png files to select tiles.
     */
    private final HashMap<Zoom, ImageFile> corners = new HashMap<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link MousePicker} object.
     *
     * @param context {@link Context}
     * @throws Exception If an error is thrown.
     */
    public MousePicker(Context context) throws Exception {
        LOGGER.debug("Creates MousePicker object.");

        tileRenderer = new TileRenderer(context.engine);

        init(context);
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Load png files.
     *
     * @param context {@link Context}
     * @throws Exception If an error is thrown.
     */
    private void init(Context context) throws Exception {
        rectangle = context.engine.getResourceManager().loadResource(Texture.class, CELL_FILE);
        highlight = context.engine.getResourceManager().loadResource(Texture.class, TILE_FILE);

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
     * Highlights cell and tile.
     *
     * @param zoom The current {@link Zoom}
     */
    public void renderDebug(Zoom zoom, boolean highlightCell, boolean highlightTile) {
        var size = MousePicker.getTileWidthAndHeight(zoom);
        var cell = MousePicker.getActiveCell(zoom);

        if (highlightCell) {
            tileRenderer.render(
                    rectangle.getId(),
                    new Vector2f(cell.x * size.x, cell.y * size.y),
                    new Vector2f(size.x, size.y)
            );
        }

        if (highlightTile) {
            tileRenderer.render(
                    highlight.getId(),
                    new Vector2f(cell.x * size.x, cell.y * size.y),
                    new Vector2f(size.x, size.y)
            );
        }
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
    private static Vector2i getActiveCell(Zoom zoom) {
        var wh = getTileWidthAndHeight(zoom);

        var cell = new Vector2i();
        cell.x = (int)MouseInput.getX() / wh.x;
        cell.y = (int)MouseInput.getY() / wh.y;

        return cell;
    }

    /**
     * Work out mouse offset into cell in screen space.
     *
     * @param zoom The current {@link Zoom}
     *
     * @return The offset coordinates as {@link Vector2i}
     */
    private static Vector2i getCellOffset(Zoom zoom) {
        var wh = getTileWidthAndHeight(zoom);

        var offset = new Vector2i();
        offset.x = (int)MouseInput.getX() % wh.x;
        offset.y = (int)MouseInput.getY() % wh.y;

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
    public static Vector2i getCellUnderMouse(Camera camera, Zoom zoom) {
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
        var cell = MousePicker.getCellUnderMouse(camera, zoom);
        var offset = MousePicker.getCellOffset(zoom);

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
    }
}
