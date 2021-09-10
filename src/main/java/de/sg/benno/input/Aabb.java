/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.input;

import de.sg.benno.ogl.Config;
import de.sg.benno.ogl.resource.Texture;
import de.sg.benno.renderer.TileGraphicRenderer;
import de.sg.benno.state.Context;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * 2D collision detection with axis-aligned bounding boxes (AABB).
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
     * Renders a single texture.
     */
    private final TileGraphicRenderer tileGraphicRenderer;

    /**
     * The texture to render the {@link Aabb}.
     */
    private final Texture rectangleTexture;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Aabb} object.
     *
     * @param context {@link Context}
     * @throws Exception If an error is thrown.
     */
    public Aabb(Context context) throws Exception {
        LOGGER.debug("Creates Aabb object.");

        this.tileGraphicRenderer = new TileGraphicRenderer(context);
        this.rectangleTexture = context.engine.getResourceManager().loadResource(Texture.class, AABB_FILE);
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
    // Logic
    //-------------------------------------------------

    /**
     * Renders an {@link Aabb}.
     *
     * @param camera {@link Camera}
     */
    public void render(Camera camera) {
        tileGraphicRenderer.render(
                camera,
                rectangleTexture,
                getModelMatrix()
        );
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Creates a model matrix for rendering.
     *
     * @return {@link Matrix4f}
     */
    private Matrix4f getModelMatrix() {
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix
            .identity()
            .translate(new Vector3f(position, 0.0f))
            .scale(new Vector3f(size, 1.0f));

        return modelMatrix;
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the Aabb.");

        tileGraphicRenderer.cleanUp();
    }
}
