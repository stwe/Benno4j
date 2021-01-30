import de.sg.ogl.SgOglEngine;

public class BennoMain {

    //-------------------------------------------------
    // Main
    //-------------------------------------------------

    public static void main(String[] args) {
        try {
            var engine = new SgOglEngine(new BennoApp());
            engine.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
