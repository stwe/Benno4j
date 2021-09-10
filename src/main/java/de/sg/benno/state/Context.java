/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.state;

import de.sg.benno.file.BennoFiles;
import de.sg.benno.ogl.OglEngine;
import de.sg.benno.ogl.state.StateContext;

public class Context implements StateContext {
    public OglEngine engine;
    public BennoFiles bennoFiles;
}
