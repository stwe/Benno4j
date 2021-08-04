/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.renderer;

import de.sg.benno.file.BennoFiles;
import de.sg.benno.file.ImageFile;
import de.sg.benno.input.Camera;
import de.sg.benno.state.Context;
import de.sg.ogl.OpenGL;
import de.sg.ogl.buffer.Vao;
import de.sg.ogl.resource.Geometry;
import de.sg.ogl.resource.Shader;
import de.sg.ogl.resource.Texture;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL45.glTextureStorage3D;
import static org.lwjgl.opengl.GL45.glTextureSubImage3D;

public class IslandAtlasRenderer {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * The name of the used shader.
     */
    private static final String SHADER_NAME = "islandAtlas";

    private static final int NR_OF_GFX_ATLAS_IMAGES = 24; // 24 textures a (16 * 16) pics = 6144
    private static final int NR_OF_GFX_ROWS = 16;
    private static final int GFX_MAX_WIDTH = 64;
    private static final int GFX_MAX_HEIGHT = 286;

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
     * The {@link Shader} using in this renderer.
     */
    private final Shader shader;

    /**
     * The {@link Vao} object.
     */
    private final Vao vao;

    /**
     * The texture array Id.
     */
    private int textureArrayId;

    /**
     * A list of GFX tile atlas images.
     */
    private final ArrayList<ImageFile> gfxAtlasTextures = new ArrayList<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link IslandAtlasRenderer} object.
     *
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public IslandAtlasRenderer(Context context) throws Exception {
        LOGGER.debug("Creates IslandAtlasRenderer object.");

        this.context = Objects.requireNonNull(context, "context must not be null");
        this.quadGeometry = context.engine.getResourceManager().loadGeometry(Geometry.GeometryId.QUAD_2D);
        this.shader = context.engine.getResourceManager().loadResource(Shader.class, SHADER_NAME);
        this.vao = new Vao();

        init();
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Initializes a {@link Vao} and all textures to render a single quad.
     */
    private void init() throws Exception {
        vao.addVbo(quadGeometry.vertices, quadGeometry.drawCount, quadGeometry.defaultBufferLayout);
        createTextureArray();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Renders a {@link Texture}.
     * The texture is flipped in y direction.
     *
     * @param camera The {@link Camera} object.
     * @param position The position of the texture.
     * @param size The size of the texture.
     */
    public void render(Camera camera, Vector2f position, Vector2f size) {
        //OpenGL.enableAlphaBlending();
        //OpenGL.enableWireframeMode();

        shader.bind();
        Texture.bindForReading(textureArrayId, GL_TEXTURE0, GL_TEXTURE_2D_ARRAY);

        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix
                .identity()
                .translate(new Vector3f(position, 0.0f))
                .scale(new Vector3f(size, 1.0f));

        shader.setUniform("model", modelMatrix);
        shader.setUniform("view", camera.getViewMatrix());
        shader.setUniform("projection", context.engine.getWindow().getOrthographicProjectionMatrix());
        shader.setUniform("sampler", 0);
        shader.setUniform("nrOfRows", 16.0f);

        // example offset
        shader.setUniform("offset", BennoFiles.getGfxTextureOffset(1, 16));

        vao.bind();
        vao.drawPrimitives(GL_TRIANGLES);
        vao.unbind();

        Shader.unbind();
        Texture.unbind();

        //OpenGL.disableBlending();
    }

    //-------------------------------------------------
    // Load textures
    //-------------------------------------------------

    private void loadGfxTextures() throws Exception {
        for (var i = 0; i < NR_OF_GFX_ATLAS_IMAGES; i++) {
            var path = "atlas/GFX/" + i + ".png";
            var image = new ImageFile(path);
            gfxAtlasTextures.add(image);
        }
    }

    private void createTextureArray() throws Exception {
        textureArrayId = Texture.generateNewTextureId();
        Texture.bind(textureArrayId, GL_TEXTURE_2D_ARRAY);

        loadGfxTextures();
        glTextureStorage3D(textureArrayId, 1, GL_RGBA8, GFX_MAX_WIDTH * NR_OF_GFX_ROWS, GFX_MAX_HEIGHT * NR_OF_GFX_ROWS, NR_OF_GFX_ATLAS_IMAGES);

        var zOffset = 0;
        for (var texture : gfxAtlasTextures) {
            glTextureSubImage3D(
                    textureArrayId,
                    0,
                    0, 0,
                    zOffset,
                    GFX_MAX_WIDTH * NR_OF_GFX_ROWS, GFX_MAX_HEIGHT * NR_OF_GFX_ROWS,
                    1,
                    GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV,
                    texture.getArgb()
            );

            zOffset++;
        }

        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Clean up IslandAtlasRenderer.");

        this.vao.cleanUp();
    }
}
