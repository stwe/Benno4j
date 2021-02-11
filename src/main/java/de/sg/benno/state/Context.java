/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.state;

import de.sg.benno.file.BennoFiles;
import de.sg.benno.file.PaletteFile;
import de.sg.ogl.SgOglEngine;
import de.sg.ogl.state.StateContext;

public class Context implements StateContext {
    public SgOglEngine engine;
    public BennoFiles filesystem;
    public PaletteFile paletteFile;
}
