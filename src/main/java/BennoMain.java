/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

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
