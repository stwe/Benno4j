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

package de.sg.benno.ogl.renderer;

import de.sg.benno.ogl.Config;
import de.sg.benno.ogl.OglEngine;
import de.sg.benno.ogl.OpenGL;
import de.sg.benno.ogl.buffer.Vao;
import de.sg.benno.ogl.resource.ShaderProgram;
import de.sg.benno.ogl.resource.Texture;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.Objects;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

/**
 * Represents a SpriteRenderer. Renders a single textured 2D quad.
 */
public class SpriteRenderer {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * The name of the used shader.
     */
    private static final String SHADER_NAME = Config.SPRITE_RENDERER_SHADER_FOLDER;

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The parent {@link OglEngine} object.
     */
    private final OglEngine engine;

    /**
     * A {@link Vao} object.
     */
    private final Vao vao;

    /**
     * The {@link ShaderProgram} using in this renderer.
     */
    private final ShaderProgram shaderProgram;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link SpriteRenderer} object.
     *
     * @param engine The parent {@link OglEngine} object.
     * @throws IOException If an I/O error is thrown.
     */
    public SpriteRenderer(OglEngine engine) throws IOException {
        this.engine = Objects.requireNonNull(engine, "engine must not be null");

        this.vao = new Vao();
        this.vao.add2DQuadVbo();

        this.shaderProgram = engine.getResourceManager().getShaderProgramResource(SHADER_NAME);
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Renders a single textured 2D quad.
     *
     * @param viewMatrix The view {@link Matrix4f} object.
     * @param texture The {@link Texture} to render.
     * @param modelMatrix The model {@link Matrix4f} object.
     */
    public void render(Matrix4f viewMatrix, Texture texture, Matrix4f modelMatrix) {
        var textureId = texture.getId();

        OpenGL.enableAlphaBlending();

        shaderProgram.bind();
        Texture.bindForReading(textureId, GL_TEXTURE0);

        shaderProgram.setUniform("projection", engine.getWindow().getOrthographicProjectionMatrix());
        shaderProgram.setUniform("view", viewMatrix);
        shaderProgram.setUniform("model", modelMatrix);
        shaderProgram.setUniform("diffuseMap", 0);

        vao.bind();
        vao.drawPrimitives(GL_TRIANGLES);
        vao.unbind();

        ShaderProgram.unbind();
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
        LOGGER.debug("Clean up SpriteRenderer.");

        vao.cleanUp();
    }
}
