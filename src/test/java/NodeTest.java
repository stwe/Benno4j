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

import de.sg.benno.Node;
import org.joml.Vector2i;

import static org.junit.jupiter.api.Assertions.*;

class NodeTest {

    @org.junit.jupiter.api.Test
    void testEquals() {
        var node0 = new Node();
        node0.position = new Vector2i(12, 24);
        node0.parentPosition = new Vector2i( 66, 88);
        node0.g = 3.0f;
        node0.h = 1.0f;
        node0.f = 8.0f;

        var node1 = new Node();
        node1.position = null;
        node1.parentPosition = null;
        node1.g = 5.0f;
        node1.h = 4.0f;
        node1.f = 7.5f;

        var node2 = new Node();
        node2.position = new Vector2i(12, 24);
        node2.parentPosition = new Vector2i( 66, 88);
        node2.g = 3.0f;
        node2.h = 1.0f;
        node2.f = 8.0f;

        assertNotEquals(node0, node1);
        assertNotEquals(node1, node0);

        assertNotEquals(node1, node2);
        assertNotEquals(node2, node1);

        assertEquals(node0, node2);
        assertEquals(node2, node0);

        assertEquals(node2, node2);

        assertEquals(node0.hashCode(), node2.hashCode());
    }
}
