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

import de.sg.benno.ai.Astar;
import de.sg.benno.ai.Node;
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.content.Water;
import de.sg.benno.debug.MousePicker;
import de.sg.benno.ecs.components.PositionComponent;
import de.sg.benno.ecs.components.TargetComponent;
import de.sg.benno.ecs.core.EntitySystem;
import de.sg.benno.ecs.core.Signature;
import de.sg.benno.input.Camera;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static de.sg.benno.ogl.Log.LOGGER;

public class FindPathSystem extends EntitySystem {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    private final Context context;
    private final Camera camera;
    private Zoom currentZoom;
    private final MousePicker mousePicker;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public FindPathSystem(Context context, Water water, Camera camera, Zoom currentZoom, Signature signature) throws Exception {
        super(signature);

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
            if (mouseInput.isRightButtonPressed()) {
                for (var entity : getEntities()) {
                    LOGGER.debug("ship in path system");

                    var target = mousePicker.getTileUnderMouse(camera, currentZoom);
                    var positionOptional = entity.getComponent(PositionComponent.class);
                    var data = new Byte[500*350];
                    Arrays.fill(data, Byte.valueOf((byte)0));
                    var obst = new ArrayList<Byte>(Arrays.asList(data));

                    var path = Astar.findPathToTarget(
                            new Node(positionOptional.get().worldPosition).position,
                            target,
                            obst
                    );

                    Optional<TargetComponent> targetComponent = null;
                    try {
                        targetComponent = entity.addComponent(TargetComponent.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    targetComponent.get().path = path;
                    targetComponent.get().target = target;
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
