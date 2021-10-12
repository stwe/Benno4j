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

import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.content.Water;
import de.sg.benno.debug.MousePicker;
import de.sg.benno.ecs.components.PositionComponent;
import de.sg.benno.ecs.components.SelectedComponent;
import de.sg.benno.ecs.core.Component;
import de.sg.benno.ecs.core.Ecs;
import de.sg.benno.ecs.core.EntitySystem;
import de.sg.benno.input.Camera;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;

import static de.sg.benno.ogl.Log.LOGGER;

public class SelectShipSystem extends EntitySystem {

    private final Context context;
    private final Camera camera;
    private Zoom currentZoom;
    private final MousePicker mousePicker;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    @SafeVarargs
    public SelectShipSystem(
            Context context, Water water, Camera camera, Zoom currentZoom,
            Ecs ecs, int priority, Class<? extends Component>... signatureComponentTypes) throws Exception {
        super(ecs, priority, signatureComponentTypes);

        this.context = context;
        this.camera = camera;
        this.currentZoom = currentZoom;
        this.mousePicker = new MousePicker(context, water, TileGraphic.TileHeight.SEA_LEVEL);
    }

    //-------------------------------------------------
    // Setter
    //-------------------------------------------------

    /**
     * Set {@link #currentZoom}.
     *
     * @param currentZoom A {@link Zoom}.
     */
    public void setCurrentZoom(Zoom currentZoom) {
        this.currentZoom = currentZoom;
    }

    //-------------------------------------------------
    // Implement System
    //-------------------------------------------------

    @Override
    public void input() {

    }

    @Override
    public void update() {
        var mouseInput = context.engine.getMouseInput();
        if (mouseInput.isInWindow()) {
            if (mouseInput.isLeftButtonPressed()) {
                for (var entity : getEntities()) {
                    var shipPositionOptional = entity.getComponent(PositionComponent.class);
                    if (shipPositionOptional.isPresent()) {
                        // get ship position in world space
                        var shipPosition = shipPositionOptional.get().worldPosition;

                        // get world position of the tile under mouse
                        var selectedPosition = mousePicker.getTileUnderMouse(camera, currentZoom);

                        // compare
                        if (shipPosition.equals(selectedPosition)) {
                            LOGGER.debug("ship selected");

                            // todo
                            try {
                                if (entity.hasComponent(SelectedComponent.class)) {
                                    entity.removeComponent(SelectedComponent.class);
                                    LOGGER.debug("Ship is unselected");
                                    return;
                                }

                                entity.addComponent(SelectedComponent.class);
                                LOGGER.debug("Ship is selected");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void render() {
        mousePicker.render(camera, currentZoom);
    }

    @Override
    public void cleanUp() {

    }
}
