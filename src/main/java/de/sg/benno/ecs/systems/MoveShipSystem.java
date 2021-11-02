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
import de.sg.benno.chunk.Ship4;
import de.sg.benno.content.Water;
import de.sg.benno.ecs.components.*;
import de.sg.benno.ecs.core.EntitySystem;
import de.sg.benno.ecs.core.Signature;
import de.sg.benno.renderer.Zoom;
import org.joml.Vector2f;
import org.joml.Vector2i;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents a MoveShipSystem.
 */
public class MoveShipSystem extends EntitySystem {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link Water} object.
     */
    private final Water water;

    /**
     * The current {@link Zoom}.
     */
    private Zoom currentZoom;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link MoveShipSystem} object.
     */
    public MoveShipSystem(Water water, Zoom currentZoom) {
        super(new Signature());
        getSignature().setAll(
                GfxIndexComponent.class,
                PositionComponent.class,
                Ship4Component.class,
                SelectedComponent.class,
                TargetComponent.class
        );

        LOGGER.debug("Creates MoveShipSystem object.");

        this.water = water;
        this.currentZoom = currentZoom;
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
        for (var entity : getEntities()) {
            var gfxIndexComponentOptional = entity.getComponent(GfxIndexComponent.class);
            var positionComponentOptional = entity.getComponent(PositionComponent.class);
            var ship4ComponentOptional = entity.getComponent(Ship4Component.class);
            var targetComponentOptional = entity.getComponent(TargetComponent.class);

            if (gfxIndexComponentOptional.isPresent() && positionComponentOptional.isPresent() &&
                    ship4ComponentOptional.isPresent() && targetComponentOptional.isPresent()) {

                // get components
                var gfxIndexComponent = gfxIndexComponentOptional.get();
                var positionComponent = positionComponentOptional.get();
                var ship4Component = ship4ComponentOptional.get();
                var targetComponent = targetComponentOptional.get();

                if (!targetComponent.path.isEmpty() && targetComponent.nodeIndex < targetComponent.path.size()) {
                    // start: get current path tile positions (world && screen space)
                    var currentWorldSpacePosition = targetComponent.path.get(targetComponent.nodeIndex - 1).position;
                    Vector2f currentScreenSpacePosition;
                    var currentTileGraphicOptional = water.getWaterTileGraphic(currentZoom, currentWorldSpacePosition.x, currentWorldSpacePosition.y);
                    if (currentTileGraphicOptional.isPresent()) {
                        currentScreenSpacePosition = currentTileGraphicOptional.get().screenPosition;
                    } else {
                        throw new BennoRuntimeException("No water tile graphic found.");
                    }

                    // next target: get next path tile positions (world && screen space)
                    var nextWorldSpacePosition = targetComponent.path.get(targetComponent.nodeIndex).position;
                    Vector2f nextScreenSpacePosition;
                    var nextTileGraphicOptional = water.getWaterTileGraphic(currentZoom, nextWorldSpacePosition.x, nextWorldSpacePosition.y);
                    if (nextTileGraphicOptional.isPresent()) {
                        nextScreenSpacePosition = nextTileGraphicOptional.get().screenPosition;
                    } else {
                        throw new BennoRuntimeException("No water tile graphic found.");
                    }

                    // set a gfx toward the next target
                    updateShipDirection(ship4Component, currentWorldSpacePosition, nextWorldSpacePosition, gfxIndexComponent);

                    // get direction vector to the next target in screen space
                    var d = new Vector2f(nextScreenSpacePosition).sub(new Vector2f(currentScreenSpacePosition));
                    d.normalize();

                    // add direction vector to the *ship* screen space position (moves the ship)
                    var currentShipScreenPosition = positionComponent.screenPositions.get(currentZoom);
                    var velocityComponentOptional = entity.getComponent(VelocityComponent.class);
                    velocityComponentOptional.ifPresent(velocityComponent -> d.mul(velocityComponent.velocity));
                    currentShipScreenPosition.add(d);

                    // update components if next target reached
                    if (isTargetReached(currentShipScreenPosition, targetComponent)) {
                        targetReachedUpdate(ship4Component, positionComponent, targetComponent);
                        targetComponent.nodeIndex++;
                    }
                } else {
                    // the ship has reached its destination
                    targetReachedUpdate(ship4Component, positionComponent, targetComponent);
                    entity.removeComponent(TargetComponent.class);
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

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Depending on the destination, the gfx of the ship changes.
     *
     * @param ship4Component The {@link Ship4Component} to update.
     * @param currentPosition The current ship position on world space.
     * @param nextPosition The target ship position in world space.
     * @param gfxIndexComponent The {@link GfxIndexComponent} to update.
     */
    private void updateShipDirection(
            Ship4Component ship4Component,
            Vector2i currentPosition,
            Vector2i nextPosition,
            GfxIndexComponent gfxIndexComponent
    ) {
        // get direction and angle to the next tile in world space
        var targetDirection = Ship4.getTargetDirectionVector(currentPosition, nextPosition);

        // set new ship direction by angle for calculation the right gfx index
        ship4Component.ship4.direction = Ship4.getShipDirection(targetDirection.z);

        // set the new gfx index
        gfxIndexComponent.gfxIndex = ship4Component.ship4.getCurrentGfxIndex();
    }

    /**
     * Checks whether the ship has reached the next target/waypoint.
     *
     * @param currentShipPosition The current position of the ship in screen space.
     * @param targetComponent The {@link TargetComponent} object.
     *
     * @return boolean
     */
    private boolean isTargetReached(Vector2f currentShipPosition, TargetComponent targetComponent) {
        var v = new Vector2f(targetComponent.waypoints.get(currentZoom).get(targetComponent.nodeIndex)).sub(currentShipPosition);
        return v.length() < 1;
    }

    /**
     * Updates the world and screen position if a new target was reached.
     *
     * @param ship4Component The {@link Ship4Component} to update.
     * @param positionComponent The {@link PositionComponent} to update.
     * @param targetComponent The {@link TargetComponent} to update.
     */
    private void targetReachedUpdate(
            Ship4Component ship4Component,
            PositionComponent positionComponent,
            TargetComponent targetComponent
    ) {
        var index = targetComponent.nodeIndex;

        if (index == targetComponent.path.size()) {
            // set last target as position in world space
            ship4Component.ship4.xPos = targetComponent.targetWorldPosition.x;
            ship4Component.ship4.yPos = targetComponent.targetWorldPosition.y;
            index--;
        } else {
            // set current node as position in world space
            ship4Component.ship4.xPos = targetComponent.path.get(index).position.x;
            ship4Component.ship4.yPos = targetComponent.path.get(index).position.y;
        }

        // update the new world and screen positions in PositionComponent
        positionComponent.worldPosition.x = ship4Component.ship4.xPos;
        positionComponent.worldPosition.y = ship4Component.ship4.yPos;

        positionComponent.screenPositions.put(Zoom.GFX, targetComponent.waypoints.get(Zoom.GFX).get(index));
        positionComponent.screenPositions.put(Zoom.MGFX, targetComponent.waypoints.get(Zoom.MGFX).get(index));
        positionComponent.screenPositions.put(Zoom.SGFX, targetComponent.waypoints.get(Zoom.SGFX).get(index));
    }
}
