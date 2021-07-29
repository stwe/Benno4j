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

public class MousePicker {

    private static Vector2i getWidthAndHeight(Zoom zoom) {
        var width = zoom.defaultTileWidth;

        var height = zoom.defaultTileHeight;
        if (height == 31) {
            height = 32;
        }

        return new Vector2i(width, height);
    }

    public static Vector2i getActiveCell(Zoom zoom) {
        var wh = getWidthAndHeight(zoom);

        var cell = new Vector2i();
        cell.x = (int)MouseInput.getX() / wh.x;
        cell.y = (int)MouseInput.getY() / wh.y;

        return cell;
    }

    public static Vector2i getCellOffset(Zoom zoom) {
        var wh = getWidthAndHeight(zoom);

        var offset = new Vector2i();
        offset.x = (int)MouseInput.getX() % wh.x;
        offset.y = (int)MouseInput.getY() % wh.y;

        return offset;
    }

    public static Vector2i getSelectedTile(Camera camera, Zoom zoom) {
        var wh = getWidthAndHeight(zoom);

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
