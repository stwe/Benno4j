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

import java.util.ArrayList;

import static java.lang.System.out;
import static org.junit.jupiter.api.Assertions.*;

class EntityManagerTest {

    ArrayList<Class<? extends Component>> componentTypes;

    private Ecs ecs;
    private EntityManager em;

    private Entity e0;
    private Entity e1;
    private Entity e2;

    static class Position implements Component {
        public Position() {
        }
    }

    static class Transform implements Component {
        public Transform() {
        }
    }

    static class Health implements Component {
        public Health() {
        }
    }

    static class Velocity implements Component {
        public Velocity() {
        }
    }

    static class Attack implements Component {
        public Attack() {
        }
    }

    static class MoveSystem extends EntitySystem {
        @SafeVarargs
        public MoveSystem(Ecs ecs, int priority, Class<? extends Component>... signatureComponentTypes) {
            super(ecs, priority, signatureComponentTypes);
        }

        @Override
        public void init(Object... params) throws Exception {}

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
        componentTypes = new ArrayList<>();
        componentTypes.add(Position.class);
        componentTypes.add(Transform.class);
        componentTypes.add(Health.class);
        componentTypes.add(Velocity.class);
        componentTypes.add(Attack.class);

        // todo
        //var moveSystemAdd = new MoveSystem(ecs, 0, Transform.class, Health.class);
        //ecs.addSystem(moveSystemAdd);
    }

    @Test
    void getEcs() {
    }

    @Test
    void getAllEntities() throws Exception {
        ecs = new Ecs(componentTypes);
        em = ecs.getEntityManager();

        e0 = em.createEntity();
        e0.addComponent(Position.class);
        e0.addComponent(Health.class);

        e1 = em.createEntity();
        e1.addComponent(Transform.class);
        e1.addComponent(Velocity.class);

        e2 = em.createEntity();
        e2.addComponent(Health.class);
        e2.addComponent(Attack.class);

        assertEquals(3, em.getAllEntities().size());
    }

    @Test
    void getEntitiesBySignature() throws Exception {
        ecs = new Ecs(componentTypes);
        em = ecs.getEntityManager();

        e0 = em.createEntity();
        e0.addComponent(Position.class);
        e0.addComponent(Health.class);

        e1 = em.createEntity();
        e1.addComponent(Transform.class);
        e1.addComponent(Velocity.class);

        e2 = em.createEntity();
        e2.addComponent(Health.class);
        e2.addComponent(Attack.class);

        var signature = new Signature(Transform.class, Velocity.class);
        signature.initSignatureBitSet(ecs.getAllComponentTypes());

        var entities = em.getEntitiesBySignature(signature);

        assertEquals(1, entities.size());
    }

    @Test
    void createEntity() {
        // todo systems

        for (var i = 0; i < 1000; i++) {
            em.createEntity();
        }

        assertEquals(3 + 1000, em.getAllEntities().size());
    }

    @Test
    void removeEntity() {
        // todo systems

        assertEquals(3, em.getAllEntities().size());

        em.removeEntity(e1);
        em.removeEntity(e2);
        assertEquals(1, em.getAllEntities().size());

        em.removeEntity(e0);
        assertEquals(0, em.getAllEntities().size());
    }
}
