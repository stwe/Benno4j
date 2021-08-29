/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno;

import org.joml.Vector2i;

/**
 * The A* take a graph as input. A graph is a set of locations ({@link Node}).
 * A {@link Node} represents a tile in the world.
 */
public class Node {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    public Vector2i position;
    public Vector2i parentPosition;

    public float g = 0.0f;
    public float h = 0.0f;
    public float f = 0.0f;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Node} object.
     */
    public Node() {
    }

    /**
     * Constructs a new {@link Node} object.
     *
     * @param position The start position in world space.
     */
    public Node(Vector2i position) {
        this.position = position;
    }

    //-------------------------------------------------
    // Override
    //-------------------------------------------------

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + ((position == null) ? 0 : position.hashCode());
        hash = 31 * hash + ((parentPosition == null) ? 0 : parentPosition.hashCode());

        hash = 31 * hash + Float.floatToIntBits(g);
        hash = 31 * hash + Float.floatToIntBits(h);
        hash = 31 * hash + Float.floatToIntBits(f);

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!obj.getClass().equals(getClass())) {
            return false;
        }

        Node other = (Node) obj;

        if (position == null) {
            if (other.position != null) {
                return false;
            }
        } else if (!position.equals(other.position)) {
            return false;
        }

        if (parentPosition == null) {
            if (other.parentPosition != null) {
                return false;
            }
        } else if (!parentPosition.equals(other.parentPosition)) {
            return false;
        }

        return g == other.g && h == other.h && f == other.f;
    }
}
