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

class EcsTest {

    private Ecs ecs;

    static class Position implements Component {}
    static class Transform implements Component {}
    static class Health implements Component {}
    static class Velocity implements Component {}
    static class Attack implements Component {}

    static class MoveSystem extends EntitySystem {
        public MoveSystem(Ecs ecs, int priority, Class<? extends Component>... signatureComponentTypes) {
            super(ecs, priority, signatureComponentTypes);
        }

        @Override
        public void init(Object... params) throws Exception {}

        @Override
        public void input() {}

        @Override
        public void update() {
            for (var entity : getEcs().getEntityManager().getEntitiesBySignature(getSignature())) {
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
        ArrayList<Class<? extends Component>> componentTypes = new ArrayList<>();
        componentTypes.add(Position.class);
        componentTypes.add(Transform.class);
        componentTypes.add(Health.class);
        componentTypes.add(Velocity.class);
        componentTypes.add(Attack.class);

        ecs = new Ecs(componentTypes);
    }

    @Test
    void getAllComponentTypes() {
        var componentTypes = ecs.getAllComponentTypes();
        assertEquals(5, componentTypes.size());
    }

    @Test
    void getEntityManager() {
        var em = ecs.getEntityManager();
        assertNotNull(em);
        assertEquals(EntityManager.class, ecs.getEntityManager().getClass());
    }

    @Test
    void getSystems() {
        assertEquals(0, ecs.getSystems().size());

        var moveSystem = new MoveSystem(ecs, 0, Transform.class, Health.class);
        ecs.addSystem(moveSystem);
        assertEquals(1, ecs.getSystems().size());

        // todo get system
        assertTrue(MoveSystem.class.isAssignableFrom(ecs.getSystems().get(0).getClass()));
    }

    @Test
    void init() {
    }

    @Test
    void input() {
    }

    @Test
    void update() {
        //ecs.update();
    }

    @Test
    void render() {
    }

    @Test
    void cleanUp() {
    }

    @Test
    void addSystem() {
        var moveSystem = new MoveSystem(ecs, 0, Transform.class, Health.class);
        ecs.addSystem(moveSystem);
        assertEquals(1, ecs.getSystems().size());
        var signatureBitset = moveSystem.getSignature().getSignatureBitSet();

        assertTrue(signatureBitset.get(1));
        assertTrue(signatureBitset.get(2));
    }

    @Test
    void getComponentIndex() {
        var index2 = ecs.getComponentIndex(Health.class);
        var index4 = ecs.getComponentIndex(Attack.class);
        assertEquals(2, index2);
        assertEquals(4, index4);
    }
}
