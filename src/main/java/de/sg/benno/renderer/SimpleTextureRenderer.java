/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.renderer;

import de.sg.benno.state.Context;
import de.sg.ogl.OpenGL;
import de.sg.ogl.buffer.Vao;
import de.sg.ogl.resource.Geometry;
import de.sg.ogl.resource.Shader;
import de.sg.ogl.resource.Texture;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

/**
 * Represents a SimpleTextureRenderer.
 * Renders a {@link Texture}.
 */
public class SimpleTextureRenderer {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * The name of the used shader.
     */
    private static final String SHADER_NAME = "simpleTexture";

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
     * Constructs a new {@link SimpleTextureRenderer} object.
     *
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public SimpleTextureRenderer(Context context) throws Exception {
        LOGGER.debug("Creates SimpleTextureRenderer object.");

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
     * Renders a {@link Texture}.
     * The texture is flipped in y direction.
     *
     * @param texture The {@link Texture} to render.
     * @param position The position of the texture.
     * @param size The size of the texture.
     */
    public void render(Texture texture, Vector2f position, Vector2f size) {
        OpenGL.enableAlphaBlending();

        shader.bind();
        Texture.bindForReading(texture.getId(), GL_TEXTURE0);

        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix
                .identity()
                .translate(new Vector3f(position, 0.0f))
                .scale(new Vector3f(size, 1.0f));

        shader.setUniform("model", modelMatrix);
        shader.setUniform("diffuseMap", 0);

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
        LOGGER.debug("Clean up SimpleTextureRenderer.");

        this.vao.cleanUp();
    }
}
