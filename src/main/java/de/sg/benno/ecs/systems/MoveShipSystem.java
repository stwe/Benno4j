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

                var currentWorldPosition = positionComponentOptional.get().worldPosition;
                var currentShipScreenPosition = positionComponentOptional.get().screenPositions.get(currentZoom);
                var targetComponent = targetComponentOptional.get();
                var ship = ship4ComponentOptional.get().ship4;

                if (!targetComponent.path.isEmpty() && targetComponent.currentNodeIndex < targetComponent.path.size()) {
                    // current tile/node
                    var currentWaypoint = targetComponent.path.get(targetComponent.currentNodeIndex - 1);
                    var currentTileScreenPosition = water.getWaterTileGraphic(currentZoom, currentWaypoint.position.x, currentWaypoint.position.y).get().screenPosition;

                    // next node/tile
                    var nextWaypoint = targetComponent.path.get(targetComponent.currentNodeIndex);
                    var nextTileScreenPosition = water.getWaterTileGraphic(currentZoom, nextWaypoint.position.x, nextWaypoint.position.y).get().screenPosition;

                    // get direction to the next waypoint
                    var d = new Vector2f(nextTileScreenPosition).sub(new Vector2f(currentTileScreenPosition));
                    d.normalize();

                    // add direction to ship screen position
                    currentShipScreenPosition.add(d);
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
