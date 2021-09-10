/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl.resource;

import de.sg.benno.ogl.OglRuntimeException;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import static de.sg.benno.ogl.Log.LOGGER;

/**
 * Represents the ResourceManager.
 * This avoids unnecessary loading routines. Wherever we want to load a texture or a shader program,
 * we first check if it hasn't been loaded already.
 */
public class ResourceManager {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * Store all the loaded textures.
     */
    private final HashMap<String, Texture> textures = new HashMap<>();

    /**
     * Store all the loaded shader programs.
     */
    private final HashMap<String, ShaderProgram> shaderPrograms = new HashMap<>();

    //-------------------------------------------------
    // Load
    //-------------------------------------------------

    /**
     * Get a {@link Texture} object. First checks if it hasn't been loaded already.
     *
     * @param path The texture file path.
     * @param args A list of arguments.
     * @return {@link Texture}
     * @throws IOException If an I/O error is thrown.
     */
    public Texture getTextureResource(String path, Object... args) throws IOException {
        Objects.requireNonNull(path, "path must not be null");

        var textureOptional = Optional.ofNullable(textures.get(path));
        if (textureOptional.isPresent()) {
            return textureOptional.get();
        }

        LOGGER.debug("The texture {} must be loaded.", path);

        addTexture(path, args);

        return textures.get(path);
    }

    /**
     * Get a {@link ShaderProgram} object. First checks if it hasn't been loaded already.
     *
     * @param path The shader program file path.
     * @param args A list of arguments.
     * @return {@link ShaderProgram}
     * @throws IOException If an I/O error is thrown.
     */
    public ShaderProgram getShaderProgramResource(String path, Object... args) throws IOException {
        Objects.requireNonNull(path, "path must not be null");

        var shaderProgramOptional = Optional.ofNullable(shaderPrograms.get(path));
        if (shaderProgramOptional.isPresent()) {
            return shaderProgramOptional.get();
        }

        LOGGER.debug("The shader program {} must be loaded.", path);

        addShader(path, args);

        return shaderPrograms.get(path);
    }

    //-------------------------------------------------
    // Add
    //-------------------------------------------------

    /**
     * Loads a {@link Texture} object.
     *
     * @param path The texture file path.
     * @param args A list of arguments.
     * @throws IOException If an I/O error is thrown.
     */
    private void addTexture(String path, Object... args) throws IOException {
        Texture texture;

        if (args.length > 0) {
            if (!(args[0] instanceof Boolean)) {
                throw new OglRuntimeException("Invalid argument given.");
            }
            texture = new Texture(path, (boolean)args[0]);
        } else {
            texture = new Texture(path);
        }

        texture.load();

        textures.put(path, texture);
    }

    /**
     * Loads a {@link ShaderProgram} object.
     *
     * @param path The shader program file path.
     * @param args A list of arguments.
     * @throws IOException If an I/O error is thrown.
     */
    private void addShader(String path, Object... args) throws IOException {
        ShaderProgram shaderProgram;

        if (args.length > 0) {
            if (!(args[0] instanceof EnumSet)) {
                throw new OglRuntimeException("Invalid argument given.");
            }
            shaderProgram = new ShaderProgram(path, (EnumSet<ShaderProgram.Options>)args[0]);
        } else {
            shaderProgram = new ShaderProgram(path);
        }

        shaderProgram.load();

        shaderPrograms.put(path, shaderProgram);
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    /**
     * Clean up.
     */
    public void cleanUp() {
        LOGGER.debug("Clean up Resources.");

        if (!textures.isEmpty()) {
            LOGGER.debug("Clean up {} textures.", textures.size());

            textures.forEach((k, v) -> v.cleanUp());
        } else {
            LOGGER.debug("There is nothing texture to clean up.");
        }

        if (!shaderPrograms.isEmpty()) {
            LOGGER.debug("Clean up {} shader programs.", shaderPrograms.size());

            shaderPrograms.forEach((k, v) -> v.cleanUp());
        } else {
            LOGGER.debug("There is nothing shader program to clean up.");
        }
    }

}
