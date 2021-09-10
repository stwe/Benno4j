/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl.resource;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import static de.sg.ogl.Log.LOGGER;

public class ResourceManager {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    private final HashMap<String, ShaderProgram> shaderPrograms = new HashMap<>();

    private final HashMap<String, Texture> textures = new HashMap<>();

    //-------------------------------------------------
    // Load
    //-------------------------------------------------

    public ShaderProgram getShaderProgramResource(String path, Object... args) {
        Objects.requireNonNull(path, "path must not be null");

        var shaderProgramOptional = Optional.ofNullable(shaderPrograms.get(path));
        if (shaderProgramOptional.isPresent()) {
            LOGGER.debug("The shader program {} was already loaded.", path);
            return shaderProgramOptional.get();
        }

        LOGGER.debug("The shader program {} must be loaded.", path);

        // todo add
    }

    public Texture getTextureResource(String path, Object... args) {
        Objects.requireNonNull(path, "path must not be null");

        var textureOptional = Optional.ofNullable(textures.get(path));
        if (textureOptional.isPresent()) {
            LOGGER.debug("The texture {} was already loaded.", path);
            return textureOptional.get();
        }

        LOGGER.debug("The texture {} must be loaded.", path);

        // todo add
    }
}
