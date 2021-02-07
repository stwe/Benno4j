import de.sg.benno.file.BennoFiles;
import de.sg.benno.file.BshFile;
import de.sg.benno.file.PaletteFile;
import de.sg.benno.gui.MainMenu;
import de.sg.ogl.Color;
import de.sg.ogl.OpenGL;
import de.sg.ogl.SgOglApplication;

import java.io.IOException;

public class BennoApp extends SgOglApplication {

    private BshFile startBshFile;
    private MainMenu mainMenu;

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

        startBshFile = new BshFile(
                filesystem.getInterfaceBshFilePath(BennoFiles.InterfaceBshFile.START),
                paletteFile.getPalette(),
                false
                );

        mainMenu = new MainMenu(getEngine(), startBshFile);
    }

    @Override
    public void input() {
        mainMenu.getMainMenu().input();
    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void render() {
        OpenGL.setClearColor(Color.CORNFLOWER_BLUE);
        OpenGL.clear();

        mainMenu.getMainMenu().render();
    }

    @Override
    public void renderImGui() {

    }

    @Override
    public void cleanUp() {
        mainMenu.getMainMenu().cleanUp();
        startBshFile.cleanUp();
    }
}
