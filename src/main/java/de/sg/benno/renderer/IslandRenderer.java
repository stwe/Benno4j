/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.renderer;

import de.sg.benno.BennoConfig;
import de.sg.benno.TileAtlas;
import de.sg.benno.chunk.TileGraphic;
import de.sg.benno.input.Camera;
import de.sg.benno.state.Context;
import de.sg.ogl.Config;
import de.sg.ogl.OpenGL;
import de.sg.ogl.buffer.Vao;
import de.sg.ogl.buffer.Vbo;
import de.sg.ogl.buffer.Vertex2D;
import de.sg.ogl.resource.Geometry;
import de.sg.ogl.resource.Shader;
import de.sg.ogl.resource.Texture;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static de.sg.benno.TileAtlas.*;
import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;

/**
 * Represents an IslandRenderer.
 */
public class IslandRenderer {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * Number of vertices to be rendered per instance.
     */
    private static final int DRAW_COUNT = 6;

    /**
     * The name of the used shader.
     */
    private static final String SHADER_NAME = "island";

    /**
     * The configured frame time.
     */
    private final int DELTA = (int) (1000 / Config.FPS);

    /**
     * Indicated that a tile is unselected.
     */
    private static final int TILE_IS_UNSELECTED = 1;

    /**
     * Indicated that a tile is selected.
     */
    private static final int TILE_IS_SELECTED = 2;

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The {@link TileGraphic} objects for each {@link Zoom}.
     */
    private final HashMap<Zoom, ArrayList<TileGraphic>> tileGraphics;

    /**
     * The {@link Context} object.
     */
    private final Context context;

    /**
     * The {@link Shader} using in this renderer.
     */
    private final Shader shader;

    /**
     * The model matrices for each {@link Zoom}.
     */
    private final HashMap<Zoom, ArrayList<Matrix4f>> modelMatrices = new HashMap<>();

    /**
     * Number of instances to render.
     */
    private int instances = 0;

    /**
     * Specifies from which atlas image the texture is to be taken.
     */
    private final HashMap<Zoom, ArrayList<Integer>> textureAtlasIndex = new HashMap<>();

    /**
     * Animation info (current Gfx, start Gfx, count, frame time).
     */
    private final ArrayList<Integer> animationInfo = new ArrayList<>();

    /**
     * Additional animation info (animAdd, rotate, orientation).
     */
    private final ArrayList<Integer> animationAddInfo = new ArrayList<>();

    /**
     * Precalculated texture offsets.
     * Specifies where the texture is on the atlas image (coordinates).
     */
    private final HashMap<Zoom, ArrayList<Float>> offsets = new HashMap<>();

    /**
     * The height of each texture.
     */
    private final HashMap<Zoom, ArrayList<Float>> yBuffer = new HashMap<>();

    /**
     * The {@link Vbo} for "selected" data.
     * Every tile has a flag indicating whether it has been selected.
     */
    private final HashMap<Zoom, Vbo> selectedVbos = new HashMap<>();

    /**
     * The {@link Vao} objects for each {@link Zoom}.
     */
    private final HashMap<Zoom, Vao> vaos = new HashMap<>();

