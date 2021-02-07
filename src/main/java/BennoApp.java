import de.sg.benno.file.BennoFiles;
import de.sg.benno.file.BshFile;
import de.sg.benno.file.PaletteFile;
import de.sg.ogl.Color;
import de.sg.ogl.OpenGL;
import de.sg.ogl.SgOglApplication;
import de.sg.ogl.gui.Anchor;
import de.sg.ogl.gui.Gui;
import org.joml.Vector2f;

import java.io.IOException;

public class BennoApp extends SgOglApplication {

    private BshFile bshFile;
    private Gui gui;

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

        bshFile = new BshFile(
                filesystem.getInterfaceBshFilePath(BennoFiles.InterfaceBshFile.START),
                paletteFile.getPalette(),
                true
                );

        gui = new Gui(getEngine());

        var panel0 = gui.addPanel(
                Anchor.TOP_LEFT,
                new Vector2f(0.0f, 0.0f),
                bshFile.getBshTextures().get(0).getTexture().getWidth(),
                bshFile.getBshTextures().get(0).getTexture().getHeight(),
                Color.WHITE,
                bshFile.getBshTextures().get(0).getTexture()
        );

        var button0 = panel0.addButton(
                Anchor.TOP_RIGHT,
                new Vector2f(0.0f, 0.0f),
                bshFile.getBshTextures().get(2).getTexture().getWidth(),
                bshFile.getBshTextures().get(2).getTexture().getHeight(),
                Color.WHITE,
                bshFile.getBshTextures().get(2).getTexture()
        );

        var i = 0;
    }

    @Override
    public void input() {
        gui.input();
    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void render() {
        OpenGL.setClearColor(Color.CORNFLOWER_BLUE);
        OpenGL.clear();

        gui.render();

        /*
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
        */
    }

    @Override
    public void renderImGui() {

    }

    @Override
    public void cleanUp() {
        gui.cleanUp();
        bshFile.cleanUp();
    }
}
