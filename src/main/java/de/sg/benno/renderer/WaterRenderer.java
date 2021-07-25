/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.renderer;

import de.sg.benno.BennoRuntimeException;
import de.sg.benno.data.Building;
import de.sg.benno.file.BshFile;
import de.sg.benno.state.Context;
import de.sg.ogl.OpenGL;
import de.sg.ogl.buffer.Vao;
import de.sg.ogl.buffer.Vbo;
import de.sg.ogl.buffer.Vertex2D;
import de.sg.ogl.camera.OrthographicCamera;
import de.sg.ogl.resource.Geometry;
import de.sg.ogl.resource.Shader;
import de.sg.ogl.resource.Texture;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL45.glTextureStorage3D;
import static org.lwjgl.opengl.GL45.glTextureSubImage3D;

/**
 * Represents a WaterRenderer.
 */
public class WaterRenderer {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * Color (blue) if no texture is used.
     */
    private static final Vector3f WATER_COLOR = new Vector3f(0.0f, 0.0f, 1.0f);

    /**
     * Number of vertices to be rendered per instance.
     */
    private static final int DRAW_COUNT = 6;

    /**
     * Number of texture levels.
     */
    private static final int MIP_LEVEL_COUNT = 1;

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
    private static final String SHADER_NAME = "deepWater";

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link ArrayList<Matrix4f>} object with model matrices.
     */
    private final ArrayList<Matrix4f> modelMatrices;

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
     * The {@link Shader} using in this renderer.
     */
    private final Shader shader;

    /**
     * The {@link Vao} object.
     */
    private final Vao vao;

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
     * The texture array id.
     */
    private int textureArrayId;

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
        this.modelMatrices = modelMatrices;
        this.waterGfxStartIndex = waterGfxStartIndex;
        this.building = building;
        this.context = context;
        this.zoom = zoom;
        this.shader = context.engine.getResourceManager().loadResource(Shader.class, SHADER_NAME);
        this.vao = new Vao();
        this.vao.setDrawCount(DRAW_COUNT);
        this.instances = modelMatrices.size();
        this.textureWidth = zoom.defaultTileWidth;
        this.textureHeight = zoom.defaultTileHeight;
        this.bshFile = context.bennoFiles.getStadtfldBshFile(zoom);

        initVao();

        last = System.currentTimeMillis();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Renders the whole water area.
     *
     * @param camera {@link OrthographicCamera}
     */
    public void render(OrthographicCamera camera, boolean wireframe) {
        // todo: tmp code
        var now = System.currentTimeMillis();
        var delta = now - last;
        if (delta >= building.animTime) {
            frame = (frame + 1) % building.animAnz;
            updateGfxStartIndexVbo();
            last = now;
        }

        if (!wireframe) {
            OpenGL.enableAlphaBlending();
        } else {
            OpenGL.enableWireframeMode();
        }

        shader.bind();

        Texture.bindForReading(textureArrayId, GL_TEXTURE0, GL_TEXTURE_2D_ARRAY);

        shader.setUniform("projection", context.engine.getWindow().getOrthographicProjectionMatrix());
        shader.setUniform("view", camera.getViewMatrix());
        shader.setUniform("sampler", 0);

        vao.bind();
        vao.drawInstanced(GL_TRIANGLES, instances);
        vao.unbind();

        Shader.unbind();

        if (!wireframe) {
            OpenGL.disableBlending();
        } else {
            OpenGL.disableWireframeMode();
        }
    }

    //-------------------------------------------------
    // Init Vao/Vbo
    //-------------------------------------------------

    /**
     * Setup {@link #vao}.
     */
    private void initVao() {
        addMeshVbo();
        addModelMatricesVbo();
        addTextureIdsVbo();
        addSelectedVbo();

        createTextureArray();
    }

    /**
     * Add a 2DQuad to a new {@link de.sg.ogl.buffer.Vbo}.
     */
    private void addMeshVbo() {
        var quadGeometry = context.engine.getResourceManager().loadGeometry(Geometry.GeometryId.QUAD_2D);
        for (var vertex : quadGeometry.vertices) {
            vertex.color = WATER_COLOR;
        }

        vao.addVbo(Vertex2D.toFloatArray(quadGeometry.vertices), quadGeometry.defaultBufferLayout);
    }

    /**
     * Add {@link #modelMatrices} to a new {@link de.sg.ogl.buffer.Vbo}.
     */
    private void addModelMatricesVbo() {
        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // store model matrices (static draw)
        vbo.storeMatrix4fInstances(modelMatrices, instances);

        // set buffer layout
        vbo.addFloatAttribute(3, 4, 16, 0, true);
        vbo.addFloatAttribute(4, 4, 16, 4, true);
        vbo.addFloatAttribute(5, 4, 16, 8, true);
        vbo.addFloatAttribute(6, 4, 16, 12, true);

        // unbind vao
        vao.unbind();
    }

    /**
     * Add {@link #waterGfxStartIndex} to a new {@link de.sg.ogl.buffer.Vbo}.
     */
    private void addTextureIdsVbo() {
        // bind vao
        vao.bind();

        // create and add new vbo
        textureVbo = vao.addVbo();

        // store index (dynamic draw)
        textureVbo.storeIntegerInstances(waterGfxStartIndex, instances, GL_DYNAMIC_DRAW);

        // set buffer layout
        textureVbo.addIntAttribute(7, 1, 1, 0, true);

        // unbind vao
        vao.unbind();
    }

    /**
     * Every tile has a flag indicating whether it has been selected.
     * Add this data to a new {@link de.sg.ogl.buffer.Vbo}.
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

        // store index (dynamic draw)
        selectedVbo.storeIntegerInstances(selectedValues, instances, GL_DYNAMIC_DRAW);

        // set buffer layout
        selectedVbo.addIntAttribute(8, 1, 1, 0, true);

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

            selectedVbo.storeData((long)index * Integer.BYTES, ib);
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

        textureVbo.storeIntegerInstances(
                i -> (i + frame) % building.animAnz,
                waterGfxStartIndex, instances, GL_DYNAMIC_DRAW);
    }

    //-------------------------------------------------
    // Texture array
    //-------------------------------------------------

    /**
     * Creates a texture array from {@link #building}.
     */
    private void createTextureArray() {
        textureArrayId = Texture.generateNewTextureId();
        Texture.bind(textureArrayId, GL_TEXTURE_2D_ARRAY);

        var gfxCount = building.animAnz * building.animAdd;
        var startGfx = building.gfx;
        var endGfx = startGfx + gfxCount;

        glTextureStorage3D(textureArrayId, MIP_LEVEL_COUNT, GL_RGBA8, textureWidth, textureHeight, gfxCount);

        var zOffset = 0;

        for (var i = startGfx; i < endGfx; i++) {
            var currentTexture = bshFile.getBshTextures().get(i);
            if (currentTexture.getWidth() != textureWidth || currentTexture.getHeight() != textureHeight) {
                throw new BennoRuntimeException("Invalid texture size.");
            }

            var dbb = (DataBufferInt) currentTexture.getBufferedImage().getRaster().getDataBuffer();

            glTextureSubImage3D(
                    textureArrayId,
                    0,
                    0, 0,
                    zOffset,
                    textureWidth, textureHeight,
                    1,
                    GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV,
                    dbb.getData()
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
        LOGGER.debug("Clean up WaterRenderer for {}.", zoom);

        this.vao.cleanUp();
    }
}
