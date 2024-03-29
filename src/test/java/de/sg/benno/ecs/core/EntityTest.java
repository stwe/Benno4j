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

package de.sg.benno.ecs.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    private Entity e0;
    private Entity e1;
    private Entity e2;

    private static class Position implements Component {
        public Position() {
        }
    }

    private static class Transform implements Component {
        public Transform() {
        }
    }

    private static class Health implements Component {
        public Health() {
        }
    }

    private static class Velocity implements Component {
        public Velocity() {
        }
    }

    private static class Attack implements Component {
        public Attack() {
        }
    }

    @BeforeEach
    void setUp() {
        EcsSettings.setAllComponentTypes(
                Position.class,
                Transform.class,
                Health.class,
                Velocity.class,
                Attack.class
        );
        var ecs = new Ecs();

        var em = ecs.getEntityManager();

        e0 = em.createEntity();
        e1 = em.createEntity();
        e2 = em.createEntity();
    }

    @Test
    void getComponents() throws Exception {
        e0.addComponent(Position.class);
        e0.addComponent(Health.class);
        assertEquals(2, e0.getComponents().size());
    }

    @Test
    void getSignatureBitSet() throws Exception {
        // add
        e0.addComponent(Position.class);
        e0.addComponent(Health.class);
        var signature = e0.getComponentsBitSet();
        assertTrue(signature.get(0));
        assertTrue(signature.get(2));

        signature = e1.getComponentsBitSet();
        assertTrue(signature.isEmpty());

        e2.addComponent(Transform.class);
        e2.addComponent(Attack.class);
        signature = e2.getComponentsBitSet();
        assertTrue(signature.get(1));
        assertTrue(signature.get(4));

        // remove
        e2.removeComponent(Transform.class);
        signature = e2.getComponentsBitSet();
        assertFalse(signature.get(1));
        assertTrue(signature.get(4));

        e0.removeComponent(Position.class);
        e0.removeComponent(Health.class);
        signature = e0.getComponentsBitSet();
        assertTrue(signature.isEmpty());
    }

    @Test
    void getComponent() throws Exception {
        e0.addComponent(Position.class);
        e0.addComponent(Health.class);
        var health = e0.getComponent(Health.class);
        var transform = e0.getComponent(Transform.class);
        assertTrue(health.isPresent());
        assertFalse(transform.isPresent());
    }

    @Test
    void hasComponent() throws Exception {
        e0.addComponent(Position.class);
        e0.addComponent(Health.class);
        assertTrue(e0.hasComponent(Position.class));
        assertFalse(e0.hasComponent(Attack.class));
    }

    @Test
    void addComponent() throws Exception {
        e0.addComponent(Position.class);
        e0.addComponent(Health.class);
        assertEquals(2, e0.getComponents().size());

        e1.addComponent(Transform.class);
        e1.addComponent(Velocity.class);
        assertEquals(2, e1.getComponents().size());

        e2.addComponent(Health.class);
        e2.addComponent(Attack.class);
        assertEquals(2, e2.getComponents().size());

        // each component can only be added once
        e0.addComponent(Position.class);
        assertEquals(2, e0.getComponents().size());

        e2.addComponent(Attack.class);
        assertEquals(2, e2.getComponents().size());
    }

    @Test
    void removeComponent() throws Exception {
        e0.addComponent(Position.class);
        e0.addComponent(Health.class);
        assertEquals(2, e0.getComponents().size());

        e0.removeComponent(Position.class);
        assertEquals(1, e0.getComponents().size());

        // each component can only be removed once
        e0.removeComponent(Position.class);
        assertEquals(1, e0.getComponents().size());
    }
}
