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

import static java.lang.System.out;
import static org.junit.jupiter.api.Assertions.*;

class EntityManagerTest {

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

    private static class MoveSystem extends EntitySystem {
        @SafeVarargs
        public MoveSystem(Class<? extends Component>... signatureComponentTypes) {
            super(signatureComponentTypes);
        }

        @Override
        public void input() {}

        @Override
        public void update() {
            for (var entity : getEntities()) {
                out.println("update MoveSystem");
            }
        }

        @Override
        public void render() {}

        @Override
        public void cleanUp() {}
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
    }

    @Test
    void getAllEntities() throws Exception {
        var ecs = new Ecs();
        var em = ecs.getEntityManager();

        var e0 = em.createEntity();
        e0.addComponent(Position.class);
        e0.addComponent(Health.class);

        var e1 = em.createEntity();
        e1.addComponent(Transform.class);
        e1.addComponent(Velocity.class);

        var e2 = em.createEntity();
        e2.addComponent(Health.class);
        e2.addComponent(Attack.class);

        assertEquals(3, em.getAllEntities().size());
    }

    @Test
    void getEntitiesBySignature() throws Exception {
        var ecs = new Ecs();
        var em = ecs.getEntityManager();

        var e0 = em.createEntity();
        e0.addComponent(Position.class);
        e0.addComponent(Health.class);

        var e1 = em.createEntity();
        e1.addComponent(Transform.class);
        e1.addComponent(Velocity.class);

        var e2 = em.createEntity();
        e2.addComponent(Health.class);
        e2.addComponent(Attack.class);

        var signature = new Signature(Transform.class, Velocity.class);
        var entities = em.getEntitiesBySignature(signature);

        assertEquals(1, entities.size());
    }

    @Test
    void createEntity() {
        var ecs = new Ecs();
        var em = ecs.getEntityManager();

        for (var i = 0; i < 1000; i++) {
            em.createEntity();
        }

        assertEquals(1000, em.getAllEntities().size());
    }

    @Test
    void removeEntity() throws Exception {
        var ecs = new Ecs();
        var em = ecs.getEntityManager();

        // create and add a system
        var moveSystem = new MoveSystem(Attack.class, Health.class);
        ecs.getSystemManager().addSystem(moveSystem);

        // create entities
        var e0 = em.createEntity();
        e0.debugName = "e0";
        e0.addComponent(Position.class);
        e0.addComponent(Health.class);

        var e1 = em.createEntity();
        e1.debugName = "e1";
        e1.addComponent(Transform.class);
        e1.addComponent(Velocity.class);

        var e2 = em.createEntity();
        e2.debugName = "e2";
        e2.addComponent(Health.class);
        e2.addComponent(Attack.class);

        assertEquals(3, em.getAllEntities().size());

        // create and add a system
        //var moveSystem = new MoveSystem(Attack.class, Health.class);
        //ecs.getSystemManager().addSystem(moveSystem);

        // check if entity e2 is in moveSystem
        ecs.processEntityTodos();
        assertEquals(1, moveSystem.getEntities().size());

        // remove
        em.removeEntity(e1);
        ecs.processEntityTodos();
        assertEquals(2, em.getAllEntities().size());
        assertEquals(1, moveSystem.getEntities().size());

        em.removeEntity(e2);
        ecs.processEntityTodos();
        assertEquals(1, em.getAllEntities().size());
        assertEquals(0, moveSystem.getEntities().size());

        em.removeEntity(e0);
        ecs.processEntityTodos();
        assertEquals(0, em.getAllEntities().size());
    }
}
