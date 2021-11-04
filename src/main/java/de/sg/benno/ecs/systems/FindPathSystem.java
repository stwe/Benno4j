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
import de.sg.benno.chunk.Ship4;
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.content.World;
import de.sg.benno.debug.MousePicker;
import de.sg.benno.ecs.components.*;
import de.sg.benno.ecs.core.Entity;
import de.sg.benno.ecs.core.EntitySystem;
import de.sg.benno.ecs.core.Signature;
import de.sg.benno.input.Camera;
import de.sg.benno.ogl.renderer.SpriteRenderer;
import de.sg.benno.ogl.resource.Texture;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.*;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents a FindPathSystem.
 * The {@link TargetComponent} is added to the entity with the right mouse button.
 * If the {@link TargetComponent} exists, the path to the target is highlighted in color.
 * The {@link GfxIndexComponent} is changed when the target changes.
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
     * The {@link World} object.
     */
    private final World world;

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
     * @param world {@link World} object
     * @param camera The {@link Camera} object.
     * @param currentZoom The current {@link Zoom}.
     * @param signature A {@link Signature} object.
     * @throws Exception If an error is thrown.
     */
    public FindPathSystem(
            Context context,
            World world,
            Camera camera,
            Zoom currentZoom,
            Signature signature
    ) throws Exception {
        super(Objects.requireNonNull(signature, "signature must not be null"));

        LOGGER.debug("Creates FindPathSystem object.");

        this.context = Objects.requireNonNull(context, "context must not be null");
        this.world = Objects.requireNonNull(world, "world must not be null");
        this.camera = Objects.requireNonNull(camera, "camera must not be null");
        this.currentZoom = Objects.requireNonNull(currentZoom, "currentZoom must not be null");
        this.mousePicker = new MousePicker(context, world.getWater(), TileGraphic.TileHeight.SEA_LEVEL);
        this.highlightTexture = context.engine.getResourceManager().getTextureResource("/debug/full.png");
        this.spriteRenderer = new SpriteRenderer(context.engine);
    }

    /**
     * Constructs a new {@link FindPathSystem} object.
     *
     * @param context The {@link Context} object.
     * @param world {@link World} object
     * @param camera The {@link Camera} object.
     * @param currentZoom The current {@link Zoom}.
     * @throws Exception If an error is thrown.
     */
    public FindPathSystem(Context context, World world, Camera camera, Zoom currentZoom) throws Exception {
        this(context, world, camera, currentZoom, new Signature());
        getSignature().setAll(GfxIndexComponent.class, PositionComponent.class, Ship4Component.class, SelectedComponent.class);
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
                    var gfxIndexComponentOptional = entity.getComponent(GfxIndexComponent.class);
                    var shipPositionOptional = entity.getComponent(PositionComponent.class);
                    if (gfxIndexComponentOptional.isPresent() && shipPositionOptional.isPresent()) {
                        // get ship position in world space
                        var shipPosition = shipPositionOptional.get().worldPosition;

                        // get target position in world space
                        var targetPosition = mousePicker.getTileUnderMouse(camera, currentZoom);
                        if (world.getTerrain().isNotPassableByShip(targetPosition.x, targetPosition.y)) {
                            LOGGER.debug("No valid target for ship {}.", entity.debugName);
                            return;
                        }

                        // get path to target
                        var path = Astar.findPathToTarget(
                                new Node(shipPosition).position,
                                targetPosition,
                                world.getTerrain().getShipPassableArea()
                        );

                        // create waypoints
                        var gfxIndex = gfxIndexComponentOptional.get().gfxIndex;
                        var waypointMap = new HashMap<Zoom, ArrayList<Vector2f>>();
                        for (var zoom : Zoom.values()) {
                            var waypoints = new ArrayList<Vector2f>();

                            for (var node : path) {
                                try {
                                    var waypoint = Ship4.createWaypoint(node.position.x, node.position.y, context, gfxIndex, zoom);
                                    waypoints.add(new Vector2f(waypoint.x, waypoint.y));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            waypointMap.put(zoom, waypoints);
                        }

                        // change or add target component
                        Optional<TargetComponent> targetComponentOptional;
                        targetComponentOptional = entity.getComponent(TargetComponent.class);
                        if (targetComponentOptional.isPresent()) {
                            var targetComponent = targetComponentOptional.get();
                            targetComponent.targetWorldPosition = targetPosition;
                            targetComponent.path = path;
                            targetComponent.waypoints = waypointMap;

                            updateDirection(entity);

                            LOGGER.debug("Change Ship target to x: {}, y: {}.", targetComponent.targetWorldPosition.x, targetComponent.targetWorldPosition.y);
                        } else {
                            try {
                                targetComponentOptional = entity.addComponent(TargetComponent.class);
                                if (targetComponentOptional.isPresent()) {
                                    var targetComponent = targetComponentOptional.get();
                                    targetComponent.targetWorldPosition = targetPosition;
                                    targetComponent.path = path;
                                    targetComponent.waypoints = waypointMap;

                                    updateDirection(entity);

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
                    var tileGraphicOptional = world.getTileGraphic(currentZoom, node.position.x, node.position.y);
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

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Changes the ship's gfx index depending on the direction to the target.
     *
     * @param entity An {@link Entity} object.
     */
    private void updateDirection(Entity entity) {
        var targetComponentOptional = entity.getComponent(TargetComponent.class);
        var ship4ComponentOptional = entity.getComponent(Ship4Component.class);
        var gfxIndexComponentOptional = entity.getComponent(GfxIndexComponent.class);

        if (targetComponentOptional.isPresent() && ship4ComponentOptional.isPresent() && gfxIndexComponentOptional.isPresent()) {
            var target = targetComponentOptional.get().targetWorldPosition;
            var ship = ship4ComponentOptional.get().ship4;

            // get direction and angle to the target
            var targetDirection = Ship4.getTargetDirectionVector(ship.getPosition(), target);

            // set new ship direction by angle for calculation the right gfx index
            ship.direction = Ship4.getShipDirection(targetDirection.z);

            // set the new gfx index
            gfxIndexComponentOptional.get().gfxIndex = ship.getCurrentGfxIndex();
        }
    }
}
