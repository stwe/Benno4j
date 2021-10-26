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
import de.sg.benno.ecs.components.*;
import de.sg.benno.ecs.core.Entity;
import de.sg.benno.ecs.core.EntitySystem;
import de.sg.benno.ecs.core.Signature;
import de.sg.benno.input.Camera;
import de.sg.benno.ogl.renderer.SpriteRenderer;
import de.sg.benno.ogl.resource.Texture;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import de.sg.benno.util.TileUtil;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.*;

import static de.sg.benno.chunk.Ship4.getShipDirection;
import static de.sg.benno.chunk.Ship4.getTargetDirectionVector;
import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents a FindPathSystem.
 * This system iterates over all entities with the {@link PositionComponent}, the {@link SelectedComponent},
 * the {@link GfxIndexComponent} and {@link Ship4Component}.
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
        getSignature().setAll(PositionComponent.class, SelectedComponent.class, GfxIndexComponent.class, Ship4Component.class);
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

                        // create waypoints
                        var gfxIndex = gfxIndexComponentOptional.get().gfxIndex;
                        var waypointMap = new HashMap<Zoom, ArrayList<Vector2f>>();
                        for (var zoom : Zoom.values()) {
                            var waypoints = new ArrayList<Vector2f>();

                            for (var node : path) {
                                try {
                                    waypoints.add(createWaypoint(node.position.x, node.position.y, gfxIndex, zoom));
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

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Changes the ship's gfx index depending on the direction.
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
            var targetDirection = getTargetDirectionVector(target, ship.getPosition());

            // set new ship direction by angle for calculation the right gfx index
            ship.direction = getShipDirection(targetDirection.z);

            // set the new gfx index
            gfxIndexComponentOptional.get().gfxIndex = ship.getCurrentGfxIndex();
        }
    }

    /**
     * Calculates the screen coordinates of a ship for a tile at one position in the world.
     *
     * @param x The x position of a ship in world space.
     * @param y The y position of a ship in world space.
     * @param gfxIndex The current gfx index of a ship to get the texture width and height.
     * @param zoom A {@link Zoom}.
     * @return The waypoint in screen space.
     * @throws IOException If an I/O error is thrown.
     */
    private Vector2f createWaypoint(int x, int y, int gfxIndex, Zoom zoom) throws IOException {
        var xWorldPos = x + 1; // correction for rendering
        var yWorldPos = y - 1; // correction for rendering

        var shipBshFile = context.bennoFiles.getShipBshFile(zoom);
        var shipBshTexture = shipBshFile.getBshTextures().get(gfxIndex);

        var screenPosition = TileUtil.worldToScreen(xWorldPos, yWorldPos, zoom.getTileWidthHalf(), zoom.getTileHeightHalf());
        var adjustHeight = TileUtil.adjustHeight(zoom.getTileHeightHalf(), TileGraphic.TileHeight.SEA_LEVEL.value, zoom.getElevation());
        screenPosition.y += adjustHeight;
        screenPosition.x -= shipBshTexture.getWidth();
        screenPosition.y -= shipBshTexture.getHeight();
        screenPosition.x -= zoom.getTileWidthHalf() * 0.5f;
        screenPosition.y -= zoom.getTileHeightHalf() * 0.5f;

        return new Vector2f(screenPosition);
    }
}
