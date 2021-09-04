/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a logger.
 */
public class Log {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    static public final Logger LOGGER = LogManager.getRootLogger();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    private Log() {}
}
