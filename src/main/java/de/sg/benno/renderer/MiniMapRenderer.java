/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.renderer;

import de.sg.benno.ogl.OpenGL;
import de.sg.benno.ogl.resource.Texture;
import de.sg.benno.state.Context;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL13.*;

/**
 * Represents a MiniMapRenderer.
 */
public class MiniMapRenderer {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * The name of the used shader.
     */
    private static final String SHADER_NAME = "miniMap";

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link Geometry} object.
     */
    private final Geometry quadGeometry;

    /**
     * The {@link Vao} object.
     */
    private final Vao vao;

    /**
     * The {@link Shader} using in this renderer.
     */
    private final Shader shader;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link MiniMapRenderer} object.
     *
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public MiniMapRenderer(Context context) throws Exception {
        LOGGER.debug("Creates MiniMapRenderer object.");

        Objects.requireNonNull(context, "context must not be null");

        this.quadGeometry = context.engine.getResourceManager().loadGeometry(Geometry.GeometryId.QUAD_2D);
        this.shader = context.engine.getResourceManager().loadResource(Shader.class, SHADER_NAME);
        this.vao = new Vao();

        this.initVao();
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Initializes a {@link Vao} to render a single quad.
     */
    private void initVao() {
        vao.addVbo(quadGeometry.vertices, quadGeometry.drawCount, quadGeometry.defaultBufferLayout);
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Renders the minimap.
     * The texture is flipped in y direction.
     *
     * @param bottomLayer The bottom layer {@link Texture}.
     * @param shipsLayer The ships layer {@link Texture}.
     * @param cameraLayer The camera {@link Texture}.
     * @param position The position of the texture.
     * @param size The size of the texture.
     */
    public void render(
            Texture bottomLayer,
            Texture shipsLayer,
            Texture cameraLayer,
            Vector2f position,
            Vector2f size
    ) {
        OpenGL.enableAlphaBlending();

        shader.bind();

        Texture.bindForReading(bottomLayer.getId(), GL_TEXTURE0);
        Texture.bindForReading(shipsLayer.getId(), GL_TEXTURE1);
        Texture.bindForReading(cameraLayer.getId(), GL_TEXTURE2);

        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix
                .identity()
                .translate(new Vector3f(position, 0.0f))
                .scale(new Vector3f(size, 1.0f));

        shader.setUniform("model", modelMatrix);
        shader.setUniform("bottomLayer", 0);
        shader.setUniform("shipsLayer", 1);
        shader.setUniform("cameraLayer", 2);

        vao.bind();
        vao.drawPrimitives(GL_TRIANGLES);
        vao.unbind();

        Shader.unbind();
        Texture.unbind();

        OpenGL.disableBlending();
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Clean up MiniMapRenderer.");

        this.vao.cleanUp();
    }
}
