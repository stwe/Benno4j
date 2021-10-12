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

package de.sg.benno.ogl.physics;

import de.sg.benno.ogl.Config;
import de.sg.benno.ogl.OglEngine;
import de.sg.benno.ogl.camera.OrthographicCamera;
import de.sg.benno.ogl.renderer.SpriteRenderer;
import de.sg.benno.ogl.renderer.RenderUtil;
import de.sg.benno.ogl.resource.Texture;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents an Aabb (axis-aligned bounding box) for collision detection.
 */
public class Aabb {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * Highlights an {@link Aabb}.
     */
    private static final String AABB_FILE = Config.AABB_DEBUG_TEXTURE_PATH;

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The position in screen space.
     */
    public Vector2f position = new Vector2f(0.0f, 0.0f);

    /**
     * The size in screen space.
     */
    public Vector2f size = new Vector2f(Config.WIDTH, Config.HEIGHT);

    /**
     * Renders the {@link #aabbTexture} to highlight this {@link Aabb}.
     */
    private SpriteRenderer spriteRenderer;

    /**
     * The {@link Texture} to highlight this {@link Aabb}.
     */
    private Texture aabbTexture;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Aabb} object.
     */
    public Aabb() {
    }

    /**
     * Constructs a new {@link Aabb} object.
     *
     * @param position The position in screen space.
     * @param size The size in screen space.
     */
    public Aabb(Vector2f position, Vector2f size) {
        this.position = position;
        this.size = size;
    }

    /**
     * Constructs a new {@link Aabb} object.
     *
     * @param engine The parent {@link OglEngine} object.
     * @throws IOException If an I/O error is thrown.
     */
    public Aabb(OglEngine engine) throws IOException {
        LOGGER.debug("Creates Aabb object.");

        this.spriteRenderer = new SpriteRenderer(Objects.requireNonNull(engine, "engine must not be null"));
        this.aabbTexture = engine.getResourceManager().getTextureResource(AABB_FILE);
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Renders the {@link #aabbTexture} to highlight this {@link Aabb}.
     *
     * @param camera The {@link OrthographicCamera} object.
     */
    public void render(OrthographicCamera camera) {
        if (spriteRenderer != null) {
            spriteRenderer.render(camera.getViewMatrix(), aabbTexture, RenderUtil.createModelMatrix(position, size));
        }
    }

    //-------------------------------------------------
    // Collision check
    //-------------------------------------------------

    /**
     * Point vs Aabb.
     *
     * @param point A {@link Vector2f} point in screen space.
     * @param aabb An {@link Aabb}
     *
     * @return True if collision
     */
    public static boolean pointVsAabb(Vector2f point, Aabb aabb) {
        return
                point.x >= aabb.position.x &&
                point.y >= aabb.position.y &&
                point.x < aabb.position.x + aabb.size.x &&
                point.y < aabb.position.y + aabb.size.y;
    }

    /**
     * Aabb vs aabb.
     *
     * @param a An {@link Aabb}
     * @param b Another {@link Aabb}
     *
     * @return True if collision
     */
    public static boolean aabbVsAabb(Aabb a, Aabb b) {
        return
                a.position.x < b.position.x + b.size.x &&
                a.position.x + a.size.x > b.position.x &&
                a.position.y < b.position.y + b.size.y &&
                a.position.y + a.size.y > b.position.y;
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the Aabb.");

        if (spriteRenderer != null) {
            spriteRenderer.cleanUp();
        }
    }
}
