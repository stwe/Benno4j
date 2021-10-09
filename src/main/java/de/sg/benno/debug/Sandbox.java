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

import de.sg.benno.BennoConfig;
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.content.Water;
import de.sg.benno.chunk.Island5;
import de.sg.benno.chunk.WorldData;
import de.sg.benno.ecs.components.GfxIndexComponent;
import de.sg.benno.ecs.components.PositionComponent;
import de.sg.benno.ecs.components.ZoomComponent;
import de.sg.benno.ecs.core.Ecs;
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
     * A {@link Ecs} - an Entity Component System.
     */
    private Ecs ecs;

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

        initEcs();
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
     * Initialize {@link Ecs}.
     *
     * @throws Exception If an error is thrown.
     */
    private void initEcs() throws Exception {
        ecs = new Ecs(GfxIndexComponent.class, PositionComponent.class, ZoomComponent.class);

        ecs.getSystemManager().addSystem(new SpriteRenderSystem(
                context, camera,
                ecs, 0, GfxIndexComponent.class, PositionComponent.class, ZoomComponent.class)
        );

        createEntities();
    }

    /**
     * Creates entities to display.
     *
     * @throws Exception If an error is thrown.
     */
    private void createEntities() throws Exception {
        var em = ecs.getEntityManager();

        for (var zoom : Zoom.values()) {
            for (var ship : provider.getShips4List()) {
                // new entity
                var entity = em.createEntity();

                entity.debugName = ship.name + "_" + zoom;

                // add gfx index component
                var gfxIndexComponent = entity.addComponent(GfxIndexComponent.class);
                gfxIndexComponent.get().gfxIndex = ship.getCurrentGfxIndex();

                // add position component
                var xWorldPos = ship.xPos + 1;
                var yWorldPos = ship.yPos - 1;

                var positionComponent = entity.addComponent(PositionComponent.class);
                positionComponent.get().worldPosition.x = xWorldPos;
                positionComponent.get().worldPosition.y = yWorldPos;

                var shipBshFile = context.bennoFiles.getShipBshFile(zoom);
                var shipBshTexture = shipBshFile.getBshTextures().get(ship.getCurrentGfxIndex());

                var screenPosition = TileUtil.worldToScreen(xWorldPos, yWorldPos, zoom.getTileWidthHalf(), zoom.getTileHeightHalf());
                var adjustHeight = TileUtil.adjustHeight(zoom.getTileHeightHalf(), TileGraphic.TileHeight.SEA_LEVEL.value, zoom.getElevation());
                screenPosition.y += adjustHeight;
                screenPosition.x -= shipBshTexture.getWidth();
                screenPosition.y -= shipBshTexture.getHeight();
                screenPosition.x -= zoom.getTileWidthHalf() * 0.5f;
                screenPosition.y -= zoom.getTileHeightHalf() * 0.5f;
                positionComponent.get().screenPosition.x = screenPosition.x;
                positionComponent.get().screenPosition.y = screenPosition.y;
                positionComponent.get().size = new Vector2f(shipBshTexture.getWidth(), shipBshTexture.getHeight());

                // add zoom component
                var zoomComponent = entity.addComponent(ZoomComponent.class);
                zoomComponent.get().zoom = zoom;
            }
        }
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
        if (context.engine.getWindow().isKeyPressed(GLFW_KEY_1)) {
            changeZoomTo(Zoom.SGFX);
        }

        if (context.engine.getWindow().isKeyPressed(GLFW_KEY_2)) {
            changeZoomTo(Zoom.MGFX);
        }

        if (context.engine.getWindow().isKeyPressed(GLFW_KEY_3)) {
            changeZoomTo(Zoom.GFX);
        }

        camera.update(context.engine.getWindow(), context.engine.getMouseInput(), currentZoom);
    }

    /**
     * Renders the sandbox.
     */
    public void render() {
        water.render(camera, false, currentZoom);
        ecs.render();
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Reinitialize stuff when the zoom has changed.
     *
     * @param zoom The new {@link Zoom}.
     */
    private void changeZoomTo(Zoom zoom) {
        currentZoom = zoom;
        camera.resetPosition(currentZoom);

        // todo: SpriteRenderSystem über den Zoomwechsel informieren -> Event
        var spriteRenderSystemOptional = ecs.getSystemManager().getSystem(SpriteRenderSystem.class);
        spriteRenderSystemOptional.ifPresent(spriteRenderSystem -> spriteRenderSystem.setCurrentZoom(currentZoom));
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
        ecs.cleanUp();
    }
}
