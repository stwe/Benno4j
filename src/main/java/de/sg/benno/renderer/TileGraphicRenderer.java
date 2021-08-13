/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.renderer;

import de.sg.benno.input.Camera;
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.file.BshFile;
import de.sg.benno.state.Context;
import de.sg.ogl.OpenGL;
import de.sg.ogl.buffer.Vao;
import de.sg.ogl.resource.Geometry;
import de.sg.ogl.resource.Shader;
import de.sg.ogl.resource.Texture;
import org.joml.Matrix4f;

import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

/**
 * Represents a TileGraphicRenderer.
 * Renders a single {@link de.sg.benno.chunk.TileGraphic} or a single {@link Texture}.
 */
public class TileGraphicRenderer {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * The name of the used shader.
     */
    private static final String SHADER_NAME = "sprite";

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link Context} object.
     */
    private final Context context;

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
     * Constructs a new {@link TileGraphicRenderer} object.
     *
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public TileGraphicRenderer(Context context) throws Exception {
        LOGGER.debug("Creates TileGraphicRenderer object.");

        this.context = Objects.requireNonNull(context, "context must not be null");
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
     * Renders a single {@link TileGraphic}.
     *
     * @param camera The {@link Camera} object.
     * @param tileGraphic The {@link TileGraphic} to render.
     * @param bshFile The {@link BshFile} to get the texture.
     */
    public void render(Camera camera, TileGraphic tileGraphic, BshFile bshFile) {
        var bshTexture = bshFile.getBshTextures().get(tileGraphic.gfx);
        var textureId = bshTexture.getTexture().getId();

        OpenGL.enableAlphaBlending();

        shader.bind();
        Texture.bindForReading(textureId, GL_TEXTURE0);

        shader.setUniform("projection", context.engine.getWindow().getOrthographicProjectionMatrix());
        shader.setUniform("view", camera.getViewMatrix());
        shader.setUniform("model", tileGraphic.getModelMatrix());
        shader.setUniform("diffuseMap", 0);

        vao.bind();
        vao.drawPrimitives(GL_TRIANGLES);
        vao.unbind();

        Shader.unbind();
        Texture.unbind();

        OpenGL.disableBlending();
    }

    /**
     * Renders a single {@link Texture}.
     *
     * @param camera The {@link Camera} object.
     * @param texture The {@link Texture} to render.
     * @param modelMatrix The model matrix.
     */
    public void render(Camera camera, Texture texture, Matrix4f modelMatrix) {
        var textureId = texture.getId();

        OpenGL.enableAlphaBlending();

        shader.bind();
        Texture.bindForReading(textureId, GL_TEXTURE0);

        shader.setUniform("projection", context.engine.getWindow().getOrthographicProjectionMatrix());
        shader.setUniform("view", camera.getViewMatrix());
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
        LOGGER.debug("Clean up TileGraphicRenderer.");

        this.vao.cleanUp();
    }
}
