/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno;

import de.sg.benno.chunk.Island5;
import de.sg.benno.chunk.WorldData;
import de.sg.benno.file.ImageFile;
import de.sg.benno.input.Camera;
import de.sg.benno.renderer.*;
import de.sg.benno.state.Context;
import de.sg.ogl.input.KeyInput;
import de.sg.ogl.input.MouseInput;
import de.sg.ogl.renderer.TileRenderer;
import de.sg.ogl.resource.Texture;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_3;

/**
 * Represents a complete game world.
 */
public class World {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    /**
     * The width of the world in tiles.
     */
    public static final int WORLD_WIDTH = 500;

    /**
     * The height of the world in tiles.
     */
    public static final int WORLD_HEIGHT = 350;

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * Provides data for drawing. For example the {@link Island5} objects from a loaded GAM file.
     */
    private final WorldData provider;

    /**
     * The {@link Context} object.
     */
    private final Context context;

    /**
     * The {@link Camera} object.
     */
    private final Camera camera;

    /**
     * The current {@link Zoom}.
     */
    private Zoom currentZoom = Zoom.GFX;

    /**
     * Enable and disable wireframe mode.
     */
    private boolean wireframe = false;

    /**
     * The {@link Water} object.
     */
    private Water water;

    /**
     * The {@link Terrain} object.
     */
    private Terrain terrain;

    /**
     * The {@link MiniMap} of this world.
     */
    private MiniMap miniMap;



    private Texture rectangle;
    private Texture highlight;
    private ImageFile corner;
    private TileRenderer tileRenderer;
    public Vector2i cell = new Vector2i(0, 0);
    public Vector2i offset = new Vector2i(0, 0);
    public Vector2i selected = new Vector2i(0, 0);



    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link World} object.
     *
     * @param provider A {@link WorldData} object (e.g. {@link de.sg.benno.file.GamFile}).
     * @param context The {@link Context} object.
     * @throws Exception If an error is thrown.
     */
    public World(WorldData provider, Context context) throws Exception {
        LOGGER.debug("Creates World object from provider class {}.", provider.getClass());

        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");
        this.camera = new Camera(0, 0, currentZoom);

        rectangle = context.engine.getResourceManager().loadResource(Texture.class, "/debug/frame.png");
        highlight = context.engine.getResourceManager().loadResource(Texture.class, "/debug/red.png");
        corner = new ImageFile("debug/corner.png");
        tileRenderer = new TileRenderer(context.engine);

        init();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #camera}.
     *
     * @return {@link #camera}
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * Get {@link #currentZoom}.
     *
     * @return {@link #currentZoom}
     */
    public Zoom getCurrentZoom() {
        return currentZoom;
    }

    /**
     * Get {@link #miniMap}.
     *
     * @return {@link #miniMap}
     */
    public MiniMap getMiniMap() {
        return miniMap;
    }

    //-------------------------------------------------
    // Setter
    //-------------------------------------------------

    /**
     * Set {@link #currentZoom}.
     *
     * @param currentZoom {@link #currentZoom}
     */
    public void setCurrentZoom(Zoom currentZoom) {
        this.currentZoom = currentZoom;
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Initialize world content.
     *
     * @throws Exception If an error is thrown.
     */
    private void init() throws Exception {
        // create water
        water = new Water(provider, context);

        // create terrain
        //terrain = new Terrain(provider, context);

        // create and render minimap to a Fbo (creates a texture)
        //miniMap = new MiniMap(provider, context, camera);
        //miniMap.renderToFbo();
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Handle mouse and keyboard input.
     */
    public void input() {
        // wireframe flag
        if (KeyInput.isKeyPressed(GLFW_KEY_G)) {
            wireframe = !wireframe;
        }

        // change zoom
        if (KeyInput.isKeyPressed(GLFW_KEY_1)) {
            currentZoom = Zoom.SGFX;
            camera.resetPosition(currentZoom);
        }

        if (KeyInput.isKeyPressed(GLFW_KEY_2)) {
            currentZoom = Zoom.MGFX;
            camera.resetPosition(currentZoom);
        }

        if (KeyInput.isKeyPressed(GLFW_KEY_3)) {
            currentZoom = Zoom.GFX;
            camera.resetPosition(currentZoom);
        }

        camera.input(currentZoom);
    }

    /**
     * Update world.
     *
     * @param dt The delta time.
     */
    public void update(float dt) {
        camera.update(dt);
        //terrain.update(dt);
    }

    /**
     * Renders the world.
     */
    public void render() {
        water.render(camera, wireframe, currentZoom);
        //terrain.render(camera, wireframe, currentZoom);

        // work out active cell in screen space
        cell.x = (int) MouseInput.getX() / currentZoom.defaultTileWidth;        // 64
        cell.y = (int)MouseInput.getY() / (currentZoom.defaultTileHeight + 1); // 31 + 1

        // work out mouse offset into cell in screen space
        offset.x = (int)MouseInput.getX() % currentZoom.defaultTileWidth;
        offset.y = (int)MouseInput.getY() % (currentZoom.defaultTileHeight + 1);

        // work out selected tile in world space
        var origin = new Vector2i(0);
        origin.x = (int)camera.position.x / 64;
        origin.y = (int)camera.position.y / 32;

        selected.x = (cell.y + origin.y) + (cell.x + origin.x);
        selected.y = (cell.y + origin.y) - (cell.x + origin.x);

        selected.x += 1;
        selected.y -= 1;

        var cellScreenSpace = new Vector2i(cell);

        // "Bodge" selected cell by sampling corners
        var pixel = corner.getFastRGB(offset.x, offset.y);
        if (pixel[0] == 255 && pixel[1] == 0 && pixel[2] == 0) {
            //debugText = "red | selected.x - 1";
            cellScreenSpace.x -= 1;
        } else if (pixel[0] == 0 && pixel[1] == 255 && pixel[2] == 0) {
            //debugText = "green | selected.y - 1";
            cellScreenSpace.y -= 1;
        } else  if (pixel[0] == 0 && pixel[1] == 0 && pixel[2] == 255) {
            //debugText = "blue | selected.y + 1";
            cellScreenSpace.y += 1;
        } else if (pixel[0] == 255 && pixel[1] == 255 && pixel[2] == 0) {
            //debugText = "yellow | selected.x + 1";
            cellScreenSpace.x += 1;
        } else {
            //debugText = "";
        }

        // draw rectangle
        tileRenderer.render(
                rectangle.getId(),
                new Vector2f(cell.x * 64.0f, cell.y * 32.0f),
                new Vector2f(64.0f, 32.0f)
        );

        // highlight tile
        tileRenderer.render(
                highlight.getId(),
                new Vector2f(cell.x * 64.0f, cell.y * 32.0f),
                new Vector2f(64.0f, 32.0f)
        );
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Start clean up for the World.");

        water.cleanUp();
        //terrain.cleanUp();
        //miniMap.cleanUp();
    }
}
