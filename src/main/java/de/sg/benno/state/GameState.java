/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.state;

import de.sg.benno.BennoRuntimeException;
import de.sg.benno.Camera;
import de.sg.benno.debug.DebugUi;
import de.sg.benno.file.GamFile;
import de.sg.benno.file.ImageFile;
import de.sg.benno.renderer.Zoom;
import de.sg.ogl.input.KeyInput;
import de.sg.ogl.input.MouseInput;
import de.sg.ogl.renderer.TileRenderer;
import de.sg.ogl.resource.Texture;
import de.sg.ogl.state.ApplicationState;
import de.sg.ogl.state.StateMachine;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.nio.file.Path;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.glfw.GLFW.*;

public class GameState extends ApplicationState {

    private GamFile gamFile;

    public Camera camera;

    private boolean wireframe = false;

    public Zoom currentZoom = Zoom.GFX;

    private Texture rectangle;
    private Texture highlight;
    private ImageFile corner;
    private TileRenderer tileRenderer;
    public Vector2i cell = new Vector2i(0, 0);
    public Vector2i offset = new Vector2i(0, 0);
    private DebugUi debugUi;
    public String debugText = "";

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public GameState(StateMachine stateMachine) {
        super(stateMachine);

        LOGGER.debug("Creates GameState object.");
    }

    //-------------------------------------------------
    // Implement State
    //-------------------------------------------------

    @Override
    public void init(Object... params) throws Exception {
        if (params.length != 1) {
            throw new BennoRuntimeException("Wrong total number of params.");
        }

        var path = params[0];

        //var context = (Context)getStateMachine().getStateContext();
        //var files = context.bennoFiles;

        //var buildings = files.getDataFiles().getBuildings();
        //var water = buildings.get(1201);

        /*
        id = 1201
        gfx = 758
        rotate = 0
        randAnz = -1
        animAnz = 6
        animTime = 130
        animFrame = 0
        animAdd = 1
        */


        /*
        2622 (Ruine):    673    Rot: 0   AnimAnz:  -   AnimAdd:  -
        1383 (Wald):     674    Rot: 0   AnimAnz:  5   AnimAdd:  1
               ?         679
        ----------------------------------------------------------
        1253 (Meer):     680    Rot: 0   AnimAnz:  6   AnimAdd:  1
        1203 (Meer):     686    Rot: 0   AnimAnz:  6   AnimAdd:  1
        1252 (Meer):     692    Rot: 1   AnimAnz:  6   AnimAdd:  4
        1202 (Meer):     716    Rot: 1   AnimAnz:  6   AnimAdd:  4
        1254 (Meer):     740    Rot: 0   AnimAnz:  6   AnimAdd:  1
        1204 (Meer):     746    Rot: 0   AnimAnz:  6   AnimAdd:  1
        1251 (Meer):     752    Rot: 0   AnimAnz:  6   AnimAdd:  1
        1201 (Meer):     758    Rot: 0   AnimAnz:  6   AnimAdd:  1
        1259 (Meer):     764    Rot: 1   AnimAnz:  6   AnimAdd:  4
        1209 (Meer):     788    Rot: 1   AnimAnz:  6   AnimAdd:  4
        ----------------------------------------------------------
        1205 (Brandung)  812

        1206 (Brandeck)  836
        1207 (Brandeck)  860
        1208 (Mündung)   884

                                1210, 1211, 1212, 1213 fehlen
        1214 (Mündung)   908

        901 (Fluss):    1576    Rot: 1   AnimAnz:  6   AnimAdd:  4
        902 (Fluss):    1600    Rot: 1   AnimAnz:  6   AnimAdd:  4
        903 (Fluss):    1624    Rot: 1   AnimAnz:  6   AnimAdd:  4
        904 (Flusseck): 1648    Rot: 4   AnimAnz:  6   AnimAdd: 16
        905 (Flusseck): 1744    Rot: 4   AnimAnz: 16   AnimAdd: 16
        */


        if (path instanceof Path) {
            loadSavegame((Path)path);
        } else {
            throw new BennoRuntimeException("Invalid parameter type.");
        }

        camera = new Camera();

        var context = (Context)getStateMachine().getStateContext();
        rectangle = context.engine.getResourceManager().loadResource(Texture.class, "/debug/frame.png");
        highlight = context.engine.getResourceManager().loadResource(Texture.class, "/debug/red.png");
        corner = new ImageFile("/debug/corner.png");
        tileRenderer = new TileRenderer(context.engine);
        debugUi = new DebugUi(this);
    }

    @Override
    public void input() {
        // exit game
        if (KeyInput.isKeyPressed(GLFW_KEY_ESCAPE)) {
            var context = (Context)getStateMachine().getStateContext();
            glfwSetWindowShouldClose(context.engine.getWindow().getWindowHandle(), true);
        }

        // wireframe
        if (KeyInput.isKeyPressed(GLFW_KEY_G)) {
            wireframe = !wireframe;
        }

        // zoom
        if (KeyInput.isKeyPressed(GLFW_KEY_1)) {
            currentZoom = Zoom.SGFX;
        }

        if (KeyInput.isKeyPressed(GLFW_KEY_2)) {
            currentZoom = Zoom.MGFX;
        }

        if (KeyInput.isKeyPressed(GLFW_KEY_3)) {
            currentZoom = Zoom.GFX;
        }
    }

    @Override
    public void update(float dt) {
        camera.update(currentZoom);
    }

    @Override
    public void render() {
        gamFile.render(camera, wireframe, currentZoom);

        // todo: Baustelle

        // work out active cell in screen space
        cell.x = (int)MouseInput.getX() / currentZoom.defaultTileWidth;        // 64
        cell.y = (int)MouseInput.getY() / (currentZoom.defaultTileHeight + 1); // 31 + 1

        // work out mouse offset into cell in screen space
        offset.x = (int)MouseInput.getX() % currentZoom.defaultTileWidth;
        offset.y = (int)MouseInput.getY() % (currentZoom.defaultTileHeight + 1);

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

    @Override
    public void renderImGui() {
        debugUi.render();
    }

    @Override
    public void cleanUp() {
        LOGGER.debug("Start clean up for the GameState.");

        gamFile.cleanUp();
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    private void loadSavegame(Path path) throws Exception {
        gamFile = new GamFile(path, (Context)getStateMachine().getStateContext());
    }
}
