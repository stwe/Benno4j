/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.input;

import de.sg.benno.renderer.Zoom;
import de.sg.ogl.input.MouseInput;
import org.joml.Vector2i;

/**
 * Represents a MousePicker
 */
public class MousePicker {

    /**
     * Get width and height of current zoom.
     *
     * @param zoom The current {@link Zoom}
     *
     * @return {@link Vector2i}
     */
    public static Vector2i getTileWidthAndHeight(Zoom zoom) {
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
     * @return {@link Vector2i}
     */
    public static Vector2i getActiveCell(Zoom zoom) {
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
     * @return {@link Vector2i}
     */
    public static Vector2i getCellOffset(Zoom zoom) {
        var wh = getTileWidthAndHeight(zoom);

        var offset = new Vector2i();
        offset.x = (int)MouseInput.getX() % wh.x;
        offset.y = (int)MouseInput.getY() % wh.y;

        return offset;
    }

    /**
     * Work out selected tile in world space.
     *
     * @param camera {@link Camera}
     * @param zoom The current {@link Zoom}
     *
     * @return {@link Vector2i}
     */
    public static Vector2i getSelectedTile(Camera camera, Zoom zoom) {
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
}
