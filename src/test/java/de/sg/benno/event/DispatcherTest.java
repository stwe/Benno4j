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

package de.sg.benno.event;

import de.sg.benno.ecs.core.Component;
import de.sg.benno.ecs.core.Entity;
import de.sg.benno.ecs.core.EntitySystem;
import de.sg.benno.ecs.core.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DispatcherTest {

    private static class MoveSystem extends EntitySystem implements Listener {

        public MoveSystem(Signature signature) {
            super(signature);
        }

        @Override
        public void input() {}

        @Override
        public void update() {
            for (var entity : getEntities()) {
                System.out.println("update MoveSystem");
            }
        }

        @Override
        public void render() {}

        @Override
        public void cleanUp() {}

        @Override
        public void handleEvent(Entity entity, Event event) {
            if (event instanceof CustomEvent) {
                System.out.println("handle custom event");
            }
        }
    }

    private static class CustomEvent extends Event {

    }

    private MoveSystem moveSystem;
    private Event customEvent;

    @BeforeEach
    void setUp() {
        moveSystem = new MoveSystem(new Signature());
        customEvent = new CustomEvent();
    }

    @Test
    void addListener() {
        Dispatcher.addListener(moveSystem);
        Dispatcher.notify(null, customEvent);
    }

    @Test
    void testNotify() {
    }
}
