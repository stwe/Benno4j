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

package de.sg.benno.ecs.systems;

import de.sg.benno.ecs.components.PositionComponent;
import de.sg.benno.ecs.core.Component;
import de.sg.benno.ecs.core.Ecs;
import de.sg.benno.ecs.core.EntitySystem;
import de.sg.benno.ogl.physics.Aabb;
import de.sg.benno.state.Context;
import org.joml.Vector2f;

import static de.sg.benno.ogl.Log.LOGGER;

public class SelectShipSystem extends EntitySystem {

    private final Context context;

    @SafeVarargs
    public SelectShipSystem(Context context, Ecs ecs, int priority, Class<? extends Component>... signatureComponentTypes) {
        super(ecs, priority, signatureComponentTypes);

        this.context = context;
    }

    @Override
    public void input() {

    }

    @Override
    public void update() {
        var mouseInput = context.engine.getMouseInput();
        if (mouseInput.isInWindow()) {
            if (mouseInput.isLeftButtonPressed()) {
                // todo select ship
                for (var entity : getEntities()) {
                    var positionOptional = entity.getComponent(PositionComponent.class);
                    if (positionOptional.isPresent()) {
                        var positionComponent = positionOptional.get();
                        if (Aabb.pointVsAabb(new Vector2f((float)mouseInput.getCurrentPos().x, (float)mouseInput.getCurrentPos().y), new Aabb(positionComponent.screenPosition, positionComponent.size))) {
                            LOGGER.debug("treffer");
                        }
                    }

                    // hole positionComponent
                    // erstelle daraus ein Aabb
                    // prüfe, ob die Maus darin ist
                    // füge selectedComponent hinzu
                }
            }
        }
    }

    @Override
    public void render() {

    }

    @Override
    public void cleanUp() {

    }
}
