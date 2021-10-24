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
import de.sg.benno.ecs.components.GfxIndexComponent;
import de.sg.benno.ecs.components.PositionComponent;
import de.sg.benno.ecs.components.SelectedComponent;
import de.sg.benno.ecs.components.TargetComponent;
import de.sg.benno.ecs.core.EntitySystem;
import de.sg.benno.ecs.core.Signature;
import de.sg.benno.input.Camera;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;

import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents a SelectShipSystem.
 * This system iterates over all entities with the {@link GfxIndexComponent} and the {@link PositionComponent}.
 * With the left mouse button the {@link SelectedComponent} is added to the entity if there is a ship at the position.
 * Another click removes the {@link SelectedComponent} and the possibly existing {@link TargetComponent}.
 */
public class SelectShipSystem extends EntitySystem {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link Context} object.
     */
    private final Context context;

    /**
     * The {@link Camera} object.
     */
    private final Camera camera;

    /**
     * The current {@link Zoom}.
     */
    private Zoom currentZoom;

    /**
     * A {@link MousePicker} object.
     */
    private final MousePicker mousePicker;

    /**
     * Prevent multiple handling of the same event.
     */
    private boolean inputWasDone = false;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link SelectShipSystem} object.
     *
     * @param context The {@link Context} object.
     * @param water The {@link Water} object.
     * @param camera The {@link Camera} object.
     * @param currentZoom The current {@link Zoom}.
     * @param signature A {@link Signature} object.
     * @throws Exception If an error is thrown.
     */
    public SelectShipSystem(Context context, Water water, Camera camera, Zoom currentZoom, Signature signature) throws Exception {
        super(signature);

        LOGGER.debug("Creates SelectShipSystem object.");

        this.context = Objects.requireNonNull(context, "context must not be null");
        this.camera = Objects.requireNonNull(camera, "camera must not be null");
        this.currentZoom = Objects.requireNonNull(currentZoom, "currentZoom must not be null");
        this.mousePicker = new MousePicker(context, water, TileGraphic.TileHeight.SEA_LEVEL);
    }

    /**
     * Constructs a new {@link SelectShipSystem} object.
     *
     * @param context The {@link Context} object.
     * @param water The {@link Water} object.
     * @param camera The {@link Camera} object.
     * @param currentZoom The current {@link Zoom}.
     * @throws Exception If an error is thrown.
     */
    public SelectShipSystem(Context context, Water water, Camera camera, Zoom currentZoom) throws Exception {
        this(context, water, camera, currentZoom, new Signature());
        getSignature().setAll(GfxIndexComponent.class, PositionComponent.class);
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
        var mouseInput = context.engine.getMouseInput();
        if (mouseInput.isInWindow()) {
            // button release event
            if (!mouseInput.isLeftButtonPressed()) {
                inputWasDone = false;
            }

            // button press event
            if (mouseInput.isLeftButtonPressed() && !inputWasDone) {
                inputWasDone = true;

                for (var entity : getEntities()) {
                    var shipPositionOptional = entity.getComponent(PositionComponent.class);
                    if (shipPositionOptional.isPresent()) {
                        // get ship position in world space
                        var shipPosition = shipPositionOptional.get().worldPosition;

                        // get world position of the tile under mouse
                        var selectedPosition = mousePicker.getTileUnderMouse(camera, currentZoom);

                        // compare both positions
                        if (shipPosition.equals(selectedPosition)) {
                            if (entity.hasComponent(SelectedComponent.class)) {
                                // deselect
                                entity.removeComponent(SelectedComponent.class);
                                if (entity.hasComponent(TargetComponent.class)) {
                                    entity.removeComponent(TargetComponent.class);
                                }
                                LOGGER.debug("Ship {} is now deselected.", entity.debugName);
                            } else {
                                // select
                                try {
                                    entity.addComponent(SelectedComponent.class);
                                    LOGGER.debug("Ship {} is now selected.", entity.debugName);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void update() {

    }

    @Override
    public void render() {
        mousePicker.render(camera, currentZoom);
    }

    @Override
    public void cleanUp() {

    }
}
