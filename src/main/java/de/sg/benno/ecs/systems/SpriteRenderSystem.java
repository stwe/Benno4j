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

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import de.sg.benno.ecs.components.GfxIndexComponent;
import de.sg.benno.ecs.components.PositionComponent;
import de.sg.benno.file.BshFile;
import de.sg.benno.input.Camera;
import de.sg.benno.ogl.renderer.RenderUtil;
import de.sg.benno.ogl.renderer.SpriteRenderer;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;

import java.io.IOException;
import java.util.HashMap;

public class SpriteRenderSystem extends IteratingSystem {

    private Zoom zoom;
    private final Camera camera;

    private final SpriteRenderer spriteRenderer;

    private final HashMap<Zoom, BshFile> shipBshFiles = new HashMap<>();

    private ComponentMapper<PositionComponent> positionComponentMapper = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<GfxIndexComponent> gfxIndexComponentMapper = ComponentMapper.getFor(GfxIndexComponent.class);

    public SpriteRenderSystem(Context context, Zoom zoom, Camera camera) throws IOException {
        super(Family.all(PositionComponent.class, GfxIndexComponent.class).get());

        this.zoom = zoom;
        this.camera = camera;

        this.spriteRenderer = new SpriteRenderer(context.engine);

        for (var z : Zoom.values()) {
            this.shipBshFiles.put(z, context.bennoFiles.getShipBshFile(z));
        }
    }

    public void setZoom(Zoom zoom) {
        this.zoom = zoom;
    }

    @Override
    protected void processEntity(Entity entity, float dt) {
        var gfxIndex = gfxIndexComponentMapper.get(entity).gfxIndex;
        var screenPosition = positionComponentMapper.get(entity).screenPosition;
        var size = positionComponentMapper.get(entity).size;
        var modelMatrix = RenderUtil.createModelMatrix(screenPosition, size);

        var bshTexture = shipBshFiles.get(zoom).getBshTextures().get(gfxIndex);
        spriteRenderer.render(camera.getViewMatrix(), bshTexture.getTexture(), modelMatrix);
    }
}
