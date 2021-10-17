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

class SignatureTest {

    private Ecs ecs;

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

        ecs = new Ecs();
    }

    @Test
    void matchesAll() throws Exception {
        var s0 = new Signature();
        s0.setAll(Transform.class, Velocity.class);

        var e0 = ecs.getEntityManager().createEntity();
        e0.addComponent(Transform.class);
        e0.addComponent(Velocity.class);

        var e1 = ecs.getEntityManager().createEntity();
        e1.addComponent(Transform.class);
        e1.addComponent(Velocity.class);
        e1.addComponent(Health.class);

        var e2 = ecs.getEntityManager().createEntity();
        e2.addComponent(Attack.class);
        e2.addComponent(Velocity.class);
        e2.addComponent(Health.class);

        assertTrue(s0.matches(e0));
        assertTrue(s0.matches(e1));
        assertFalse(s0.matches(e2));
    }

    @Test
    void matchesOne() throws Exception {
        var s0 = new Signature();
        s0.setAll(Transform.class, Velocity.class);
        s0.setOne(Health.class, Attack.class);

        var e0 = ecs.getEntityManager().createEntity();
        e0.addComponent(Transform.class);
        e0.addComponent(Velocity.class);
        e0.addComponent(Health.class);

        var e1 = ecs.getEntityManager().createEntity();
        e1.addComponent(Transform.class);
        e1.addComponent(Velocity.class);
        e1.addComponent(Attack.class);

        var e2 = ecs.getEntityManager().createEntity();
        e2.addComponent(Position.class);
        e2.addComponent(Velocity.class);
        e2.addComponent(Attack.class);

        var e3 = ecs.getEntityManager().createEntity();
        e3.addComponent(Position.class);
        e3.addComponent(Velocity.class);
        e3.addComponent(Transform.class);

        var e4 = ecs.getEntityManager().createEntity();
        e4.addComponent(Position.class);
        e4.addComponent(Velocity.class);

        assertTrue(s0.matches(e0));
        assertTrue(s0.matches(e1));
        assertFalse(s0.matches(e2));
        assertFalse(s0.matches(e4));
    }

    @Test
    void matchesExclude() throws Exception {
        var s0 = new Signature();
        s0.setAll(Transform.class, Velocity.class);
        s0.setExclude(Health.class, Attack.class);

        var e0 = ecs.getEntityManager().createEntity();
        e0.addComponent(Transform.class);
        e0.addComponent(Velocity.class);
        e0.addComponent(Health.class);

        var e1 = ecs.getEntityManager().createEntity();
        e1.addComponent(Transform.class);
        e1.addComponent(Velocity.class);

        var e2 = ecs.getEntityManager().createEntity();
        e2.addComponent(Transform.class);
        e2.addComponent(Velocity.class);
        e2.addComponent(Attack.class);

        var e3 = ecs.getEntityManager().createEntity();
        e3.addComponent(Transform.class);
        e3.addComponent(Velocity.class);
        e3.addComponent(Position.class);

        assertFalse(s0.matches(e0));
        assertTrue(s0.matches(e1));
        assertFalse(s0.matches(e2));
        assertTrue(s0.matches(e3));
    }

    @Test
    void matches() throws Exception {
        var s0 = new Signature();
        s0.setAll(Transform.class, Velocity.class);
        s0.setOne(Position.class, Health.class);
        s0.setExclude(Attack.class);

        var e0 = ecs.getEntityManager().createEntity();
        e0.addComponent(Transform.class);
        e0.addComponent(Velocity.class);
        e0.addComponent(Health.class);

        var e1 = ecs.getEntityManager().createEntity();
        e1.addComponent(Transform.class);
        e1.addComponent(Velocity.class);
        e1.addComponent(Health.class);
        e1.addComponent(Attack.class);

        var e2 = ecs.getEntityManager().createEntity();
        e2.addComponent(Transform.class);
        e2.addComponent(Velocity.class);

        assertTrue(s0.matches(e0));
        assertFalse(s0.matches(e1));
        assertFalse(s0.matches(e2));
    }
}
