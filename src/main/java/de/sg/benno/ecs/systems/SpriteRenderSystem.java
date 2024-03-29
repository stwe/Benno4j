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

import de.sg.benno.ecs.components.GfxIndexComponent;
import de.sg.benno.ecs.components.PositionComponent;
import de.sg.benno.ecs.core.EntitySystem;
import de.sg.benno.ecs.core.Signature;
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
 * This system iterates over all entities with the {@link GfxIndexComponent} and the {@link PositionComponent}.
 * Renders the entities.
 */
public class SpriteRenderSystem extends EntitySystem {

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
     * The current {@link Zoom}.
     */
    private Zoom currentZoom;

    /**
     * For convenience.
     */
    private final HashMap<Zoom, BshFile> shipBshFiles = new HashMap<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link SpriteRenderSystem} object.
     *
     * @param context The {@link Context} object.
     * @param camera The {@link Camera} object.
     * @param currentZoom The current {@link Zoom}.
     * @param signature A {@link Signature} object.
     * @throws IOException If an I/O error is thrown.
     */
    public SpriteRenderSystem(Context context, Camera camera, Zoom currentZoom, Signature signature) throws IOException {
        super(signature);

        LOGGER.debug("Creates SpriteRenderSystem object.");

        Objects.requireNonNull(context, "context must not be null");

        this.camera = Objects.requireNonNull(camera, "camera must not be null");
        this.spriteRenderer = new SpriteRenderer(context.engine);
        this.currentZoom = Objects.requireNonNull(currentZoom, "currentZoom must not be null");

        for (var zoom : Zoom.values()) {
            this.shipBshFiles.put(zoom, context.bennoFiles.getShipBshFile(zoom));
        }
    }

    /**
     * Constructs a new {@link SpriteRenderSystem} object.
     *
     * @param context The {@link Context} object.
     * @param camera The {@link Camera} object.
     * @param currentZoom The current {@link Zoom}.
     * @throws IOException If an I/O error is thrown.
     */
    public SpriteRenderSystem(Context context, Camera camera, Zoom currentZoom) throws IOException {
        this(context, camera, currentZoom, new Signature());
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

    }

    @Override
    public void update() {

    }

    @Override
    public void render() {
        for (var entity : getEntities()) {
            var gfxIndexOptional = entity.getComponent(GfxIndexComponent.class);
            var positionOptional = entity.getComponent(PositionComponent.class);
            if (gfxIndexOptional.isPresent() && positionOptional.isPresent()) {
                var gfxIndexComponent = gfxIndexOptional.get();
                var positionComponent = positionOptional.get();

                var gfxIndex = gfxIndexComponent.gfxIndex;

                var screenPosition = positionComponent.screenPositions.get(currentZoom);
                var size = positionComponent.sizes.get(currentZoom);
                var modelMatrix = RenderUtil.createModelMatrix(screenPosition, size);

                var bshTexture = shipBshFiles.get(currentZoom).getBshTextures().get(gfxIndex);
                spriteRenderer.render(camera.getViewMatrix(), bshTexture.getTexture(), modelMatrix);
            }
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
