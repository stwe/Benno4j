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
import de.sg.benno.ecs.components.ZoomComponent;
import de.sg.benno.file.BshFile;
import de.sg.benno.input.Camera;
import de.sg.benno.ogl.renderer.RenderUtil;
import de.sg.benno.ogl.renderer.SpriteRenderer;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents a SpriteRenderSystem.
 */
public class SpriteRenderSystem extends IteratingSystem {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link Camera} object to get the view matrix.
     */
    private final Camera camera;

    /**
     * A {@link SpriteRenderer} object.
     */
    private final SpriteRenderer spriteRenderer;

    /**
     * Only entities with this zoom are rendered.
     */
    private Zoom currentZoom = Zoom.GFX;

    /**
     * For convenience.
     */
    private final HashMap<Zoom, BshFile> shipBshFiles = new HashMap<>();

    /**
     * Retrieving {@link PositionComponent}.
     */
    private final ComponentMapper<PositionComponent> positionComponentMapper = ComponentMapper.getFor(PositionComponent.class);

    /**
     * Retrieving {@link GfxIndexComponent}.
     */
    private final ComponentMapper<GfxIndexComponent> gfxIndexComponentMapper = ComponentMapper.getFor(GfxIndexComponent.class);

    /**
     * Retrieving {@link ZoomComponent}.
     */
    private final ComponentMapper<ZoomComponent> zoomComponentMapper = ComponentMapper.getFor(ZoomComponent.class);

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link SpriteRenderSystem} object.
     *
     * @param context The {@link Context} object.
     * @param camera The {@link Camera} object.
     * @throws IOException If an I/O error is thrown.
     */
    public SpriteRenderSystem(Context context, Camera camera) throws IOException {
        super(Family.all(PositionComponent.class, GfxIndexComponent.class, ZoomComponent.class).get());

        LOGGER.debug("Creates SpriteRenderSystem object.");

        Objects.requireNonNull(context, "context must not be null");

        this.camera = Objects.requireNonNull(camera, "camera must not be null");
        this.spriteRenderer = new SpriteRenderer(context.engine);

        for (var zoom : Zoom.values()) {
            this.shipBshFiles.put(zoom, context.bennoFiles.getShipBshFile(zoom));
        }
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
    // Implement IteratingSystem
    //-------------------------------------------------

    @Override
    protected void processEntity(Entity entity, float dt) {
        if (zoomComponentMapper.get(entity).zoom == currentZoom) {
            var gfxIndex = gfxIndexComponentMapper.get(entity).gfxIndex;

            var screenPosition = positionComponentMapper.get(entity).screenPosition;
            var size = positionComponentMapper.get(entity).size;
            var modelMatrix = RenderUtil.createModelMatrix(screenPosition, size);

            var bshTexture = shipBshFiles.get(currentZoom).getBshTextures().get(gfxIndex);
            spriteRenderer.render(camera.getViewMatrix(), bshTexture.getTexture(), modelMatrix);
        }
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the SpriteRenderSystem.");

        spriteRenderer.cleanUp();
    }
}
