import de.sg.benno.file.BennoFiles;
import de.sg.benno.file.BshFile;
import de.sg.benno.file.PaletteFile;
import de.sg.benno.renderer.TileRenderer;
import de.sg.benno.renderer.Zoom;
import de.sg.ogl.SgOglApplication;

import java.io.IOException;

public class BennoApp extends SgOglApplication {

    private BshFile bshFile;
    private BshFile menuBshFile;
    private TileRenderer renderer;

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
        var filesystem = new BennoFiles("/home/steffen/Anno");

        var paletteFile = new PaletteFile(filesystem.getOtherBshFilePath(BennoFiles.OtherBshFile.PALETTE));

        /*
        var bshPath= filesystem.getZoomableBshFilePath(Zoom.ZoomId.GFX, Zoom.ZoomableBshFile.STADTFLD);
        if (bshPath.isPresent()) {
            bshFile = new BshFile(bshPath.get(), paletteFile.getPalette());
        }
        */

        //renderer = new TileRenderer(getEngine());

        menuBshFile = new BshFile(
                filesystem.getInterfaceBshFilePath(BennoFiles.InterfaceBshFile.EDITOR),
                paletteFile.getPalette(),
                false
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
        //renderer.render(bshFile.getBshTextures().get(100));
    }

    @Override
    public void renderImGui() {

    }

    @Override
    public void cleanUp() {
        //bshFile.cleanUp();
        //renderer.cleanUp();
    }
}
