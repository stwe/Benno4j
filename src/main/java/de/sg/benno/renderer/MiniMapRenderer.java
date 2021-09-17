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

package de.sg.benno.renderer;

import de.sg.benno.ogl.OpenGL;
import de.sg.benno.ogl.buffer.Vao;
import de.sg.benno.ogl.resource.ShaderProgram;
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
     * The {@link Vao} object.
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
     * Constructs a new {@link MiniMapRenderer} object.
     *
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public MiniMapRenderer(Context context) throws Exception {
        LOGGER.debug("Creates MiniMapRenderer object.");

        Objects.requireNonNull(context, "context must not be null");

        this.shaderProgram = context.engine.getResourceManager().getShaderProgramResource(SHADER_NAME);
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
        vao.add2DQuadVbo();
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

        shaderProgram.bind();

        Texture.bindForReading(bottomLayer.getId(), GL_TEXTURE0);
        Texture.bindForReading(shipsLayer.getId(), GL_TEXTURE1);
        Texture.bindForReading(cameraLayer.getId(), GL_TEXTURE2);

        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix
                .identity()
                .translate(new Vector3f(position, 0.0f))
                .scale(new Vector3f(size, 1.0f));

        shaderProgram.setUniform("model", modelMatrix);
        shaderProgram.setUniform("bottomLayer", 0);
        shaderProgram.setUniform("shipsLayer", 1);
        shaderProgram.setUniform("cameraLayer", 2);

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
        LOGGER.debug("Clean up MiniMapRenderer.");

        vao.cleanUp();
    }
}
