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

import de.sg.benno.BennoConfig;
import de.sg.benno.BennoRuntimeException;
import de.sg.benno.input.Camera;
import de.sg.benno.data.Building;
import de.sg.benno.file.BshFile;
import de.sg.benno.ogl.OpenGL;
import de.sg.benno.ogl.buffer.Vao;
import de.sg.benno.ogl.buffer.Vbo;
import de.sg.benno.ogl.resource.ShaderProgram;
import de.sg.benno.ogl.resource.Texture;
import de.sg.benno.state.Context;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.util.ArrayList;
import java.util.Arrays;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;

/**
 * Represents a WaterRenderer.
 */
public class WaterRenderer {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * Indicated that a tile is unselected.
     */
    private static final int WATER_TILE_IS_UNSELECTED = 1;

    /**
     * Indicated that a tile is selected.
     */
    private static final int WATER_TILE_IS_SELECTED = 2;

    /**
     * The name of the used shader.
     */
    private static final String SHADER_NAME = BennoConfig.WATER_RENDERER_SHADER_FOLDER;

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link ArrayList<Integer>} object with water gfx start index.
     */
    private final ArrayList<Integer> waterGfxStartIndex;

    /**
     * A {@link Building} object.
     */
    private final Building building;

    /**
     * The {@link Context} object.
     */
    private final Context context;

    /**
     * The {@link Zoom} using in this renderer.
     */
    private final Zoom zoom;

    /**
     * The {@link ShaderProgram} using in this renderer.
     */
    private final ShaderProgram shaderProgram;

    /**
     * The {@link Vao} object.
     */
    private Vao vao;

    /**
     * Number of instances to render.
     */
    private final int instances;

    /**
     * The width of all textures.
     */
    private final int textureWidth;

    /**
     * The height of all textures.
     */
    private final int textureHeight;

    /**
     * The {@link BshFile} object.
     */
    private final BshFile bshFile;

    /**
     * The texture array.
     */
    private Texture textureArray;

    /**
     * The {@link Vbo} for texture index data.
     */
    private Vbo textureVbo;

    /**
     * The {@link Vbo} for "selected" data.
     * Every tile has a flag indicating whether it has been selected.
     */
    private Vbo selectedVbo;

    /**
     * The start time in milliseconds.
     */
    private static long last;

    /**
     * The current frame.
     */
    private int frame = 0;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link WaterRenderer} object.
     *
     * @param modelMatrices {@link ArrayList<Matrix4f>}
     * @param waterGfxStartIndex {@link ArrayList<Integer>}
     * @param building {@link Building}
     * @param context {@link Context}
     * @param zoom {@link Zoom}
     * @throws Exception If an error is thrown.
     */
    public WaterRenderer(
            ArrayList<Matrix4f> modelMatrices,
            ArrayList<Integer> waterGfxStartIndex,
            Building building,
            Context context,
            Zoom zoom
    ) throws Exception {
        this.waterGfxStartIndex = waterGfxStartIndex;
        this.building = building;
        this.context = context;
        this.zoom = zoom;

        this.shaderProgram = context.engine.getResourceManager().getShaderProgramResource(SHADER_NAME);

        this.instances = modelMatrices.size();
        this.textureWidth = zoom.getTileWidth();
        this.textureHeight = zoom.getTileHeight();
        this.bshFile = context.bennoFiles.getStadtfldBshFile(zoom);

        initVao(modelMatrices);

        last = System.currentTimeMillis();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Renders the whole water area.
     *
     * @param camera {@link Camera}
     * @param wireframe True if a wireframe is to be rendered.
     * @param animated To toggle the animation on and off.
     */
    public void render(Camera camera, boolean wireframe, boolean animated) {
        if (animated) {
            var now = System.currentTimeMillis();
            var delta = now - last;
            if (delta >= building.animTime) {
                frame = (frame + 1) % building.animAnz;
                updateGfxStartIndexVbo();
                last = now;
            }
        }

        if (!wireframe) {
            OpenGL.enableAlphaBlending();
        } else {
            OpenGL.enableWireframeMode();
        }

        shaderProgram.bind();

        Texture.bindForReading(textureArray.getId(), GL_TEXTURE0, GL_TEXTURE_2D_ARRAY);

        shaderProgram.setUniform("projection", context.engine.getWindow().getOrthographicProjectionMatrix());
        shaderProgram.setUniform("view", camera.getViewMatrix());
        shaderProgram.setUniform("sampler", 0);

        vao.bind();
        vao.drawInstanced(GL_TRIANGLES, instances);
        vao.unbind();

        ShaderProgram.unbind();

        if (!wireframe) {
            OpenGL.disableBlending();
        } else {
            OpenGL.disableWireframeMode();
        }
    }

    /**
     * Renders the whole water area.
     *
     * @param camera {@link Camera}
     * @param wireframe True if a wireframe is to be rendered.
     */
    public void render(Camera camera, boolean wireframe) {
        render(camera, wireframe, true);
    }

    //-------------------------------------------------
    // Init Vao/Vbo
    //-------------------------------------------------

    /**
     * Setup {@link #vao}.
     *
     * @param modelMatrices The model matrices to store.
     */
    private void initVao(ArrayList<Matrix4f> modelMatrices) {
        vao = new Vao();
        vao.add2DQuadVbo();

        addModelMatricesVbo(modelMatrices);
        addTextureStartIndexVbo();

        // all tiles are unselected by default
        addSelectedVbo();

        createTextureArray();
    }

    /**
     * Add model matrices to a new {@link Vbo}.
     *
     * @param modelMatrices The model matrices to store.
     */
    private void addModelMatricesVbo(ArrayList<Matrix4f> modelMatrices) {
        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // store model matrices (static draw)
        vbo.storeMatrix4f(modelMatrices);

        // set buffer layout
        vbo.addFloatAttribute(2, 4, 16, 0, true);
        vbo.addFloatAttribute(3, 4, 16, 4, true);
        vbo.addFloatAttribute(4, 4, 16, 8, true);
        vbo.addFloatAttribute(5, 4, 16, 12, true);

        // unbind vao
        vao.unbind();
    }

    /**
     * Add {@link #waterGfxStartIndex} to a new {@link Vbo}.
     */
    private void addTextureStartIndexVbo() {
        // bind vao
        vao.bind();

        // create and add new vbo
        textureVbo = vao.addVbo();

        // store index (dynamic draw)
        textureVbo.storeInteger(waterGfxStartIndex, GL_DYNAMIC_DRAW);

        // set buffer layout
        textureVbo.addIntAttribute(6, 1, 1, 0, true);

        // unbind vao
        vao.unbind();
    }

    /**
     * Every tile has a flag indicating whether it has been selected.
     * Add this data to a new {@link Vbo}.
     * 1 = {@link #WATER_TILE_IS_UNSELECTED}; 2 = {@link #WATER_TILE_IS_SELECTED} (results in a darker color)
     */
    private void addSelectedVbo() {
        // set default values (unselected = 1)
        var values = new Integer[instances];
        Arrays.fill(values, WATER_TILE_IS_UNSELECTED);
        var selectedValues = new ArrayList<>(Arrays.asList(values));

        // bind vao
        vao.bind();

        // create and add new vbo
        selectedVbo = vao.addVbo();

        // store default values (dynamic draw)
        selectedVbo.storeInteger(selectedValues, GL_DYNAMIC_DRAW);

        // set buffer layout
        selectedVbo.addIntAttribute(7, 1, 1, 0, true);

        // unbind vao
        vao.unbind();
    }

    //-------------------------------------------------
    // Update Vbo
    //-------------------------------------------------

    /**
     * Updates the selected flag.
     *
     * @param index The instance of the tile.
     */
    public void updateSelectedVbo(int index) {
        if (index >= 0) {
            var ib = BufferUtils.createIntBuffer(1);
            ib.put(WATER_TILE_IS_SELECTED);
            ib.flip();

            selectedVbo.storeSubData((long)index * Integer.BYTES, ib);
        }
    }

    /**
     * Update water gfx index Vbo.
     */
    private void updateGfxStartIndexVbo() {
        // todo: glBufferData ist nicht die schnellste Lösung
        /*
        When replacing the entire data store, consider using glBufferSubData
        rather than completely recreating the data store with glBufferData.
        This avoids the cost of reallocating the data store.
        */

        textureVbo.storeInteger(
                i -> (i + frame) % building.animAnz,
                waterGfxStartIndex, GL_DYNAMIC_DRAW);
    }

    //-------------------------------------------------
    // Texture array
    //-------------------------------------------------

    /**
     * Creates a texture array from {@link #building}.
     */
    private void createTextureArray() {
        var gfxCount = building.animAnz * building.animAdd;
        var startGfx = building.gfx;
        var endGfx = startGfx + gfxCount;

        // new texture
        textureArray = new Texture();

        // specify texture array storage requirements
        Texture.textureArrayStorageRequirements(textureArray.getId(), textureWidth, textureHeight, gfxCount);

        // store textures in texture array
        var zOffset = 0;
        for (var i = startGfx; i < endGfx; i++) {
            // check the size
            var currentTexture = bshFile.getBshTextures().get(i);
            if (currentTexture.getWidth() != textureWidth || currentTexture.getHeight() != textureHeight) {
                throw new BennoRuntimeException("Invalid texture size.");
            }

            // store texture
            Texture.bufferedImageToTextureArray(textureArray.getId(), currentTexture.getBufferedImage(), zOffset);

            zOffset++;
        }
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Clean up WaterRenderer for {}.", zoom);

        vao.cleanUp();
        textureArray.cleanUp();
    }
}
