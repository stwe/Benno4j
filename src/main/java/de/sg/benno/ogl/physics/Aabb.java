/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
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
    private static final String AABB_FILE = "/debug/aabb.png";

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
    private final SpriteRenderer spriteRenderer;

    /**
     * The {@link Texture} to highlight this {@link Aabb}.
     */
    private final Texture aabbTexture;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

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
        spriteRenderer.render(camera.getViewMatrix(), aabbTexture, RenderUtil.createModelMatrix(position, size));
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
     * @param b An other {@link Aabb}
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

        spriteRenderer.cleanUp();
    }
}
