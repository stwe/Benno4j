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

package de.sg.benno.ai;

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
