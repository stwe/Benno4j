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

class EcsTest {

    private Ecs ecs;

    private static class Position implements Component {}
    private static class Transform implements Component {}
    private static class Health implements Component {}
    private static class Velocity implements Component {}
    private static class Attack implements Component {}

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
    void getEntityManager() {
        var em = ecs.getEntityManager();
        assertNotNull(em);
        assertEquals(EntityManager.class, em.getClass());
    }

    @Test
    void getSystemManager() {
        var sm = ecs.getSystemManager();
        assertNotNull(sm);
        assertEquals(SystemManager.class, sm.getClass());
    }

    @Test
    void init() {
    }

    @Test
    void input() {
    }

    @Test
    void update() {

    }

    @Test
    void render() {
    }

    @Test
    void cleanUp() {
    }
}
