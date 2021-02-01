import de.sg.benno.data.DataFiles;
import de.sg.benno.file.BennoFiles;
import de.sg.benno.file.BshFile;
import de.sg.benno.file.PaletteFile;
import de.sg.benno.renderer.TileRenderer;
import de.sg.ogl.Color;
import de.sg.ogl.OpenGL;
import de.sg.ogl.SgOglApplication;
import org.joml.Vector2f;

import java.io.IOException;

public class BennoApp extends SgOglApplication {

    private TileRenderer renderer;
    private DataFiles dataFiles;
    private BshFile bshFile;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public BennoApp() throws IOException, IllegalAccessException {
    }

    //-------------------------------------------------
    // Override
    //-------------------------------------------------

    @Override
    public void init() throws Exception {
        var filesystem = new BennoFiles("E:\\Anno");

        var paletteFile = new PaletteFile(filesystem.getOtherBshFilePath(BennoFiles.OtherBshFile.PALETTE));

        renderer = new TileRenderer(getEngine());
        dataFiles = new DataFiles();
        bshFile = new BshFile(
                filesystem.getInterfaceBshFilePath(BennoFiles.InterfaceBshFile.START),
                paletteFile.getPalette(),
                true
                );
    }

    @Override
    public void input() {

    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void render() {
        OpenGL.setClearColor(Color.CORNFLOWER_BLUE);
        OpenGL.clear();

        // background
        renderer.render(bshFile.getBshTextures().get(0), new Vector2f(0.0f, 0.0f));

        // ship
        renderer.render(bshFile.getBshTextures().get(14), new Vector2f(500.0f, 359.0f));

        // singleplayer
        renderer.render(bshFile.getBshTextures().get(2), new Vector2f(113.0f, 362.0f));

        // multiplayer
        renderer.render(bshFile.getBshTextures().get(3), new Vector2f(113.0f, 415.0f));

        // options
        renderer.render(bshFile.getBshTextures().get(4), new Vector2f(113.0f, 469.0f));

        // credits
        renderer.render(bshFile.getBshTextures().get(5), new Vector2f(113.0f, 523.0f));

        // intro
        renderer.render(bshFile.getBshTextures().get(6), new Vector2f(113.0f, 574.0f));

        // exit
        renderer.render(bshFile.getBshTextures().get(7), new Vector2f(113.0f, 630.0f));
    }

    @Override
    public void renderImGui() {

    }

    @Override
    public void cleanUp() {
        bshFile.cleanUp();
        renderer.cleanUp();
    }
}
