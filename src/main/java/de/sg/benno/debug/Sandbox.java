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

package de.sg.benno.debug;

import com.badlogic.ashley.core.PooledEngine;
import de.sg.benno.BennoConfig;
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.content.Water;
import de.sg.benno.chunk.Island5;
import de.sg.benno.chunk.WorldData;
import de.sg.benno.ecs.components.GfxIndexComponent;
import de.sg.benno.ecs.components.PositionComponent;
import de.sg.benno.ecs.systems.SpriteRenderSystem;
import de.sg.benno.input.Camera;
import de.sg.benno.renderer.Zoom;
import de.sg.benno.state.Context;
import de.sg.benno.util.TileUtil;
import org.joml.Vector2f;

import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.*;

/**
 * Represents a Sandbox.
 */
public class Sandbox {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * Provides data for drawing. For example the {@link Island5} objects from a loaded GAM file.
     */
    private final WorldData provider;

    /**
     * The {@link Context} object.
     */
    private final Context context;

    /**
     * The current {@link Zoom}.
     */
    private Zoom currentZoom = Zoom.GFX;

    /**
     * The {@link Camera} object.
     */
    private final Camera camera;

    /**
     * The {@link Water} object with the deep water area.
     */
    private final Water water;

    /**
     * A {@link PooledEngine} - an efficient ECS with pooling.
     */
    private final PooledEngine engine;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Sandbox} object.
     *
     * @param provider A {@link WorldData} object (e.g. {@link de.sg.benno.file.GamFile}).
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public Sandbox(WorldData provider, Context context) throws Exception {
        LOGGER.debug("Creates Sandbox object from provider class {}.", provider.getClass());

        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");

        if (BennoConfig.ZOOM_START >= 1 && BennoConfig.ZOOM_START <= 3) {
            currentZoom = Zoom.values()[BennoConfig.ZOOM_START - 1];
        }

        this.camera = new Camera(BennoConfig.CAMERA_START_X, BennoConfig.CAMERA_START_Y, context.engine, currentZoom);
        this.water = new Water(provider, context);
        this.engine = new PooledEngine();

        init();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #currentZoom}.
     *
     * @return {@link #currentZoom}
     */
    public Zoom getCurrentZoom() {
        return currentZoom;
    }

    /**
     * Get {@link #camera}.
     *
     * @return {@link #camera}
     */
    public Camera getCamera() {
        return camera;
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Initialize sandbox content.
     *
     * @throws Exception If an error is thrown.
     */
    private void init() throws Exception {
        // create entities
        for (var zoom : Zoom.values()) {
            for (var ship : provider.getShips4List()) {
                // new entity
                var entity = engine.createEntity();

                // add gfx index component
                var gfxIndex = ship.getCurrentGfxIndex();
                var gfxIndexComponent = engine.createComponent(GfxIndexComponent.class);
                gfxIndexComponent.gfxIndex = gfxIndex;
                entity.add(gfxIndexComponent);

                // add position component
                var xWorldPos = ship.xPos + 1;
                var yWorldPos = ship.yPos - 1;

                var positionComponent = engine.createComponent(PositionComponent.class);
                positionComponent.worldPosition.x = xWorldPos;
                positionComponent.worldPosition.y = yWorldPos;

                var shipBshFile = context.bennoFiles.getShipBshFile(zoom);
                var shipBshTexture = shipBshFile.getBshTextures().get(gfxIndex);

                var screenPosition = TileUtil.worldToScreen(xWorldPos, yWorldPos, zoom.getTileWidthHalf(), zoom.getTileHeightHalf());
                var adjustHeight = TileUtil.adjustHeight(zoom.getTileHeightHalf(), TileGraphic.TileHeight.SEA_LEVEL.value, zoom.getElevation());
                screenPosition.y += adjustHeight;
                screenPosition.x -= shipBshTexture.getWidth();
                screenPosition.y -= shipBshTexture.getHeight();
                screenPosition.x -= zoom.getTileWidthHalf() * 0.5f;
                screenPosition.y -= zoom.getTileHeightHalf() * 0.5f;
                positionComponent.screenPosition.x = screenPosition.x;
                positionComponent.screenPosition.y = screenPosition.y;

                positionComponent.size = new Vector2f(shipBshTexture.getWidth(), shipBshTexture.getHeight());

                entity.add(positionComponent);

                // add entity
                engine.addEntity(entity);
            }
        }

        engine.addSystem(new SpriteRenderSystem(context, currentZoom, camera));
        engine.getSystem(SpriteRenderSystem.class).setProcessing(true);
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Nothing
     */
    public void input() {}

    /**
     * Update sandbox.
     */
    public void update() {
        // change zoom
        if (context.engine.getWindow().isKeyPressed(GLFW_KEY_1)) {
            currentZoom = Zoom.SGFX;
            camera.resetPosition(currentZoom);
        }

        if (context.engine.getWindow().isKeyPressed(GLFW_KEY_2)) {
            currentZoom = Zoom.MGFX;
            camera.resetPosition(currentZoom);
        }

        if (context.engine.getWindow().isKeyPressed(GLFW_KEY_3)) {
            currentZoom = Zoom.GFX;
            camera.resetPosition(currentZoom);
        }

        camera.update(context.engine.getWindow(), context.engine.getMouseInput(), currentZoom);
    }

    /**
     * Renders the sandbox.
     */
    public void render() {
        water.render(camera, false, currentZoom);
        engine.update(0.0f);
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the Sandbox.");

        camera.cleanUp();
        water.cleanUp();

        engine.clearPools();
    }
}
