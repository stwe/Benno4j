/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.renderer;

import de.sg.benno.file.BshTexture;
import de.sg.ogl.Log;
import de.sg.ogl.OpenGL;
import de.sg.ogl.SgOglEngine;
import de.sg.ogl.buffer.Vao;
import de.sg.ogl.camera.OrthographicCamera;
import de.sg.ogl.resource.Geometry;
import de.sg.ogl.resource.Shader;
import de.sg.ogl.resource.Texture;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

/**
 * @deprecated replaced by {@link WaterRenderer}.
 */
public class DeepWaterRenderer {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    private final SgOglEngine engine;

    private final Geometry quadGeometry;
    private final Shader shader;
    private final Vao vao;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public DeepWaterRenderer(SgOglEngine engine) throws Exception {
        this.engine = engine;

        this.quadGeometry = engine.getResourceManager().loadGeometry(Geometry.GeometryId.QUAD_2D);
        this.shader = engine.getResourceManager().loadResource(Shader.class, "sprite");
        this.vao = new Vao();

        this.initVao();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    public void renderTile(BshTexture texture, Vector2f position, OrthographicCamera camera) {
        OpenGL.enableAlphaBlending();
        shader.bind();
        Texture.bindForReading(texture.getTexture().getId(), GL_TEXTURE0);

        var size = new Vector2f(texture.getBufferedImage().getWidth(), texture.getBufferedImage().getHeight());

        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix
                .identity()
                .translate(new Vector3f(position, 0.0f))
                .scale(new Vector3f(size, 1.0f));

        shader.setUniform("model", modelMatrix);
        shader.setUniform("view", camera.getViewMatrix());
        shader.setUniform("projection", new Matrix4f(engine.getWindow().getOrthographicProjectionMatrix()));
        shader.setUniform("diffuseMap", 0);

        vao.bind();
        vao.drawPrimitives(GL_TRIANGLES);
        vao.unbind();

        OpenGL.disableBlending();
        Shader.unbind();
        Texture.unbind();
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    private void initVao() {
        vao.addVbo(quadGeometry.vertices, quadGeometry.drawCount, quadGeometry.defaultBufferLayout);
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    public void cleanUp() {
        Log.LOGGER.debug("Clean up DeepWaterRenderer.");

        this.vao.cleanUp();
    }
}
