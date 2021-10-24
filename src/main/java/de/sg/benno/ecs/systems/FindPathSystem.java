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

import de.sg.benno.BennoRuntimeException;
import de.sg.benno.ai.Astar;
import de.sg.benno.ai.Node;
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.content.Water;
import de.sg.benno.debug.MousePicker;
import de.sg.benno.ecs.components.PositionComponent;
import de.sg.benno.ecs.components.SelectedComponent;
import de.sg.benno.ecs.components.TargetComponent;
import de.sg.benno.ecs.core.EntitySystem;
import de.sg.benno.ecs.core.Signature;
import de.sg.benno.input.Camera;
import de.sg.benno.ogl.renderer.SpriteRenderer;
import de.sg.benno.ogl.resource.Texture;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents a FindPathSystem.
 * This system iterates over all entities with the {@link PositionComponent} and the {@link SelectedComponent}.
 * The {@link TargetComponent} is added to the entity with the right mouse button.
 * If the {@link TargetComponent} exists, the path to the target is highlighted in color.
 */
public class FindPathSystem extends EntitySystem {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link Context} object.
     */
    private final Context context;

    /**
     * The {@link Water} object.
     */
    private final Water water;

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

    /**
     * Texture that highlights the path to the target.
     */
    private final Texture highlightTexture;

    /**
     * A {@link SpriteRenderer} object.
     */
    private final SpriteRenderer spriteRenderer;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link FindPathSystem} object.
     *
     * @param context The {@link Context} object.
     * @param water The {@link Water} object.
     * @param camera The {@link Camera} object.
     * @param currentZoom The current {@link Zoom}.
     * @param signature A {@link Signature} object.
     * @throws Exception If an error is thrown.
     */
    public FindPathSystem(Context context, Water water, Camera camera, Zoom currentZoom, Signature signature) throws Exception {
        super(Objects.requireNonNull(signature, "signature must not be null"));

        LOGGER.debug("Creates FindPathSystem object.");

        this.context = Objects.requireNonNull(context, "context must not be null");
        this.water = Objects.requireNonNull(water, "water must not be null");
        this.camera = Objects.requireNonNull(camera, "camera must not be null");
        this.currentZoom = Objects.requireNonNull(currentZoom, "currentZoom must not be null");
        this.mousePicker = new MousePicker(context, water, TileGraphic.TileHeight.SEA_LEVEL);
        this.highlightTexture = context.engine.getResourceManager().getTextureResource("/debug/full.png");
        this.spriteRenderer = new SpriteRenderer(context.engine);
    }

    /**
     * Constructs a new {@link FindPathSystem} object.
     *
     * @param context The {@link Context} object.
     * @param water The {@link Water} object.
     * @param camera The {@link Camera} object.
     * @param currentZoom The current {@link Zoom}.
     * @throws Exception If an error is thrown.
     */
    public FindPathSystem(Context context, Water water, Camera camera, Zoom currentZoom) throws Exception {
        this(context, water, camera, currentZoom, new Signature());
        getSignature().setAll(PositionComponent.class, SelectedComponent.class);
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
            if (!mouseInput.isRightButtonPressed()) {
                inputWasDone = false;
            }

            // button press event
            if (mouseInput.isRightButtonPressed()  && !inputWasDone) {
                inputWasDone = true;

                for (var entity : getEntities()) {
                    var shipPositionOptional = entity.getComponent(PositionComponent.class);
                    if (shipPositionOptional.isPresent()) {
                        // get ship position in world space
                        var shipPosition = shipPositionOptional.get().worldPosition;

                        // get target position in world space
                        var targetPosition = mousePicker.getTileUnderMouse(camera, currentZoom);

                        // dummy obstacles
                        var data = new Byte[500 * 350];
                        Arrays.fill(data, (byte) 0);
                        var obst = new ArrayList<>(Arrays.asList(data));

                        // get path to target
                        var path = Astar.findPathToTarget(
                                new Node(shipPosition).position,
                                targetPosition,
                                obst
                        );

                        // change or add target component
                        Optional<TargetComponent> targetComponentOptional;
                        targetComponentOptional = entity.getComponent(TargetComponent.class);
                        if (targetComponentOptional.isPresent()) {
                            var targetComponent = targetComponentOptional.get();
                            targetComponent.targetWorldPosition = targetPosition;
                            targetComponent.path = path;

                            LOGGER.debug("Change Ship target to x: {}, y: {}.", targetComponent.targetWorldPosition.x, targetComponent.targetWorldPosition.y);
                        } else {
                            try {
                                targetComponentOptional = entity.addComponent(TargetComponent.class);
                                if (targetComponentOptional.isPresent()) {
                                    var targetComponent = targetComponentOptional.get();
                                    targetComponent.targetWorldPosition = targetPosition;
                                    targetComponent.path = path;

                                    LOGGER.debug("Add new Ship target x: {}, y: {}.", targetComponent.targetWorldPosition.x, targetComponent.targetWorldPosition.y);
                                } else {
                                    throw new BennoRuntimeException("Error while adding target component.");
                                }
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
    public void update() {

    }

    @Override
    public void render() {
        for (var entity : getEntities()) {
            var targetComponentOptional = entity.getComponent(TargetComponent.class);
            if (targetComponentOptional.isPresent()) {
                var targetComponent = targetComponentOptional.get();
                for (var node : targetComponent.path) {
                    var tileGraphicOptional = water.getWaterTileGraphic(currentZoom, node.position.x, node.position.y);
                    tileGraphicOptional.ifPresent(tileGraphic -> spriteRenderer.render(camera.getViewMatrix(), highlightTexture, tileGraphic.getModelMatrix()));
                }
            }
        }
    }

    @Override
    public void cleanUp() {
        LOGGER.debug("Start clean up for the FindPathSystem.");

        spriteRenderer.cleanUp();
    }
}
