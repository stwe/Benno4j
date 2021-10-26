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

import de.sg.benno.content.Water;
import de.sg.benno.ecs.components.*;
import de.sg.benno.ecs.core.EntitySystem;
import de.sg.benno.ecs.core.Signature;
import de.sg.benno.renderer.Zoom;
import org.joml.Vector2f;

import static de.sg.benno.chunk.Ship4.getShipDirection;
import static de.sg.benno.chunk.Ship4.getTargetDirectionVector;
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
                PositionComponent.class,
                SelectedComponent.class,
                GfxIndexComponent.class,
                Ship4Component.class,
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
            var positionComponentOptional = entity.getComponent(PositionComponent.class);
            var targetComponentOptional = entity.getComponent(TargetComponent.class);
            var ship4ComponentOptional = entity.getComponent(Ship4Component.class);
            var gfxIndexComponentOptional = entity.getComponent(GfxIndexComponent.class);

            if (positionComponentOptional.isPresent() && targetComponentOptional.isPresent() &&
                    ship4ComponentOptional.isPresent() && gfxIndexComponentOptional.isPresent()) {

                var currentShipScreenPosition = positionComponentOptional.get().screenPositions.get(currentZoom);
                var targetComponent = targetComponentOptional.get();
                var ship = ship4ComponentOptional.get().ship4;

                if (!targetComponent.path.isEmpty() && targetComponent.nodeIndex < targetComponent.path.size()) {
                    // current tile
                    var currentWorldSpacePosition = targetComponent.path.get(targetComponent.nodeIndex - 1).position;
                    var currentScreenSpacePosition = water.getWaterTileGraphic(currentZoom, currentWorldSpacePosition.x, currentWorldSpacePosition.y).get().screenPosition;

                    // next tile
                    var nextWorldSpacePosition = targetComponent.path.get(targetComponent.nodeIndex).position;
                    var nextScreenSpacePosition = water.getWaterTileGraphic(currentZoom, nextWorldSpacePosition.x, nextWorldSpacePosition.y).get().screenPosition;

                    // get direction to the next tile in screen space
                    var d = new Vector2f(nextScreenSpacePosition).sub(new Vector2f(currentScreenSpacePosition));
                    d.normalize();

                    // get direction and angle to the next tile in world space
                    var targetDirection = getTargetDirectionVector(nextWorldSpacePosition, currentWorldSpacePosition);

                    // set new ship direction by angle for calculation the right gfx index
                    ship.direction = getShipDirection(targetDirection.z);

                    // set the new gfx index
                    gfxIndexComponentOptional.get().gfxIndex = ship.getCurrentGfxIndex();

                    // add direction to ship screen space position (move)
                    currentShipScreenPosition.add(d);

                    // next waypoint
                    var v = new Vector2f(targetComponent.waypoints.get(currentZoom).get(targetComponent.nodeIndex)).sub(currentShipScreenPosition);
                    var l = v.length();
                    if (l < 1) {
                        targetComponent.nodeIndex++;
                    }
                } else {
                    // update ship object
                    ship.xPos = targetComponent.targetWorldPosition.x;
                    ship.yPos = targetComponent.targetWorldPosition.y;

                    // update position component
                    positionComponentOptional.get().worldPosition.x = ship.xPos;
                    positionComponentOptional.get().worldPosition.y = ship.yPos;
                    positionComponentOptional.get().screenPositions.put(Zoom.GFX, targetComponent.waypoints.get(Zoom.GFX).get(targetComponent.path.size()-1));
                    positionComponentOptional.get().screenPositions.put(Zoom.MGFX, targetComponent.waypoints.get(Zoom.MGFX).get(targetComponent.path.size()-1));
                    positionComponentOptional.get().screenPositions.put(Zoom.SGFX, targetComponent.waypoints.get(Zoom.SGFX).get(targetComponent.path.size()-1));

                    // remove target component
                    if (entity.hasComponent(TargetComponent.class)) {
                        entity.removeComponent(TargetComponent.class);
                    }
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