    /**
     * The number of previous updates.
     */
    private int updates = 0;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link IslandRenderer} object.
     *
     * @param tileGraphics The {@link TileGraphic} objects of an {@link de.sg.benno.chunk.Island5}.
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public IslandRenderer(HashMap<Zoom, ArrayList<TileGraphic>> tileGraphics, Context context) throws Exception {
        this.tileGraphics = tileGraphics;
        this.context = context;
        this.shader = context.engine.getResourceManager().loadResource(Shader.class, SHADER_NAME);

        // for each zoom...
        for (var zoom: Zoom.values()) {
            // prepare data
            createModelMatrices(zoom);
            createTextureInfo(zoom);

            // load data into gpu
            addVao(zoom);
        }
    }

    //-------------------------------------------------
    // Prepare data
    //-------------------------------------------------

    /**
     * Stores the model matrix of the {@link TileGraphic} objects.
     *
     * @param zoom {@link Zoom}
     */
    private void createModelMatrices(Zoom zoom) {
        var matrices = new ArrayList<Matrix4f>();
        for (var tile : tileGraphics.get(zoom)) {
            matrices.add(tile.getModelMatrix());
        }

        modelMatrices.put(zoom, matrices);

        if (instances == 0) {
            instances = matrices.size();
        }
    }

    /**
     * Stores the information about which texture is used.
     *
     * @param zoom {@link Zoom}
     */
    private void createTextureInfo(Zoom zoom) {
        var offsetList = new ArrayList<Float>();
        var textureAtlasIndexList = new ArrayList<Integer>();
        var heightBuffer = new ArrayList<Float>();

        var rows = 0;
        switch (zoom) {
            case GFX: rows = NR_OF_GFX_ROWS; break;
            case MGFX: rows = NR_OF_MGFX_ROWS; break;
            case SGFX: rows = NR_OF_SGFX_ROWS; break;
            default:
        }

        for (var tile : tileGraphics.get(zoom)) {
            var buildings = context.bennoFiles.getDataFiles().getBuildings();
            var building = buildings.get(tile.parentTile.graphicId);

            // add animation info; is the same for every zoom; is only filled once (4 ints per instance)
            if (animationInfo.size() != instances * 4) {
                animationInfo.add(tile.gfx);          // current gfx
                animationInfo.add(building.gfx);      // start gfx
                animationInfo.add(building.animAnz);  // anim count
                animationInfo.add(building.animTime); // frame time
            }

            // add other animation info; is the same for every zoom; is only filled once (3 ints per instance)
            if (animationAddInfo.size() != instances * 3) {
                animationAddInfo.add(building.animAdd);            // animAdd
                animationAddInfo.add(building.rotate);             // rotate
                animationAddInfo.add(tile.parentTile.orientation); // orientation
            }

            // offset
            var index = tile.gfx % (rows * rows);
            var offset = TileAtlas.getTextureOffset(index, rows);
            offsetList.add(offset.x);
            offsetList.add(offset.y);

            // texture atlas index
            textureAtlasIndexList.add(tile.gfx / (rows * rows));

            // yBuffer (height of each tile graphic)
            heightBuffer.add(tile.size.y);
        }

        offsets.put(zoom, offsetList);
        textureAtlasIndex.put(zoom, textureAtlasIndexList);
        yBuffer.put(zoom, heightBuffer);
    }

    //-------------------------------------------------
    // Data into Gpu
    //-------------------------------------------------

    /**
     * Creates a Vao and all Vbos.
     *
     * @param zoom {@link Zoom}
     */
    private void addVao(Zoom zoom) {
        createVao(zoom);
        addMeshVbo(zoom);
        addModelMatricesVbo(zoom);
        addAnimationInfoVbo(zoom);
        addTextureAtlasIndexVbo(zoom);
        addTextureOffsetsVbo(zoom);
        addYVbo(zoom);
        addAnimationAddInfoVbo(zoom);
        addSelectedVbo(zoom);
    }

    /**
     * Creates a new {@link Vao} for a given {@link Zoom}.
     *
     * @param zoom {@link Zoom}.
     */
    private void createVao(Zoom zoom) {
        var vao = new Vao();
        vao.setDrawCount(DRAW_COUNT);
        vaos.put(zoom, vao);
    }

    /**
     * Stores a new 2D Quad to the {@link Vao} for the given {@link Zoom}.
     *
     * @param zoom {@link Zoom}.
     */
    private void addMeshVbo(Zoom zoom) {
        var quadGeometry = context.engine.getResourceManager().loadGeometry(Geometry.GeometryId.QUAD_2D);
        var vao = vaos.get(zoom);
        vao.addVbo(Vertex2D.toFloatArray(quadGeometry.vertices), quadGeometry.defaultBufferLayout);
    }

    /**
     * Stores the {@link #modelMatrices} for the given {@link Zoom} to the {@link Vao}.
     *
     * @param zoom {@link Zoom}.
     */
    private void addModelMatricesVbo(Zoom zoom) {
        var vao = vaos.get(zoom);
        var matrices = modelMatrices.get(zoom);

        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // store model matrices (static draw)
        vbo.storeMatrix4fInstances(matrices, instances);

        // set buffer layout
        vbo.addFloatAttribute(3, 4, 16, 0, true);
        vbo.addFloatAttribute(4, 4, 16, 4, true);
        vbo.addFloatAttribute(5, 4, 16, 8, true);
        vbo.addFloatAttribute(6, 4, 16, 12, true);

        // unbind vao
        vao.unbind();
    }

    /**
     * Stores {@link #animationInfo} to the {@link Vao}.
     *
     * @param zoom {@link Zoom}.
     */
    private void addAnimationInfoVbo(Zoom zoom) {
        var vao = vaos.get(zoom);

        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // store animation info (static draw) - 4 ints per instance
        vbo.storeIntegerInstances(animationInfo, instances * 4, GL_STATIC_DRAW);

        // set buffer layout
        vbo.addIntAttribute(7, 4, 4, 0, true);

        // unbind vao
        vao.unbind();
    }

    /**
     * Stores the {@link #textureAtlasIndex} for the given {@link Zoom} to the {@link Vao}.
     *
     * @param zoom {@link Zoom}.
     */
    private void addTextureAtlasIndexVbo(Zoom zoom) {
        var vao = vaos.get(zoom);

        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // store index (static draw)
        vbo.storeIntegerInstances(textureAtlasIndex.get(zoom), instances, GL_STATIC_DRAW);

        // set buffer layout
        vbo.addIntAttribute(8, 1, 1, 0, true);

        // unbind vao
        vao.unbind();
    }

    /**
     * Stores the {@link #offsets} for the given {@link Zoom} to the {@link Vao}.
     *
     * @param zoom {@link Zoom}.
     */
    private void addTextureOffsetsVbo(Zoom zoom) {
        var vao = vaos.get(zoom);

        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // store offsets (static draw)
        vbo.storeFloatArrayList(offsets.get(zoom), GL_STATIC_DRAW);

        // set buffer layout
        vbo.addFloatAttribute(9, 2, 2, 0, true);

        // unbind vao
        vao.unbind();
    }

    /**
     * Stores the {@link #yBuffer} for the given {@link Zoom} to the {@link Vao}.
     *
     * @param zoom {@link Zoom}.
     */
    private void addYVbo(Zoom zoom) {
        var vao = vaos.get(zoom);

        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // store buffer (static draw)
        vbo.storeFloatArrayList(yBuffer.get(zoom), GL_STATIC_DRAW);

        // set buffer layout
        vbo.addFloatAttribute(10, 1, 1, 0, true);

        // unbind vao
        vao.unbind();
    }

    /**
     * Stores {@link #animationAddInfo} to the {@link Vao}.
     *
     * @param zoom {@link Zoom}.
     */
    private void addAnimationAddInfoVbo(Zoom zoom) {
        var vao = vaos.get(zoom);

        // bind vao
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // store animation add info (static draw) - 3 ints per instance
        vbo.storeIntegerInstances(animationAddInfo, instances * 3, GL_STATIC_DRAW);

        // set buffer layout
        vbo.addIntAttribute(11, 3, 3, 0, true);

        // unbind vao
        vao.unbind();
    }

    /**
     * Every tile has a flag indicating whether it has been selected.
     * Add this data to a new {@link de.sg.ogl.buffer.Vbo} for each {@link Zoom}.
     * 1 = {@link #TILE_IS_UNSELECTED}; 2 = {@link #TILE_IS_SELECTED} (results in a darker color)
     *
     *  @param zoom {@link Zoom}.
     */
    private void addSelectedVbo(Zoom zoom) {
        // set default values (unselected = 1)
        var values = new Integer[instances];
        Arrays.fill(values, TILE_IS_UNSELECTED);
        var selectedValues = new ArrayList<>(Arrays.asList(values));

        // bind vao
        var vao = vaos.get(zoom);
        vao.bind();

        // create and add new vbo
        var vbo = vao.addVbo();

        // store index (dynamic draw)
        vbo.storeIntegerInstances(selectedValues, instances, GL_DYNAMIC_DRAW);

        // set buffer layout
        vbo.addIntAttribute(12, 1, 1, 0, true);

        // store vbo
        selectedVbos.put(zoom, vbo);

        // unbind vao
        vao.unbind();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Update renderer.
     *
     * @param dt The delta time.
     */
    public void update(float dt) {
        /*
        max values:
        max anzahl 16 (time 90) = 1440 ms
        max time 220 (anzahl 6) = 1320 ms
        */

        // todo: die game engine ruft die Update methode
        // todo: nur 60x pro Sekunde auf
        // todo: Updates zählen und nach dem 90 Aufruf auf 0 setzen
        updates++;
        if (updates > 90) {
            updates = 0;
        }
    }

    /**
     * Render the {@link de.sg.benno.chunk.Island5}.
     *
     * @param camera {@link Camera} to get the view matrix.
     * @param wireframe True or false whether to render a wireframe model.
     * @param zoom {@link Zoom}
     */
    public void render(Camera camera, boolean wireframe, Zoom zoom) {
        if (!wireframe) {
            OpenGL.enableAlphaBlending();
        } else {
            OpenGL.enableWireframeMode();
        }

        var textureId = 0;
        var maxYHeight = 0.0f;
        var rows = 0.0f;
        switch (zoom) {
            case GFX:
                textureId = TileAtlas.getGfxTextureArrayId();
                maxYHeight = MAX_GFX_HEIGHT;
                rows = (float)NR_OF_GFX_ROWS;
                break;
            case MGFX:
                textureId = TileAtlas.getMgfxTextureArrayId();
                maxYHeight = MAX_MGFX_HEIGHT;
                rows = (float)NR_OF_MGFX_ROWS;
                break;
            case SGFX:
                textureId = TileAtlas.getSgfxTextureArrayId();
                maxYHeight = MAX_SGFX_HEIGHT;
                rows = (float)NR_OF_SGFX_ROWS;
                break;
            default:
        }

        shader.bind();

        Texture.bindForReading(textureId, GL_TEXTURE0, GL_TEXTURE_2D_ARRAY);

        shader.setUniform("projection", context.engine.getWindow().getOrthographicProjectionMatrix());
        shader.setUniform("view", camera.getViewMatrix());
        shader.setUniform("maxY", maxYHeight);
        shader.setUniform("nrOfRows", rows);
        shader.setUniform("sampler", 0);
        shader.setUniform("updates", updates);
        shader.setUniform("delta", DELTA);
        shader.setUniform("showGrid", BennoConfig.SHOW_GRID);

        var vao = vaos.get(zoom);
        vao.bind();
        vao.drawInstanced(GL_TRIANGLES, instances);
        vao.unbind();

        Shader.unbind();
        Texture.unbind();

        if (!wireframe) {
            OpenGL.disableBlending();
        } else {
            OpenGL.disableWireframeMode();
        }
    }

    //-------------------------------------------------
    // Update Vbo
    //-------------------------------------------------

    /**
     * Updates the selected flag.
     *
     * @param zoom {@link Zoom}
     * @param index The instance of the tile.
     */
    public void updateSelectedVbo(Zoom zoom, int index) {
        if (index >= 0) {
            var ib = BufferUtils.createIntBuffer(1);
            ib.put(TILE_IS_SELECTED);
            ib.flip();

            selectedVbos.get(zoom).storeData((long)index * Integer.BYTES, ib);
        }
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the IslandRenderer.");

        for (var zoom : Zoom.values()) {
            var vao = vaos.get(zoom);
            vao.cleanUp();
        }
    }
}
